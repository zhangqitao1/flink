\# flink 投递文件

 一：实现按照当前年月日投递文件，并设置滚动策略 

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.enableCheckpointing(TimeUnit.SECONDS.toMillis(10));

Properties properties = new Properties();
properties.setProperty("bootstrap.servers", "hadoop1:9092");
properties.setProperty("group.id", "aa");

DataStreamSource<String> message = env.addSource(
        new FlinkKafkaConsumer<>("rawlog", new SimpleStringSchema(), properties)
);
final DefaultRollingPolicy<Object, Object> rolling = DefaultRollingPolicy.builder()
                .withRolloverInterval(TimeUnit.MINUTES.toMillis(5))
                .withInactivityInterval(TimeUnit.MINUTES.toMillis(5))
                .withMaxPartSize(1024 * 5)
                .build();

final StreamingFileSink sink = StreamingFileSink
        .forRowFormat(new Path("./outputPath"), new SimpleStringEncoder<String>("UTF-8"))
        /**
         * 设置桶分配政策
         * DateTimeBucketAssigner --默认的桶分配政策，默认基于时间的分配器，每小时产生一个桶，格式如下yyyy-MM-dd--HH
         * BasePathBucketAssigner ：将所有部分文件（part file）存储在基本路径中的分配器（单个全局桶）
         */
        .withBucketAssigner(new DateTimeBucketAssigner())

        /**
         * 有三种滚动政策
         *  CheckpointRollingPolicy
         *  DefaultRollingPolicy
         *  OnCheckpointRollingPolicy 

         * 滚动策略决定了写出文件的状态变化过程
         * 1. In-progress ：当前文件正在写入中
         * 2. Pending ：当处于 In-progress 状态的文件关闭（closed）了，就变为 Pending 状态
         * 3. Finished ：在成功的 Checkpoint 后，Pending 状态将变为 Finished 状态
         *
         * 观察到的现象
         * 1.会根据本地时间和时区，先创建桶目录
         * 2.文件名称规则：part-<subtaskIndex>-<partFileIndex>
         * 3.在macos中默认不显示隐藏文件，需要显示隐藏文件才能看到处于In-progress和Pending状态的文件，因为文件是按照.开头命名的
         *
         */
        .withRollingPolicy(rolling)
        .build();  

message.addSink(sink);
env.execute();  
```

二：根据内容中的字段自定义路径投递文件，并设置滚动策略 投递内容：

 {"event":"aa","date": "2022-01-01","name":"132456"}   

{"event":"aa","date": "2022-01-01","name":"132456"}  

 {"event":"bb","date": "2022-01-01","name":"132456"} 

 自定义路径类  

```java
class CostomBucketAssigner extends DateTimeBucketAssigner {

    private JSONObject json;

    @Override
    public String getBucketId(Object element, Context context) {

        try {
            json = JSON.parseObject(element.toString());
        } catch (JSONException e) {
            return super.getBucketId(element,context);
        }

        System.out.println(element);
        return json.getString("date") + "/" + json.getString("event");
    }
}


StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.enableCheckpointing(60000);

Properties properties = new Properties();
properties.setProperty("bootstrap.servers", "hadoop1:9092");
properties.setProperty("group.id", "aa");

DataStreamSource<String> message = env.addSource(
        new FlinkKafkaConsumer<>("rawlog", new SimpleStringSchema(), properties)
);
final DefaultRollingPolicy<Object, Object> rolling = DefaultRollingPolicy.builder()
                .withRolloverInterval(TimeUnit.MINUTES.toMillis(5))
                .withInactivityInterval(TimeUnit.MINUTES.toMillis(5))
                .withMaxPartSize(1024 * 5)
                .build();

final StreamingFileSink sink = StreamingFileSink
        .forRowFormat(new Path("./outputPath"), new SimpleStringEncoder<String>("UTF-8"))
        .withBucketAssigner(new CostomBucketAssigner())  
        .withRollingPolicy(rolling)
        .withOutputFileConfig(new OutputFileConfig("a",".csv"))  
        .build();  

message.addSink(sink);
env.execute();    
```

结果： 
```java
└── 2022-01-01
    ├── aa
    │   ├── a-4-1.csv
    │   └── a-4-2.csv
    └── bb
        └── a-4-0.csv
```
