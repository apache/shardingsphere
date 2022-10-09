+++
title = "E-Hualu X ShardingSphere | Hulu Story’s Data Processing Shortcut"
weight = 21 
chapter = true
+++

> “ShardingSphere has greatly simplified sharding development and maintenance, which has played a very important role in our rapid product release. We estimate that ShardingSphere has saved us at least 4 months of R&D costs.”                                                
*— Shi Moxuan, Technical Director, E-Hualu*

At the beginning of this year, E-hualu officially launched its cloud service product “Hulu App” that targets non-business users. Since then, its architecture’s data stress has increased on a daily basis.

The R&D team of Hulu App chose to use ShardingSphere’s sharding features and scale their data out. The flexible and agile features of ShardingSphere met the requirements of Hulu App. It requires data extensibility, and needs to prevent the team from “reinventing the wheel”.

ShardingSphere can simplify sharding development and maintenance to the greatest extent. As the service is growing, the work will be increasingly complicated.

## Hulu App’s growth pressure: feature extension and new services

The high requirement for data storage and computing is in the genes of the Hulu App technical team.

Because Hulu App is rapidly growing, its service and feature changes are relatively frequent. Therefore its technical team needs to adapt quickly when a front-end service changes. As its users and service data volume are growing rapidly, its databases are faced with greater pressure.

The official Hulu App was released on May 17, 2020, and then its user data and service volume also showed a rapid growth trend. Therefore it was inevitable to scale out its databases for many times. At the same time, with business requirements changing very quickly, new challenges have emerged:

* *How to ensure feature extensibility*

Because of increasing users and developing product versions, its user data has exploded. The storage and computing capability of its old architecture has been severely challenged. Therefore, Hulu App requires that its back-end data processing platform shall have both extensible features, and flexibility.

* *How to improve efficiency*

Faced with its changeable services, the R&D team of Hulu App needs to make rapid adjustments when necessary, so as to make its architecture adapt to its services. A highly-flexible, extensible data architecture can greatly improve development efficiency of the team. On the other hand, because of such large amounts of data, database retrieval efficiency will inevitably have problems such as delays, slow read and write. These problems will ultimately affect user experience.

* *How to facilitate feature release*

All new products will face problems like frequent new feature release, bringing the scheduled release date forward, when they are just released. These problems pose a great challenge to a development team. At first, Hulu’s development team planned to base on their service demands and create their own sharding plan all by themselves. However, they don’t have enough time to do that, so it’s urgent for them to choose a new sharding plan.

* *How to guarantee system stability*
    
A newly-added technology will greatly challenge system stability. It’s especially true for bottom-level technologies, most of which can influence platform services and probably challenge system stability. So what Hulu App needs is a data application product that does not greatly stress databases, but quickly adapts to them and that is also highly stable.

## Leverage ShardingSphere’s features to Create a Flexible, Highly-Available Data Architecture Solution

Hulu App’s team studied their needs and spent two weeks evaluating ShardingSphere and other similar solutions. They had evaluative indicators like product feature, maturity, stability and performance. At last, they chose ShardingSphere because of its powerful features, its support and its high maturity can fully meet their needs.

The Hulu team deployed ShardingSphere above Alibaba Cloud’s RDS. Instead of adjusting ontology databases, the Hulu team prefers to conduct data governance above its databases. ShardingSphere can effectively improve Hulu’s data architecture, because it can achieve three things: high scalability brought by its extensible architecture, closely adaptive to Hulu’s service. ShardingSphere brings Hulu with obvious improvement:
   
***“High scalability” of the pluggable architecture***

Due to its service characteristics, Hulu App’s limited storage space is consumed very quickly, and the problem gradually begins to affect response efficiency at the front end. Therefore, the team can quickly develop enhanced features by using ShardingSphere, which can provide optimized solutions for its subsequent architectural adjustments. The example strengthens the ShardingSphere’s advantage in sharding.

***Closely adaptive to service***

The smaller the architecture change is, the more controllable it is for developers. ShardingSphere, an ecosystem located above databases, is closer to service and it deployment is even more lightweight. So it is undoubtedly the best solution to solve the contradiction between the front-end service changes and back-end architecture adjustment of Hulu App. In addition, in terms of flexibility, ShardingSphere supports instant achievement of relevant configurations, which greatly helps the team save a lot of sharding-related work. So it plays a very important role in Hulu App’s quick releases.

***“Zero attack” to the service architecture***

Hulu App chose ShardingSphere’s Proxy deployment mode. It can manage the real database cluster through Proxy without. replacing underlying databases. It basically completed the separation of service and data at the architectural level without any changes. So it can avoid the risks caused by database changes, such as unavailable service and long stable cycle. In addition, the non-status mode of ShardingSphere hardly has any perceptible impact on users at the front end and the service layer does not need to pay attention to the data storage method .

So, Hulu App is a product with tight go-live schedule and fast feature iteration. ShardingSphere-Proxy can reuse original databases and help the Hulu development team to develop incremental features above databases such as sharding and data encryption. In addition, ShardingSphere allows developers not to worry about database configuration and shields user perception, so it quickly builds service-oriented database direct connection, which is well separated from the system architecture level, and ensures that daily maintenance work at the database agent, such as bug repair and version update, will not affect the services.

This time, ShardingSphere collaborated with the R&D team of E-hualu’s Hulu App, and helped them overcome data volume explosion for several times. Actually, this is just a tiny example of ShardingSphere’s various application scenarios around the world.

**About E-hualu & Hulu App**

Founded in 2001, Beijing E-hualu Information Technology Co., Ltd. is a listed company under the state-owned enterprise China Hualu Group Co., LTD. directly administrated by the State-owned Assets Supervision and Administration Commission of the State Council (SASAC). Its mission is to reduce energy consumption and the cost of long-term data management.

Hulu App is the first to-customer product of E-hualu.

**ShardingSphere Community:**

ShardingSphere Github: [https://github.com/apache/shardingsphere]()
ShardingSphere Twitter: [https://twitter.com/ShardingSphere]()
ShardingSphere Slack Channel: [apacheshardingsphere.slack.com]()
