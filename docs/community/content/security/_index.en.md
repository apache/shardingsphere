+++
pre = "<b>4. </b>"
title = "Security"
weight = 4
chapter = true
+++

The Apache Software Foundation takes a rigorous stance on eliminating security issues in its software projects. 
Likewise, Apache ShardingSphere is also vigilant and takes security issues related to its features and functionality into the highest consideration.

If you have any concerns regarding ShardingSphere’s security, or you discover a vulnerability or potential threat, please don’t hesitate to get in touch with the [Apache Security Team](http://www.apache.org/security/) by dropping an email at [security@apache.org](mailto:security@apache.org). 

Please specify the project name as "ShardingSphere" and its product name "ShardingSphere-JDBC" or "ShardingSphere-Proxy" in the email, and provide a description of the relevant problem or potential threat.
You are also urged to recommend how to reproduce and replicate the issue. 

The Apache Security Team and the ShardingSphere community will get back to you after assessing and analyzing the findings.

**Please note** that the security issue should be reported on the security email first, before disclosing it on any public domain.

ShardingSphere-JDBC does not directly provide external services, and users need to write code to use it. Due to the complexity and variety of user scenarios, ShardingSphere cannot control how users write code that uses ShardingSphere-JDBC. Therefore, the ShardingSphere community **does not accept any security issue reports that ShardingSphere-JDBC is exploited due to improper use**.
For example, the user introduced MySQL Connector/J with security vulnerabilities in the project, and trusted the externally input JDBC URL as the data source configuration of ShardingSphere-JDBC.

ShardingSphere-Proxy provides external services through database protocol and provides authentication method based on user password. Users need to ensure the security of user passwords by themselves. Therefore, the ShardingSphere community **does not accept any security issue report that presupposes that the attacker knows the ShardingSphere-Proxy user password**.

ShardingSphere cluster mode relies on middleware such as ZooKeeper, and trusts the middleware provided by users. For the security protection of middleware such as ZooKeeper, users need to protect themselves. Therefore, the ShardingSphere community **does not accept security issue reports that ShardingSphere is exploited due to middleware such as ZooKeeper being attacked**.

Third-party dependency security advice:
For dependencies that are not included by default in the ShardingSphere release version, users are required to ensure the security of dependencies.
For example, the release version of ShardingSphere does not include MySQL Connector/J by default, so ShardingSphere does not accept any security issue report that ShardingSphere is exploited due to the vulnerability of MySQL Connector/J itself.
The same is true for dependencies that are not included by default in other ShardingSphere releases.

Security recommendations for the ShardingSphere subproject:
Since ShardingSphere-UI is no longer maintained, the ShardingSphere community **will no longer accept any security issue reports related to ShardingSphere-UI**.

ElasticJob relies on ZooKeeper and trusts the ZooKeeper provided by the user. For the security protection of middleware such as ZooKeeper, users need to protect themselves. Therefore, the ShardingSphere community **does not accept the security issue report of ElasticJob being exploited due to ZooKeeper attack**.

ElasticJob-UI aims to provide users with a convenient job management and control platform. The platform provides services to developers and operation and maintenance personnel, not directly to Internet users. It is recommended that users deploy only on the intranet and avoid leakage of user passwords. The ShardingSphere community **does not accept any security issue reports that presuppose that the attacker knows the user's password**.

Before submitting a security issue report, please refer to the CVEs released by ShardingSphere and its sub-projects in the past to avoid repeated submissions.

ShardingSphere:
[CVE-2020-1947](https://www.cve.org/CVERecord?id=CVE-2020-1947)

ShardingSphere-UI:
[CVE-2021-26558](https://www.cve.org/CVERecord?id=CVE-2021-26558)

ElasticJob-UI
[CVE-2022-22733](https://www.cve.org/CVERecord?id=CVE-2022-22733)
