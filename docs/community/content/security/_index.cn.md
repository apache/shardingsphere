+++
pre = "<b>4. </b>"
title = "安全"
weight = 4
chapter = true
+++

Apache Software Foundation 在消除其软件项目中的安全问题方面采取了严格的立场。Apache ShardingSphere 也十分关注与其特性和功能相关的安全问题。

如果您对 ShardingSphere 的安全性感到担忧，或者您发现了漏洞或潜在的威胁，请不要犹豫与 [Apache 安全团队](http://www.apache.org/security/) 联系，发送邮件至 [security@apache.org](mailto:security@apache.org)。
在邮件中请指明项目名称为 ShardingSphere 和其旗下产品名称 ShardingSphere-JDBC 或 ShardingSphere-Proxy，并提供相关问题或潜在威胁的描述。同时推荐重现和复制安全问题的方法。在评估和分析调查结果后，Apache 安全团队和 ShardingSphere 社区将直接与您回复。

**请注意** 在提交安全邮件之前，请勿在公共领域披露安全电子邮件报告的安全问题。

ShardingSphere-JDBC 并不直接对外提供服务，需要用户编写代码才能够使用。由于用户场景复杂多样，ShardingSphere 无法控制用户如何编写使用 ShardingSphere-JDBC 的代码。因此，ShardingSphere 社区**不接受任何因使用不当导致 ShardingSphere-JDBC 被利用的安全问题报告**。
例如，用户在项目中引入了存在安全漏洞的 MySQL Connector/J，并信任了外部输入的 JDBC URL 作为 ShardingSphere-JDBC 的数据源配置。

ShardingSphere-Proxy 以数据库协议对外提供服务，并提供了基于用户密码的认证方式。用户需要自行保证用户密码安全。因此，ShardingSphere 社区**不接受任何以攻击者已知 ShardingSphere-Proxy 用户密码为前提的安全问题报告**。

ShardingSphere 集群模式依赖 ZooKeeper 等中间件，且信任用户提供的中间件。对于 ZooKeeper 等中间件的安全防护，用户需要自行保障。因此，ShardingSphere 社区**不接受 ZooKeeper 等中间件被攻击导致 ShardingSphere 被利用的安全问题报告**。

第三方依赖安全建议：
对于 ShardingSphere 发布版本默认不包含的依赖，需要用户自行保证依赖的安全性。
例如：ShardingSphere 发布版本默认不包含 MySQL Connector/J，因此 ShardingSphere 不接受任何由于 MySQL Connector/J 本身漏洞导致 ShardingSphere 被利用的安全问题报告。
对于其他 ShardingSphere 发布版本默认不包含的依赖同理。

对于 ShardingSphere 子项目的安全建议：
由于 ShardingSphere-UI 已不再维护，ShardingSphere 社区将**不再接受任何与 ShardingSphere-UI 相关的安全问题报告**。

ElasticJob 依赖 ZooKeeper，且信任用户提供的 ZooKeeper。对于 ZooKeeper 等中间件的安全防护，用户需要自行保障。因此，ShardingSphere 社区**不接受 ZooKeeper 被攻击导致 ElasticJob 被利用的安全问题报告**。

ElasticJob-UI 旨在为用户提供一个便捷的作业管控平台。该平台向开发、运维人员提供服务，并非直接为互联网用户提供服务，建议用户仅在内网部署，并避免用户密码泄漏。ShardingSphere 社区**不接受任何以攻击者已知用户密码为前提的安全问题报告**。

在提交安全问题报告之前，请参考 ShardingSphere 及子项目过去已发布的 CVE，避免重复提交。

ShardingSphere:
[CVE-2020-1947](https://www.cve.org/CVERecord?id=CVE-2020-1947)

ShardingSphere-UI:
[CVE-2021-26558](https://www.cve.org/CVERecord?id=CVE-2021-26558)

ElasticJob-UI
[CVE-2022-22733](https://www.cve.org/CVERecord?id=CVE-2022-22733)
[CVE-2022-31764](https://www.cve.org/CVERecord?id=CVE-2022-31764)
