本地安装
=======
``` 

文档：https://ci.apache.org/projects/flink/flink-docs-release-1.13/zh/docs/try-flink/local_installation/
下载地址：https://archive.apache.org/dist/flink/flink-1.13.0/flink-1.13.0-bin-scala_2.11.tgz

启动集群
./bin/start-cluster.sh

sql
./bin/sql-client.sh

flink 面板
http://127.0.0.1:8081/


```




pipline.txt
==========
sql文件：（提交任务从kafka中实时读取json数据，写入mysql表中）
```sql
SET pipeline.name=kafka_json_mysql;  
SET parallelism.default=1;  
SET execution.savepoint.path=/Users/luguo/app/flink-1.13.0/flink-savepoints/savepoint-e1c449-f44bae04c8ef; 


CREATE TABLE rawlog_kafka_json (
  `event` STRING,
  `uuid` BIGINT,
  `udid` STRING,
  `ts` TIMESTAMP(3) METADATA FROM 'timestamp'
) WITH (
  'connector' = 'kafka',
  'topic' = 'rawlog_json',
  'properties.bootstrap.servers' = 'localhost:9092',
  'properties.group.id' = 'testGroup',
  'scan.startup.mode' = 'earliest-offset',
  'format' = 'json', 
  'json.fail-on-missing-field' = 'false',
  'json.ignore-parse-errors' = 'true'
);

CREATE TABLE rawlog_mysql (
  event STRING,
  uuid BIGINT,
  udid STRING
) WITH (
   'connector' = 'jdbc',
   'url' = 'jdbc:mysql://localhost:3306/flink?user=root&password=123456', 
   'table-name' = 'rawlog'
);


BEGIN STATEMENT SET;

INSERT INTO rawlog_mysql
SELECT event, uuid,udid
FROM rawlog_kafka_json;
 
END;

```


sql-client
==========
```
使用sql-client提交任务
./bin/sql-client.sh -f pipline.txt

[INFO] Submitting SQL update statement to the cluster...
[INFO] SQL update statement has been successfully submitted to the cluster:
Job ID: fcdbd3a24167b96409beed5799cf6193
Shutting down the session...
done.

```


Savepoint
==========
```
触发 Savepoint #
$ bin/flink savepoint jobId targetDirectory
bin/flink savepoint fcdbd3a24167b96409beed5799cf6193 ./flink-savepoints 

使用 Savepoint 取消作业 #
$ bin/flink cancel -s [:targetDirectory] :jobId
bin/flink cancel -s ./flink-savepoints fcdbd3a24167b96409beed5799cf6193

从 Savepoint 恢复 #
$ bin/flink run -s :savepointPath [:runArgs]
bin/flink run -s ./flink-savepoints fcdbd3a24167b96409beed5799cf6193

也可以在pipline.txt指定savepoint.path,然后用./bin/sql-client.sh -f pipline.txt 启动
SET execution.savepoint.path=/Users/luguo/app/flink-1.13.0/flink-savepoints/savepoint-e1c449-f44bae04c8ef; 

```

算子改动流程
===========
如果算子有改动，如表结构改动需要先

1、使用 Savepoint 取消作业 #
$ bin/flink cancel -s [:targetDirectory] :jobId

2、在pipline.txt指定savepoint.path,然后用
./bin/sql-client.sh -f pipline.txt 启动




