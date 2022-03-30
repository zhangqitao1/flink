\# flink 投递文件

 一：实现按照当前年月日投递文件，并设置滚动策略 

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
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
        .withRollingPolicy(OnCheckpointRollingPolicy.build())
        .withOutputFileConfig(new OutputFileConfig("a",".csv"))  
        .build();  

message.addSink(sink);
env.execute();    
```

结果： 
└── 2022-01-01
├── aa
│ ├── a-4-1.csv
│ └── a-4-2.csv
└── bb
└── a-4-0.csv
