
```
配置指南
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
