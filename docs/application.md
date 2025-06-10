# Flink 應用程式開發

+ [Project Configuration](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/configuration/overview/)
+ [DataStream API](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/datastream/overview/)、[DataStream API 中文](https://nightlies.apache.org/flink/flink-docs-lts/zh/docs/dev/datastream/overview/)
    - [Operators](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/datastream/operators/overview/)、[Operators 中文](https://nightlies.apache.org/flink/flink-docs-lts/zh/docs/dev/datastream/operators/overview/)
+ [Table API & SQL](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/table/overview/)
+ [State Processor API](https://nightlies.apache.org/flink/flink-docs-lts/docs/libs/state_processor_api/)
+ [Data Sinks](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/sinks/)
    - [Streaming File Sink]([StreamingFileSink](https://nightlies.apache.org/flink/flink-docs-lts/docs/connectors/datastream/streamfile_sink/))
+ Sample Project
    - [Sample Project using the Java API](https://nightlies.apache.org/flink/flink-docs-release-1.2/quickstart/java_api_quickstart.html)
    - [Flink application example](https://docs.cloudera.com/cdf-datahub/latest/how-to-flink/topics/csa-application-example.html)
    - [Batch Examples](https://nightlies.apache.org/flink/flink-docs-release-1.13/docs/dev/dataset/examples/)

主要內容包括：

## 開發環境

本專案以 Maven 容器啟動開發環境，其相關設定參考 [Dokcerfile](../conf/docker/maven/Dockerfile) 與 [docker-compose.yml](../conf/docker/docker-compose.yml)；使用專案維運介面 ```flink.bat``` 則操作流程如下：

```
# 啟動服務
flink up

# 進入環境
flink into --tag=java-maven
```

開發環境會掛載兩個目錄：

+ 應用程式目錄：```/app```
+ 快取目錄：```/cache```

## 專案設定

```
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.flink \
  -DarchetypeArtifactId=flink-quickstart-java \
  -DarchetypeVersion=1.20.1 \
  -DgroupId=flink \
  -DartifactId=flink-app \
  -Dversion=1.0.0 \
  -DinteractiveMode=false
```

+ 框架
    - Group : org.apache.flink
    - Artifcat : link-quickstart-java
    - Version : 1.20.1
+ 專案
    - Group : flink
    - Name : flink-app
    - Version : 1.0.0

## 編譯與封裝

在 Maven 專案中，執行 ```mvn compile``` 或 ```mvn package``` 會將輸出位置設定在 pom.xml 檔案中的 project.build.directory，倘若要利用命令參數替換位置，則需修改如下設定：

```xml
<project>
    <properties>
        ...
        <outputDirectory>./target</outputDirectory>
    </properties>
    ...
    <build>
      <directory>${outputDirectory}</directory>
      ...
    </build>
</project>
```

依據 Maven 指令操作，使用 ```-D,--define <arg> Define a user property``` 參數，來替換位在 ```<properties> ... </properties>``` 內的標籤內容。

亦即使用 ```-DoutputDirectory=/cache``` 會將本來在 ```<outputDirectory>./target</outputDirectory>``` 替換為 ```/cache``` 字串，而 project.build.directory 標籤內容則使用屬性 outputDirectory 為變數，以此達到預設值 ```./target```，但可用 ```-DoutputDirectory``` 替換輸出目錄。

因此，編譯與封裝分別使用指令如下：

+ 編譯：```mvn compile -DoutputDirectory=/cache/${PWD##*/}```
+ 封裝：```mvn package -DoutputDirectory=/cache/${PWD##*/}```
+ 清除舊檔 + 編譯 + 封裝：```mvn clean compile package -DoutputDirectory=/cache/${PWD##*/}```

最終輸出目錄會是基於兩個變數 outputDirectory 與專案目錄名稱構成的 ```/cache/flink-app```。

## 測試與執行

由於 Flink 應用程式執行需要配合 Flink 函示庫，這原因是 flink-streaming-java 的相依設定不將函示庫引入，這導致實際執行測試必需經由 Flink 運維操作指令，詳細可參考 [Flink 部屬與運維](deploymenta-and-operate.md) 文件。

+ 在專案目錄使用運維指令進入 Flink JobManager 容器
```
flink into --tag=flink-job-mgr
```

+ 執行專案發佈的 .jar 檔案
```
flink run --detached ./app/flink-app/flink-app-1.0.0.jar
```
> ```./app``` 掛載目錄與開發環境的 ```/cache``` 掛載目錄相同，因此能共享封裝的內容

+ 在專案目錄使用運維指令檢查執行內容
```
## 檢查 JobManager 的記錄
flink log --tag=flink-job-mgr
## 檢查 TaskManager 的記錄
flink log --tag=flink-task-mgr
```

由於 Flink 架構運行方式，在 JobManager 可以觀察到工作是否運行與運行狀態，這部分記錄也可以透過 [WebUI 的 Completed Job](http://localhost:8081/#/job/completed) 內的 log 記錄看到。

但在範例程式 flink-app 中，嘗試各種訊息輸出 ```print()```、```writeAsText('/tmp/rs.txt')```、```addSink()``` 其結果都是出現在 TaskManager 容器；因此，若要檢查 Stdout 的 ```print()``` 結果，需要開啟 TaskManager 的記錄來確認。
