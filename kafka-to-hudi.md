流作业文件
==========
```

kafka_hudi.txt

CREATE TABLE kafka_rawlog (
	event_id string,
	`timestamp` string,
	microtime string,
	ip string,
	appid string,
	server_id string,
	uuid string,
	udid string,
	channel string,
	subchannel string,
	referrer string,
	directed string,
	country string,
	region string,
	city string,
	locale string,
	version string,
	os string,
	browser string,
	screen string,
	event string,
	user_info string,
	area string,
	lang string,
	package string,
	manufacturer string,
	subchannel_ string,
	subchannel__ string,
	uaid string,
	event_params map<string,string>,
	status map<string,string>,
	session_properties map<string,string>
) WITH (
  'connector' = 'kafka',
  'topic' = 'rawlog',
  'properties.bootstrap.servers' = 'localhost:9092',
  'properties.group.id' = 'testGroup',
  'scan.startup.mode' = 'latest-offset',
  'format' = 'json',
  'json.fail-on-missing-field' = 'false',
 'json.ignore-parse-errors' = 'true'
);




CREATE TABLE hudi_rawlog(
  event_id STRING PRIMARY KEY NOT ENFORCED,
	`timestamp` string,
	microtime string,
	ip string,
	appid string,
	server_id string,
	uuid string,
	udid string,
	channel string,
	subchannel string,
	referrer string,
	directed string,
	country string,
	region string,
	city string,
	locale string,
	version string,
	os string,
	browser string,
	screen string,
	event string,
	user_info string,
	area string,
	lang string,
	package string,
	manufacturer string,
	subchannel_ string,
	subchannel__ string,
	uaid string,
	event_params map<string,string>,
	status map<string,string>,
	session_properties map<string,string>
)
PARTITIONED BY (`appid`)
WITH (
  'connector' = 'hudi',
  'path' = '/hudi/rawlog' ,
  'table.type' = 'MERGE_ON_READ',
  'write.operation' = 'upsert',
  'hoodie.datasource.write.recordkey.field' ='event_id',
  'write.tasks' ='1',
  'compaction.tasks'='1',
  'compaction.async.enabled'='true',
  'compaction.trigger.strategy'='num_commits',
  'compaction.delta_commits' ='1'
);
insert into hudi_rawlog  select * from kafka_rawlog;

```


提交作业
========

```
bin/sql-client.sh -f kafka_hudi.txt

```



spark 
=====

```
bin/spark-shell \
>   --packages org.apache.hudi:hudi-spark3.2-bundle_2.12:0.11.0 \
>   --conf 'spark.serializer=org.apache.spark.serializer.KryoSerializer' \
>   --conf 'spark.sql.catalog.spark_catalog=org.apache.spark.sql.hudi.catalog.HoodieCatalog' \
>   --conf 'spark.sql.extensions=org.apache.spark.sql.hudi.HoodieSparkSessionExtension'

spark.read.format("hudi").load("hdfs://127.0.0.1:9000/hudi/rawlog").show()
22/06/16 07:10:12 WARN package: Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
+-------------------+--------------------+--------------------+----------------------+--------------------+--------------------+----------+--------------+-------------+--------------+---------+---------------+----------------+-------+----------+--------+--------+-------+------+----+------+-------+---+-------+------+--------------------+---------+----+----+-------+------------+-----------+------------+----+--------------------+--------------------+------------------+
|_hoodie_commit_time|_hoodie_commit_seqno|  _hoodie_record_key|_hoodie_partition_path|   _hoodie_file_name|            event_id| timestamp|     microtime|           ip|         appid|server_id|           uuid|            udid|channel|subchannel|referrer|directed|country|region|city|locale|version| os|browser|screen|               event|user_info|area|lang|package|manufacturer|subchannel_|subchannel__|uaid|        event_params|              status|session_properties|
+-------------------+--------------------+--------------------+----------------------+--------------------+--------------------+----------+--------------+-------------+--------------+---------+---------------+----------------+-------+----------+--------+--------+-------+------+----+------+-------+---+-------+------+--------------------+---------+----+----+-------+------------+-----------+------------+----+--------------------+--------------------+------------------+
|  20220616070840689|20220616070840689...|fd2441a94a98d34f7...|        1189032567-dev|59abd391-7ad6-4b5...|fd2441a94a98d34f7...|1655323242|1655323242.315|106.38.76.146|1189032567-dev|       11|619265809019044|hahahaha01222223|       |          |        |       0|       |      |    |      |       |   |       |      |s_underground_reward|       []|    |  zh|       |            |           |            |    |{user_game_id -> ...|{rolename -> Gues...|                {}|
|  20220616070840689|20220616070840689...|                   1|        1189032567-dev|59abd391-7ad6-4b5...|                   1|1655323242|1655323242.315|106.38.76.146|1189032567-dev|       11|619265809019044|hahahaha01222223|       |          |        |       0|       |      |    |      |       |   |       |      |s_underground_reward|       []|    |  zh|       |            |           |            |    |{user_game_id -> ...|{rolename -> Gues...|                {}|
|  20220616070846257|20220616070846257...|                   2|        1189032567-dev|59abd391-7ad6-4b5...|                   2|1655323242|1655323242.315|106.38.76.146|1189032567-dev|       11|619265809019044|hahahaha01222223|       |          |        |       0|       |      |    |      |       |   |       |      |s_underground_reward|       []|    |  zh|       |            |           |            |    |{user_game_id -> ...|{rolename -> Gues...|                {}|
+-------------------+--------------------+--------------------+----------------------+--------------------+--------------------+----------+--------------+-------------+--------------+---------+---------------+----------------+-------+----------+--------+--------+-------+------+----+------+-------+---+-------+------+--------------------+---------+----+----+-------+------------+-----------+------------+----+--------------------+--------------------+------------------+

```
