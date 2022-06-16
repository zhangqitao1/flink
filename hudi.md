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

set execution.result-mode=tableau;
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
