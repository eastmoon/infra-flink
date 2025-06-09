# Flink 架構與容器部屬

+ [flink - Docker](https://hub.docker.com/_/flink)
+ [Docker Setup](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/resource-providers/standalone/docker/)
    - [Configuring Flink on Docker - Via Environment Variables](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/resource-providers/standalone/docker/#via-environment-variables)
    - [Flink Configuration File](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/config/#flink-configuration-file)
+ [Flink Architecture](https://nightlies.apache.org/flink/flink-docs-lts/docs/concepts/flink-architecture/)

## Flink Cluster

![](./img/flink-architecture)
> from [Flink Architecture](https://nightlies.apache.org/flink/flink-docs-lts/docs/concepts/flink-architecture/)

Flink 在執行階段由兩種類型的程序組成，一個 JobManager 和至少一個 TaskManager。

Flink 客戶端不屬於 Flink 執行階段程序的一部分，而是用於準備資料流並將其傳送至 JobManager；因此，Flink 客戶端可以是斷開連線的分離模式，或保持連線以接收進度報告的連線模式。

#### JobManager

JobManager 主要職責是協調與分配 Flink 應用程式的分散式執行狀態，它決定何時安排下一個任務或一組任務、對已完成的任務或執行失敗做出反應、協調檢查點、協調故障復原等。

JobManager 處理過程由三個不同的部分組成：

+ 資源管理器 ( ResourceManager )
ResourceManager 負責 Flink 叢集中的資源的釋放 ( deallocation )、分配 ( allocation )、供應 ( provisioning )，它會管理 TaskManagers 中的任務槽 (task slots)，這些任務槽是 Flink 叢集中資源調度的單位；在獨立部署中，ResourceManager 只能指派可用 TaskManager 的插槽，而無法自行啟動新的 TaskManager。

+ 調度員 ( Dispatcher )
Dispatcher 提供了一個 REST 介面來提交 Flink 應用程式以供執行，並為每個提交的作業啟動一個新的 JobMaster；它也為 Flink WebUI 提供有關作業執行的資訊。

+ 工作主管 ( JobMaster )
JobMaster 負責管理單一 JobGraph 的執行，Flink 叢集中可以同時執行多個工作，每個工作都有自己的 JobMaster。

在 Flink 系統架構中，必需有一個 JobManager；在高可用性可設定多個 JobManager，但其中一個為 Leader，其他 JobManager 處於待命狀態。

#### TaskManagers

TaskManager ( 也稱為 Worker ) 主要職責是執行資料流的任務，並處理資料流的緩衝和匯流。

在 Flink 系統架構中，必需至少有一個 TaskManager；TaskManager 中資源調度的最小單位是任務槽 ( Task Slot )，任務槽的數量表示 TaskManager 可併行處理任務的數量。

#### [Tasks and Operator Chains](https://nightlies.apache.org/flink/flink-docs-lts/docs/concepts/flink-architecture/#tasks-and-operator-chains)

對於分散式運算的執行過程，Flink 將算子子任務 ( operator subtasks ) 鏈在一起形成任務，並由一個執行緒執行。

在 Flink 中，將算子連結在一起形成任務是一項有效的最佳化，它可以減少執行緒間切換和緩衝的開銷，並在降低延遲的同時提高整體吞吐量。

算子鏈行為可以透過程式配置，詳情請參閱文件 [chaining](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/dev/datastream/operators/overview/#task-chaining-and-resource-groups)。

#### [Task Slots and Resources](https://nightlies.apache.org/flink/flink-docs-lts/docs/concepts/flink-architecture/#task-slots-and-resources)

本節描述 Flink 的任務槽 ( Task Slot ) 的運作原則與其資源分配方式，詳細參考上述連結。

## Flink Docker image

Flink 映像檔包含一個常規的 Flink ，並可用其啟動下列服務：

+ Session 叢集的 JobManager
+ Application 叢集的 JobManager
+ 任何叢集的 TaskManager

Flink 映像檔的配置設定，主要經由環境變數 ```FLINK_PROPERTIES``` 帶入，詳細可配置參數參考 [Flink Configuration File](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/config/#flink-configuration-file)，其句型結構如下：

```
# 宣告環境變數內容
export FLINK_PROPERTIES="jobmanager.rpc.address: host
taskmanager.numberOfTaskSlots: 3
blob.server.port: 6124
"
# 啟動容器
docker run --env FLINK_PROPERTIES=${FLINK_PROPERTIES} flink:1.20.1-scala_2.12 <jobmanager|standalone-job|taskmanager>
```

## Session 叢集

一個 Flink Session 叢集可以運行多個作業。叢集部署完成後，每個工作都需要提交到叢集中。

+ 叢集生命週期 ( Cluster Lifecycle )
在 Flink Session 叢集中，客戶端連接到預先存在 ( pre-existing )、長期運行 ( long-running ) 的叢集，該叢集可以接受多個工作提交；即使所有工作都完成，叢集 ( 以及 JobManager ) 仍將持續運行，直到 Seesion 手動停止；因此，Flink Session 叢集叢集的生命週期不受任何 Flink 工作生命週期的綁定。

+ 資源隔離 ( Resource Isolation )
TaskManager 的 slot 由 JobManager 的 ResourceManager 進行工作提交時分配，並在作業完成後釋放。由於所有工作共享同一個集群，因此在提交工作階段存在一些叢集資源競爭，例如網路頻寬。這種共用設定的一個限制是，如果一個 TaskManager 崩潰，那麼所有在該 TaskManager 上執行任務的工作都會失敗；同樣，如果 JobManager 發生致命錯誤，它將影響叢集中正在執行的所有作業。

+ 其他注意事項 ( Other considerations )：
擁有預先存在的叢集可以節省大量申請資源和啟動 TaskManager 的時間；對於業執行時間極短但系統啟動時間極長時，對使用者體驗會產生負面影響，因此預先啟動系統並常駐執行是非常重要，這就如同利用短查詢進行互動式分析的情況一樣，希望作業能夠使用現有資源快速執行計算。

基於容器的 Session 叢集架構部屬設定，參考 [docker-compose-session](../conf/docker/docker-compose-session.yml)

## Application 叢集

Flink 應用程式叢集是一個專用的集群，只執行單一工作；在這種情況下，您只需一步即可部署叢集並執行工作，且無需額外提交工作。

+ 叢集生命週期 ( Cluster Lifecycle )
Flink 應用程式叢集是一個專用的 Flink 叢集，它僅執行來自單一 Flink 應用程式的工作，並且 ```main()``` 方法在叢集上運行，而不是在客戶端上運行。工作提交只需一步，且無需先啟動 Flink 叢集或等待工作提交到現有的叢集；此外，您可以將應用程式邏輯和依賴庫封裝成可執行工作的 JAR 文件，叢集入口點 ( ApplicationClusterEntryPoint ) 負責呼叫 ```main()``` 方法來提取作業圖 ( JobGraph )。這允許您像在 Kubernetes 上部署任何其他應用程式一樣部署 Flink 應用程式；因此，Flink 應用程式叢集的生命週期與 Flink 應用程式的生命週期綁定。

+ 資源隔離 ( Resource Isolation )
在 Flink 應用程式叢集中，ResourceManager 和 Dispatcher 的作用域為單一 Flink 應用程式，這比 Flink Session 叢集提供了更好的關注點分離。

基於容器的 Application 叢集架構部屬設定，參考 [docker-compose-application](../conf/docker/docker-compose-application.yml)，其範本會需要運用 [JobManager 參數](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/resource-providers/standalone/docker/#jobmanager-additional-command-line-arguments)：

+ ```--job-classname <job class name>```：工作需要執行的類別名稱 ( Class name )
預設情況下，Flink 會掃描其類別路徑，尋找包含 Main-Class 或 program-class 清單條目的 JAR 檔案，並選擇其作為作業類別，使用此參數可以手動設定工作類別；但倘若類別路徑中沒有或有多個包含此類清單條目的 JAR 檔案，則需要強制使用此參數。

+ ```--job-id <job id>```：手動為作業設定 Flink 工作 ID ( 預設值為 00000000000000000000000000000000 )

+ ```--fromSavepoint /path/to/savepoint```：自保存點恢復
若要從保存點恢復狀態，需要提供保存點儲存路徑；請注意，```/path/to/savepoint``` 需要在叢集的所有 Docker 容器中均可存取，例如將其儲存在 DFS 上或從已掛載的磁碟區。

+ ```--allowNonRestoredState```：跳過損壞的保存點狀態
指定此參數以允許跳過無法復原的保存點狀態。

+ ```--jars```：工作 jar 檔案的路徑，並以逗號分隔的多個檔案
您可以指定此參數來指向儲存在 Flink 檔案系統或透過 HTTP(S) 下載的工作檔案；Flink 將在工作部署期間取得這些檔案；其設定格式如 ```--jars s3://my-bucket/my-flink-job.jar, --jars s3://my-bucket/my-flink-job.jar, s3://my-bucket/my-flink-udf.jar```。
