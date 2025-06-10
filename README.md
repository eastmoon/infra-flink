# infra-flink
Tutorial and learning report with infrastructure open source software Apache Flink.

## 專案操作

+ 啟動

使用 CLI 呼叫 docker-compose 來啟動相關服務

```
flink up
```

+ 關閉

使用 CLI 呼叫 docker-compose 來關閉相關服務

```
flink down
```

+ 進入

使用 CLI 進入目標容器內來操作相關服務的命令

```
flink into --tag=[service-name]
```

## 功能與設計

### 概念

詳細文獻整理參閱 [Flink 框架概念](./docs/concept.md)

### 架構與部署

詳細文獻整理參閱 [Flink 架構與部屬](./docs/architecture-and-deployment.md)

詳細文獻整理參閱 [Flink 部屬與運維](./docs/deploymenta-and-operate.md)

### 應用設計

詳細文獻整理參閱 [Flink 應用程式開發](./docs/application.md)

### 安全性

## Apache Airflow vs Apache Flink

+ [Apache Airflow vs Apache Flink](https://medium.com/@tonmoysaklain/c0f24f596130)

Apache Airflow primarily focuses on task scheduling and workflow management, is well-suited for workflow management, ETL, and data pipeline orchestration.

Apache Flink is a powerful data processing system with real-time streaming capabilities. Apache Flink offers scalability, a stream processing model, strong fault tolerance, built-in state management, and is often used for real-time data-intensive applications.

## 文獻

+ [Flink](https://zh.wikipedia.org/zh-tw/Apache_Flink)
    - [Data Pipelines & ETL](https://nightlies.apache.org/flink/flink-docs-lts/docs/learn-flink/etl/)
+ Introduction
    - [Apache Flink - handwiki](https://handwiki.org/wiki/Software:Apache_Flink)
+ Design
    - [Stream processing with Apache Flink and MinIO](https://blog.min.io/stream-processing-with-apache-flink-and-minio/)
    - [Apache Kafka Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/datastream/kafka/)
+ Demo
  - [PyFlink — A deep dive into Flink’s Python API](https://quix.io/blog/pyflink-deep-dive)
  - [flink-python-examples](https://github.com/wdm0006/flink-python-examples)
  - [Getting started with Apache Flink: A guide to stream processing](https://m.mage.ai/getting-started-with-apache-flink-a-guide-to-stream-processing-70a785e4bcea)
