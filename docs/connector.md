# Flink 連結器

+ [DataStream Connectors](https://nightlies.apache.org/flink/flink-docs-lts/docs/connectors/datastream/overview/)
    - [FileSystem Connector (source/sink)](https://nightlies.apache.org/flink/flink-docs-lts/docs/connectors/datastream/filesystem/)、[中文](https://nightlies.apache.org/flink/flink-docs-lts/zh/docs/connectors/datastream/filesystem/)
        + [Text files format](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/connectors/datastream/formats/text_files/)
            - [Flink中FileSink的使用](https://blog.csdn.net/AnameJL/article/details/131416376)
            - [深入理解Flink的FileSink 组件：实时流数据持久化与批量写入](https://cloud.tencent.com/developer/article/2393700)
        + [Amazon S3](https://nightlies.apache.org/flink/flink-docs-lts/zh/docs/deployment/filesystems/s3/)
        + [Stream processing with Apache Flink and MinIO](https://blog.min.io/stream-processing-with-apache-flink-and-minio/)
    - [Apache Kafka Connector (source/sink)](https://nightlies.apache.org/flink/flink-docs-lts/docs/connectors/datastream/kafka/)
    - [Prometheus Sink](https://nightlies.apache.org/flink/flink-docs-lts/docs/connectors/datastream/prometheus/)
    - [Asynchronous I/O for External Data Access](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/datastream/operators/asyncio/)
        + [Exploring the New HTTP Client in Java](https://www.baeldung.com/java-9-http-client)
        + [OkHttp](https://square.github.io/okhttp/)
    - [Table & SQL Connectors](https://nightlies.apache.org/flink/flink-docs-lts/docs/connectors/table/overview/)
        + [User-defined Sources & Sinks](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/table/sourcessinks/)
    - Reference
        + [Flink Data Sources](https://developer.confluent.io/courses/flink-java/data-sources/)
+ [Flink : 1.20-SNAPSHOT API - Java DOC](https://nightlies.apache.org/flink/flink-docs-lts/api/java/index.html?org/apache/flink/connector/file)
+ Custome source and sink
    - [Custom sources and sinks with Flink](https://medium.com/smart-ad-monetization-platform/ae00e8502b0)
    - [Class RichSourceFunction<OUT>](https://nightlies.apache.org/flink/flink-docs-lts/api/java/index.html?org/apache/flink/streaming/api/functions/source/RichSourceFunction.html)
        + [Flink的DataSource三部曲之三:自定义](https://blog.csdn.net/boling_cavalry/article/details/105472218)
    - [Class RichSinkFunction<IN>](https://nightlies.apache.org/flink/flink-docs-release-1.6/api/java/index.html?org/apache/flink/streaming/api/functions/sink/RichSinkFunction.html)
        + [Flink学习10---DataStream之Sink简介及RichSinkFunction](https://blog.csdn.net/zhuzuwei/article/details/107142494)

在 Flink 系統中內建基本資料源和資料池，其中[預先定義的資料源 ( predefined data sources )](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/datastream/overview/#data-sources)包括從檔案、目錄、 Socket 通訊、集合 ( collections ) 與迭代 ( Iterator ) 物件中取得資料，，而[預先定義的資料池 ( predefined data sinks )](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/datastream/overview/#data-sinks)則支援寫入檔案、stdout 和 stderr 以及 Socket 通訊。

但若要銜接特定第三方系統，則可使用第三方提供的連結器 ( Connector )，並利用其提供的資料源 ( Data Source ) 與資料池 ( Data Sink ) 類別存取第三系統；其中詳細引用物件需參考上述連結的函示庫介紹，或相關第三方的文獻。

在最壞情況下，第三方若未提供函示庫，則需要參考上述文獻，建立自定義的資料源 ( Data Source ) 與資料池 ( Data Sink )，並利用 Java 的非同步操作對外部系統的 REST API 存取與執行。

## 檔案連結器範本

+ Demo : [connector-filesystem](../app/maven/connector-filesystem/src/main/java/flink/DataStreamJob.java)

執行範本，請參考 [Flink 應用程式開發](./application.md)。

#### Step 1：編譯項目

於本專案根目錄，使用專案的運維指令執行如下操作：

```
# 啟動服務
flink up

# 進入環境
flink into --tag=java-maven

# 進入專案目錄
cd connector-filesystem

# 編譯專案
mvn clean compile package -DoutputDirectory=/cache/${PWD##*/}
```

編譯完成的檔案會儲存在 ```/cache``` 目錄中，而該目錄在 Flink JobManager 會掛載在 ```/opt/flink/app``` 目錄下。

#### Step 2：執行項目

於本專案根目錄，使用專案的運維指令執行如下操作：

```
# 啟動服務
flink up

# 進入環境
flink into --tag=flink-job-mgr

# 建立緩存目錄
mkdir /cache/input
mkdir /cache/output

# 執行項目
flink run --detached ./app/connector-filesystem/connector-filesystem-1.0.0.jar
```

資料緩存目錄請於執行前建立，此目錄會與 Flink TaskManager 共享，以確保執行項目時用的目錄一致。

#### Step 3：產生資料

使用 Linux 指令，隨機產生 20 個字符，其中包括數字、大小寫英文字母，並依據當前時間戳記為檔名儲存在 ```/cache/input``` 目錄。

```
echo $(tr -dc '0-9a-zA-Z' </dev/urandom | head -c 20) > /cache/input/$(date +%s)
```

本項目的操作會將檔案中的數字替換為 ```_``` 符號

#### Step 4：檢查 Stdout 輸出

於本專案根目錄，使用專案的運維指令執行如下操作：

```
# 啟動服務
flink up

# 進入環境
flink log --tag=flink-task-mgr
```

進入 TaskManager 容器的記錄畫面後，可以觀察到類似如下的記錄：

```
...
2025-06-15 11:00:40,309 INFO  org.apache.flink.connector.base.source.reader.SourceReaderBase [] - Adding split(s) to reader: [FileSourceSplit: file:/cache/input/1749985239 [0, 21) (no host info) ID=0000005264 position=null]
2025-06-15 11:00:40,310 INFO  org.apache.flink.connector.base.source.reader.fetcher.SplitFetcher [] - Starting split fetcher 7
Z_bRgv_GBrrAxg__wmUx
2025-06-15 11:00:40,343 INFO  org.apache.flink.connector.base.source.reader.fetcher.SplitFetcher [] - Finished reading from splits [0000005264]
...
```

這意思是在 2025-06-15 11:00:40 時讀取 Step 3 產生的檔案 ```/cache/input/1749985239```，由 FileSource 監控並載入後將內容解析成 ```Z_bRgv_GBrrAxg__wmUx```。

#### Step 5：檢查檔案輸出

本項目使用 FileSink 將 Step 4 轉換的內容依據日期定期寫入檔案。

可於 Step 2 的容器環境中，使用指令觀察輸出的目錄：

```
ls -R /cache/output/**/.Flink*
```

或使用指令觀察最後一個檔案的內容：

```
cat $(ls -R /cache/output/**/.Flink* | tail -n 1)
```

若正常執行，可以看到 Step 4 的轉換內容出現在此。

## 小結

Flink 連結器設計，由於依賴第三方的文件完整度或相關網路範本，實務上需要注意諸多細節，必要時請參考 Flink 的 Java 文件來確保使用的函示庫操作正確。

此外，不同於 [Flink 資料處理程式設計](./data-processing,md) 的範本都執行一次，此項目範本會常住在工作清單中，進入 [Flink WebUI](http://localhost:8081) 也能看到此工作持續在運行。
