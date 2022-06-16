配置指南
=========
```

https://hudi.apache.org/docs/flink-quick-start-guide/

1、下载hudi，放入flink-1.13.6/lib下
https://repo.maven.apache.org/maven2/org/apache/hudi/hudi-flink-bundle_2.11/0.10.0/hudi-flink-bundle_2.11-0.10.0.jar

2、设置环境变量
export HADOOP_CLASSPATH=`$HADOOP_HOME/bin/hadoop classpath`

3、配置flink
/conf/flink-conf.yaml, add config option taskmanager.numberOfTaskSlots: 4

4、启动flink
bin/start-cluster.sh


```

批处理
======
```
bin/sql-client.sh

CREATE TABLE t1(
   uuid varchar(20),
   name varchar(20),
   age int,
   ts timestamp(3),
   `partition` varchar(20)
)
PARTITIONED BY (`partition`)
WITH (
  'connector' = 'hudi',
  'path' = '/hudi/t11' ,
  'table.type' = 'MERGE_ON_READ',
  'write.tasks' = '1',
  'compaction.tasks' = '1' 
);

insert into t1 values ('id1','Da',23,timestamp '1970-01-01 00:00:01','par1');
insert into t1 values ('id3','Da',23,timestamp '1970-01-01 00:00:01','par2');

set execution.result-mode=tableau; #展示结果的格式
select * from t1;

+----+--------------------------------+--------------------------------+-------------+-------------------------+--------------------------------+
| op |                           uuid |                           name |         age |                      ts |                      partition |
+----+--------------------------------+--------------------------------+-------------+-------------------------+--------------------------------+
| +I |                            id2 |                             Da |          23 | 1970-01-01 00:00:01.000 |                           par2 |
| +I |                            id3 |                             Da |          23 | 1970-01-01 00:00:01.000 |                           par2 |
| +I |                            id1 |                             Da |          27 | 1970-01-01 00:00:01.000 |                           par1 |
+----+--------------------------------+--------------------------------+-------------+-------------------------+--------------------------------+

更新数据
insert into t1 values ('id1','大哥',33,timestamp '1970-01-01 00:00:01','par1');
select * from t1;
+----+--------------------------------+--------------------------------+-------------+-------------------------+--------------------------------+
| op |                           uuid |                           name |         age |                      ts |                      partition |
+----+--------------------------------+--------------------------------+-------------+-------------------------+--------------------------------+
| +I |                            id2 |                             Da |          23 | 1970-01-01 00:00:01.000 |                           par2 |
| +I |                            id3 |                             Da |          23 | 1970-01-01 00:00:01.000 |                           par2 |
| +I |                            id1 |                           大哥 |          33 | 1970-01-01 00:00:01.000 |                           par1 |
+----+--------------------------------+--------------------------------+-------------+-------------------------+--------------------------------+

```


流式查询
=======
```
CREATE TABLE t3(
   uuid varchar(20),
   name varchar(20),
   age int,
   ts timestamp(3),
   `partition` varchar(20)
)
PARTITIONED BY (`partition`)
WITH (
  'connector' = 'hudi',
  'path' = '/hudi/t11' ,
  'table.type' = 'MERGE_ON_READ',
  'read.tasks' = '1',
  'read.streaming.enabled' = 'true',
  'read.streaming.start-commit' = '20210316134557',
  'read.streaming.check-interval' = '4'
);

```

Kafka to hudi
============
```
1、下载kafka包，放入flink-1.13.6/lib下
https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka_2.11/1.13.6/flink-sql-connector-kafka_2.11-1.13.6.jar

CREATE TABLE kafka_test (
  event_id STRING, 
  appid STRING 
) WITH (
  'connector' = 'kafka',
  'topic' = 'test',
  'properties.bootstrap.servers' = 'localhost:9092',
  'properties.group.id' = 'testGroup',
  'scan.startup.mode' = 'latest-offset',
  'format' = 'json', 
  'json.fail-on-missing-field' = 'false',
 'json.ignore-parse-errors' = 'true'
);

Kafka消息
{"event_id":"dadsfadsfasdfsdfasdfasdfdfasdfdfasdf","appid":"tsdfggxxx"}
{"event_id":"2","appid":"2"}


CREATE TABLE hudi_test(
  event_id STRING PRIMARY KEY NOT ENFORCED,
  appid STRING  
)
PARTITIONED BY (`appid`)
WITH (
  'connector' = 'hudi',
  'path' = '/hudi/test' ,
  'table.type' = 'MERGE_ON_READ',
  'write.operation' = 'upsert',
  'hoodie.datasource.write.recordkey.field' ='event_id', 
  'write.tasks' ='1', 
  'compaction.tasks'='1',
  'compaction.async.enabled'='true',
  'compaction.trigger.strategy'='num_commits',
  'compaction.delta_commits' ='1' 
);
insert into hudi_test  select event_id,appid from kafka_test;




CREATE TABLE hudi_test_streaming(
  event_id STRING PRIMARY KEY NOT ENFORCED,
  appid STRING 
)
PARTITIONED BY (`appid`)
WITH (
  'connector' = 'hudi',
  'path' = '/hudi/test10' ,
  'table.type' = 'MERGE_ON_READ',
  'read.tasks' = '1',
  'read.streaming.enabled' = 'true',
  'read.streaming.start-commit' = '20210316134557',
  'read.streaming.check-interval' = '4'
);


select * from hudi_test_streaming
```







