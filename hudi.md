配置指南
=========
```

https://hudi.apache.org/docs/flink-quick-start-guide/

1、下载hudi，放入flink-1.13.6/lib下
https://repo.maven.apache.org/maven2/org/apache/hudi/hudi-flink-bundle_2.11/0.10.0/hudi-flink-bundle_2.11-0.10.0.jar

2、设置环境变量
export HADOOP_CLASSPATH=`$HADOOP_HOME/bin/hadoop classpath`

3、配置flink
vim /conf/flink-conf.yaml

jobmanager.rpc.address: localhost
jobmanager.memory.process.size: 1024m
taskmanager.memory.process.size: 2048m
taskmanager.numberOfTaskSlots: 4
classloader.check-leaked-classloader: false
classloader.resolve-order: parent-first
execution.checkpointing.interval: 3000
state.backend: rocksdb
state.checkpoints.dir: hdfs://127.0.0.1:9000/flink/flink-checkpoints
state.savepoints.dir: hdfs://127.0.0.1:9000/flink/flink-savepoints
state.backend.incremental: true

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

2、bin/sql-client.sh
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
  'path' = '/hudi/test' ,
  'table.type' = 'MERGE_ON_READ',
  'read.tasks' = '1',
  'read.streaming.enabled' = 'true',
  'read.streaming.start-commit' = '20210316134557',
  'read.streaming.check-interval' = '4'
);


select * from hudi_test_streaming

```



查看hudi文件
==========
```


hdfs dfs -ls -R /hudi/test

drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.aux
drwxr-xr-x   - root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/.aux/.bootstrap
drwxr-xr-x   - root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/.aux/.bootstrap/.fileids
drwxr-xr-x   - root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/.aux/.bootstrap/.partitions
-rw-r--r--   1 root supergroup       1487 2022-06-16 04:03 /hudi/test/.hoodie/.aux/20220616040336832.compaction.requested
-rw-r--r--   1 root supergroup       1557 2022-06-16 04:04 /hudi/test/.hoodie/.aux/20220616040436264.compaction.requested
-rw-r--r--   1 root supergroup       1495 2022-06-16 04:04 /hudi/test/.hoodie/.aux/20220616040448664.compaction.requested
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp
drwxr-xr-x   - root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/.temp/20220616040336832
drwxr-xr-x   - root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/.temp/20220616040336832/2
-rw-r--r--   1 root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/.temp/20220616040336832/2/e2cb2f8b-fd13-4f67-ae16-404288d2c67c_0-1-0_20220616040336832.parquet.marker.CREATE
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp/20220616040436264
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp/20220616040436264/2
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp/20220616040436264/2/e2cb2f8b-fd13-4f67-ae16-404288d2c67c_0-1-0_20220616040436264.parquet.marker.MERGE
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp/20220616040448664
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp/20220616040448664/tsdfggxxx
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/.temp/20220616040448664/tsdfggxxx/a3144cbb-55f8-46e6-a7cc-d0f963ad6e3e_0-1-0_20220616040448664.parquet.marker.CREATE
-rw-r--r--   1 root supergroup       1741 2022-06-16 04:03 /hudi/test/.hoodie/20220616040307872.deltacommit
-rw-r--r--   1 root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/20220616040307872.deltacommit.inflight
-rw-r--r--   1 root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/20220616040307872.deltacommit.requested
-rw-r--r--   1 root supergroup       1579 2022-06-16 04:03 /hudi/test/.hoodie/20220616040336832.commit
-rw-r--r--   1 root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/20220616040336832.compaction.inflight
-rw-r--r--   1 root supergroup       1487 2022-06-16 04:03 /hudi/test/.hoodie/20220616040336832.compaction.requested
-rw-r--r--   1 root supergroup       1808 2022-06-16 04:04 /hudi/test/.hoodie/20220616040336900.deltacommit
-rw-r--r--   1 root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/20220616040336900.deltacommit.inflight
-rw-r--r--   1 root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/20220616040336900.deltacommit.requested
-rw-r--r--   1 root supergroup       1591 2022-06-16 04:04 /hudi/test/.hoodie/20220616040436264.commit
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/20220616040436264.compaction.inflight
-rw-r--r--   1 root supergroup       1557 2022-06-16 04:04 /hudi/test/.hoodie/20220616040436264.compaction.requested
-rw-r--r--   1 root supergroup       1780 2022-06-16 04:04 /hudi/test/.hoodie/20220616040436328.deltacommit
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/20220616040436328.deltacommit.inflight
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/20220616040436328.deltacommit.requested
-rw-r--r--   1 root supergroup       1618 2022-06-16 04:04 /hudi/test/.hoodie/20220616040448664.commit
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/20220616040448664.compaction.inflight
-rw-r--r--   1 root supergroup       1495 2022-06-16 04:04 /hudi/test/.hoodie/20220616040448664.compaction.requested
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/20220616040449535.deltacommit.inflight
-rw-r--r--   1 root supergroup          0 2022-06-16 04:04 /hudi/test/.hoodie/20220616040449535.deltacommit.requested
drwxr-xr-x   - root supergroup          0 2022-06-16 04:03 /hudi/test/.hoodie/archived
-rw-r--r--   1 root supergroup        540 2022-06-16 04:03 /hudi/test/.hoodie/hoodie.properties
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/2
-rw-r--r--   1 root supergroup        720 2022-06-16 04:03 /hudi/test/2/.e2cb2f8b-fd13-4f67-ae16-404288d2c67c_20220616040307872.log.1_0-1-0
-rw-r--r--   1 root supergroup        720 2022-06-16 04:04 /hudi/test/2/.e2cb2f8b-fd13-4f67-ae16-404288d2c67c_20220616040336832.log.1_0-1-0
-rw-r--r--   1 root supergroup         96 2022-06-16 04:03 /hudi/test/2/.hoodie_partition_metadata
-rw-r--r--   1 root supergroup     434250 2022-06-16 04:03 /hudi/test/2/e2cb2f8b-fd13-4f67-ae16-404288d2c67c_0-1-0_20220616040336832.parquet
-rw-r--r--   1 root supergroup     434250 2022-06-16 04:04 /hudi/test/2/e2cb2f8b-fd13-4f67-ae16-404288d2c67c_0-1-0_20220616040436264.parquet
drwxr-xr-x   - root supergroup          0 2022-06-16 04:04 /hudi/test/tsdfggxxx
-rw-r--r--   1 root supergroup        806 2022-06-16 04:04 /hudi/test/tsdfggxxx/.a3144cbb-55f8-46e6-a7cc-d0f963ad6e3e_20220616040436328.log.1_0-1-0
-rw-r--r--   1 root supergroup         96 2022-06-16 04:04 /hudi/test/tsdfggxxx/.hoodie_partition_metadata
-rw-r--r--   1 root supergroup     434886 2022-06-16 04:04 /hudi/test/tsdfggxxx/a3144cbb-55f8-46e6-a7cc-d0f963ad6e3e_0-1-0_20220616040448664.parquet
```







