# Flink 資料處理程式設計

本節收錄相關文獻，提供 Flink 可用於解決的問題與設計注意事項。

+ [Data Pipelines & ETL](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/etl/)
    - [Operators](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/datastream/operators/overview/)、[Operators 中文](https://nightlies.apache.org/flink/flink-docs-lts/zh/docs/dev/datastream/operators/overview/)、[DataStream API之-转换算子（Transformation）](https://www.cnblogs.com/kunande/p/16395035.html)
    - [Table API & SQL](https://nightlies.apache.org/flink/flink-docs-lts/docs/dev/table/overview/)
+ [Intro to the DataStream API](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/datastream_api/)
+ [Streaming Analytics](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/streaming_analytics/)
+ [Event-driven Applications](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/event_driven/)

## Extract, Transform, Load

Flink 的常見的案例是實踐 ETL（提取、轉換、加載）管道，該管道從一個或多個數據源獲取數據，並執行一系列轉換或演算操作，最終將結果存儲在某個位置。

Flink 的 DataStream API 對於 ETL 操作提供豐富的函數與物件，此外 Flink 的 Table API 和 SQL API 非常適合許多 ETL 案例；在此簡介常見的函數與範例：

### map

+ Demo : [etl-map](../app/maven/etl-map/src/main/java/flink/DataStreamJob.java)
+ JavaScript [Array.prototype.map()](https://developer.mozilla.org/zh-TW/docs/Web/JavaScript/Reference/Global_Objects/Array/map)

Map 常用於處理矩陣或數列，其結構會依序將矩陣內容傳遞給繼承 ```MapFunction<Input-type, output-type>``` 物件的 map 函數執行，在將回傳的新矩陣替換原始的矩陣內容。

### filter

+ Demo : [etl-filter](../app/maven/etl-filter/src/main/java/flink/DataStreamJob.java)
+ JavaScript [Array.prototype.filter()](https://developer.mozilla.org/zh-TW/docs/Web/JavaScript/Reference/Global_Objects/Array/filter)

Map 用於處理矩陣或數列過濾，其結構會依序將矩陣內容傳遞給繼承 ```FilterFunction<Input-type>``` 物件的 filter 函數執行，而輸入值回傳保留與否的布林值，最終會將不保留的數值從矩陣內容移除。

### flatmap

+ Demo : [etl-flatmap](../app/maven/etl-flatmap/src/main/java/flink/DataStreamJob.java)
+ JavaScript [Array.prototype.flatMap()](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/flatMap)

若 Map 是整個矩陣 1 對 1 的映射回應，Flatmap 則是扁平化 ( Flatten ) 與映射 ( Map ) 兩階段操作，其結構會依序將矩陣內容傳遞給繼承 ```FlatMapFunction<Input-type, output-type>``` 物件的 flatMap 函數執行，而輸入值可依照內容計算，並將期望的結果儲存於 Collector，在結束整個矩陣尋訪，最終將 Collector 的內容替換原始的矩陣。

常見的範例是用於字串切割後重新回填矩陣的 WordCount 操作。

### keyby

+ Demo : [etl-keyby](../app/maven/etl-keyby/src/main/java/flink/DataStreamJob.java)、[etl-keyby-tuple](../app/maven/etl-keyby/src/main/java/flink/DataStreamJob.java)
+ 文獻
    - [Flink教程(2) DataStream聚合 keyBy sum min和minBy区别](https://blog.csdn.net/winterking3/article/details/106542352)
    - [Apache Flink — Understanding the working of keyBy() operator in correlation with windowing operator](https://medium.com/@chunilalkukreja/dae9be7610a4)

若 Map、Filter、Flatmap 是映射 ( Map ) 函數，相當於 MapReduce 的 Map，則 Sum、Min、Max 等聚合 ( Aggregation ) 函數，相當於 MapReduce 的 Reduce。

在 Flink 中，DataStream 的聚合函數必需先進行分區，才可進行聚合；而 keyBy 的操作是將鍵 ( key ) 整理至不同的區 ( partitions )，讓相同的鍵的數據會在同一個區內，進而在利用聚合對區內的數據進行計算；就 keyBy 原理來看，是實踐了雜湊表的設計，並用此管理數據分區的邏輯。

使用 keyBy 函數需要輸入一個參數來標示分區的方式，以下為幾種參數輸入方式：

+ Java 類別 ( 或稱 POJO、Plain Old Java Object )，以字串指定類別的屬性
    - POJO 是公開類別 ( public )
    - POJO 的建構函數是公開 ( public ) 且沒有任何輸入參數
    - POJO 類別中的屬性，是非靜態 ( non-static ) 且為公開的變數，或私有變數但有提供 getter、setter 函數
+ Tuple 類別，可以指定位置
    - Flink 提供 Tuple0 到 Tuple25 個類別
    - Tuple 的宣告可以對不同欄位 ( Field ) 指定不同類別，例如 ```Tuple2<String, Integer> person = Tuple2.of("Fred", 35);```
    - Tuple 取值使用 ```f0```、```f1``` 標示欄位0、欄位1
+ Lambda 表示式的鍵選擇器 ( KeySelector )

在 etl-keyby-tuple 範例中，當數據流使用 Tuple 類別，則使用 Lambda 的 ```r -> r.f0``` 與 Tuple 的 ```0``` 位置與 POJO 的 ```"f0"``` 字串是相同結果。

### Aggregation

簡易聚合函數其說明如下：

+ sum：對指定鍵進行加總
+ max：取最大值
+ min：取最小值

聚合函數執行時，並不會改變數列的數量，亦即對分區進行總和，可以看到如下結果：

```
(a,1)
(a,4)
```

雖然，最後一個元素的數值是最終總和，但累加過程可在其中每個元素見到；同理用於最小值，則會因為第一個元素為最小，之後所有元素都被此替代。

若簡易聚合函數不符合使用，可以使用 reduce 聚合函數，建立需要的數據處理過程。

### Window

+ [Windows](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/streaming_analytics/#windows)

流處理雖然處理的資料是無界資料流 ( unbounded streams )，但實際執行時可以是一筆一筆處理，或者一個區間處理，而這區間及稱為時間窗 ( Time Window )，時間窗中包括數個事件 ( Event )；常見的處理議題如下：

+ 每分鐘有多少頁被瀏覽
+ 每周每位用戶有多少對話 ( sessions )
+ 每分鐘每個感測器的最大溫度

這些問題在會針對連串的資料框出符合條件的窗 ( Window )，並根據窗繼續執行資料，其結構如下：

```
stream
    .keyBy(<key selector>)
    .window(<window assigner>)
    .reduce|aggregate|process(<window function>);
```

在 Flink 共提供了數個窗，大體是滾動時間窗 ( Tumbling time windows )、滑動時間窗 ( Sliding time windows ) 等，基本上就是依據不同時間條件將數筆資料匡列，交付後續的算子處理。

但需要注意，其中 process 是指 ProcessWindowFunction 類別，其設計是用來將時間匡列的事件，以迭代物件 ( Iterable ) 儲存，供開發者設計迭代處理程序；對於事件處理的詳細設計規範，參考 [Event-driven Applications](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/event_driven/) 文獻。
