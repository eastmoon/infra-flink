# Flink 安全性

+ [SSL Setup](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/security/security-ssl/)
本文講解 Flink 程序間網路通訊如何利用 TLS/SSL 進行加密通訊，其主要用於跨主機間的 Flink 叢集架構

+ [Kerberos Authentication Setup and Configuration](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/security/security-kerberos/)、[中文](https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/deployment/security/security-kerberos/)
本文講解如何利用 [Kerberos](https://zh.wikipedia.org/zh-tw/Kerberos) 設定 Flink 的驗證操作

+ [Delegation Tokens](https://nightlies.apache.org/flink/flink-docs-lts/docs/deployment/security/security-delegation-token/)
本文講解如何利用委託權杖 ( Delegation Tokens ) 設定 Flink 的驗證操作

從安全性相關文章可以發現，Apache flink 並沒有限制誰來操作，而是限制自身怎麼對外部的操作保持安全性，嚴格來說 Flink 更像是個流處理框架 ( Stream processing framework )。
