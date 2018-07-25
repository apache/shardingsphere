# Sharding-Sphere Proposal

## Abstract

Sharding-Sphere is an ecosystem of transparent distributed database middleware, focusing on data sharding, distributed transaction and database orchestration. It provides maximum compatibility for applications through Sharding-JDBC (a driver to implement JDBC) or Sharding-Proxy (a proxy to implement database transfer protocol).

## Proposal

With a large number of end users, Sharding-Sphere has a fairly huge community in China. It is also widely adopted by many [companies and organizations](http://shardingsphere.io/community/en/company/) as a solution to process their massive amounts of data. 

We believe that bringing Sharding-Sphere into Apache Software Foundation could advance development of a stronger and more diverse open source community.

Dangdang submits this proposal to donate Sharding-Sphere's source codes and all related documentations to Apache Software Foundation. The codes are already under Apache License Version 2.0.

* Code base: https://github.com/sharding-sphere/sharding-sphere

* Web site: http://shardingsphere.io/

* Documentations: http://shardingsphere.io/document/current/en/

* Community: http://shardingsphere.io/community/en/

## Background

Relational database hardly supports such huge amounts of data any more which has increased rapidly in recent years, but developers and DBAs still want to use it to preserve core data. 

Sharding-Sphere was open sourced on Github in 2016. At the very beginning, Sharding-Sphere is just a JDBC driver for data sharding (name as Sharding-JDBC) at Dangdang internal framework; now it offers data sharding, distributed transaction and database orchestration. Besides JDBC, proxy to implement MySQL database protocol is also supported at present. Furthermore, our roadmap includes sidecar model and elastic data scalability function as well.

Due to the extension of project, we provide proxy model and sidecar model in addition to JDBC model. Therefore, we rename it to Sharding-Sphere by a [public vote](https://github.com/sharding-sphere/sharding-sphere/issues/788), which refers to a sharding ecosphere with Sharding-JDBC, Sharding-Proxy and Sharding Sidecar as its three sub-projects.

Sharding-JDBC has won the [TOP 20 most popular open source projects in China 2016](http://www.oschina.net/project/top_cn_2016).

## Rationale

Relational database still plays a very important role on current application system. Many of its features, such as the maturity of products and surrounding ecosystem, the friendliness of its data query and developers' and DBAs' mastery of it, cannot be completely replaced with NoSQL in the near future. However, current relational database cannot support cloud native very well and it is not friendly to distributed system.

It is the ultimate goal of Sharding-Sphere, which manages the databases scattering around the system, to make user use distributed databases as simply as using a single one.  

Without extra cost, Sharding-JDBC directly connects database with Java application to get the best performance.

As database middleware, Sharding-Proxy is deployed as a stateless server and supports MySQL protocol at present. In the paper [What’s Really New with NewSQL?](https://db.cs.cmu.edu/papers/2016/pavlo-newsql-sigmodrec2016.pdf), three types of NewSQL are introduced, among which Sharding-Proxy is a `Transparent Sharding Middleware`.

Sharding-Sidecar is a new concept, just like `data panel` in service mesh. The interaction networks among applications and databases, which is as complex and orderly as a cobweb, is concentrated upon mesh layer. At this point, the concept of Database Mesh is similar to Service Mesh. Database Mesh is centered on how to connect the distributed data access applications and databases together. It focuses more on interaction, namely organizing the messy interaction among applications and databases effectively. By using Database Mesh, applications and databases will form a large grid architecture, in which they just need to be put into the right position, for they are all be orchestrated by mesh layer.

## Current Status

### Meritocracy

Sharding-Sphere was incubated at Dangdang in 2015 and open sourced on GitHub in 2016. In 2017, JingDong recognized its value and determined to sponsor this project. Sharding-Sphere has contributors and users from many companies; we have set up the PMC Team and Committer Team. New contributors are guided and reviewed by existed PMC members. When they are ready, PMC will start a vote to promote him/her to become a member of PMC or Committer Team. See the details See the details [here](http://shardingsphere.io/community/en/organization/). Contributions are always welcomed and highly valued. 

### Community

Now we have set development teams for Sharding-Sphere respectively in JingDong and Dangdang. Companies like Orange Finance, Sohu and Shurenyun have shown great interest in Sharding-Sphere. We hope to grow the base of contributors by inviting all those who offer contributions through The Apache Way. Right now, we make use of github as code hosting as well as gitter for community communication.

### Core Developers

The core developers, including experienced open source developers and team leaders, have formed a group full of diversity.

#### PMC members

* 张亮, Liang Zhang, Java and architect expert, Jingdong

* 曹昊, Hao Cao, Senior Architect, Dangdang

* 吴晟, Sheng Wu, APM and tracing expert, Apache SkyWalking(incubator) creator & PMC member

* 高洪涛, Hongtao Gao, Database and APM expert, Apache SkyWalking(incubator) PMC member

* 史海峰, Haifeng Shi, @PegasusS Ex-Director, ele.me

#### Committer members

* 张永伦, Yonglun Zhang, @tuohai666 Senior engineer, Jingdong

* 潘娟, Juan Pan, @tristaZero Senior DBA, Jingdong

* 王凯, Kai Wang, @oracle219 Architect, Dangdang

* 林嘉琦, Jiaqi Lin, @chidaodezhongsheng Engineer, Dangdang

* 赵俊, Jun Zhao, @cherrylzhao Senior engineer, Jingdong

* 岳令, Ling Yue, @ling.yue QA Engineer, Dangdang

* 李广云, Guangyun Li, Java Expert, Antfin

* 马晓光, Xiaoguang Ma, Senior engineer, huimai365

* 刘泽剑, ZeJian Liu, IT Manager, ZeDaYiSheng

* 陈清阳, QingYang Chen, @beckhampu Senior engineer, Orange Finance

## Known Risks

### Orphaned products

Two development teams from JingDong and Dangdang will spare no pains to work on Sharding-Sphere in the future with contributors from the growing community. Also, Sharding-Sphere is widely adopted in China by many [companies and organizations](http://shardingsphere.io/community/en/03-company/). Thus, it is very unlikely that Sharding-Sphere becomes orphaned.

### Inexperience with Open Source

The current core developers all work for companies that have developed or contributed to many open source projects, such as [Apache SkyWalking (Incubating)](https://github.com/apache/incubator-skywalking), [Apache Dubbo (Incubator)](https://incubator.apache.org/projects/dubbo.html), CNCF OpenTracing, [Elastic-Job](https://github.com/elasticjob) and so on. Therefore, we believe we have enough experience to deal with open source.

### Homogenous Developers

The current core developers work across a variety of organizations including Jingdong and Dangdang; some individual developers are accepted as core developers of Sharding-Sphere as well. Considering that CHINA TELECOM, Sohu and Shurenyun have shown great interest in Sharding-Sphere, we plan to encourage them to contribute and invite them as contributors to work together.

### Reliance on Salaried Developers

At present, two of the core developers are paid by their employer to contribute to Sharding-Sphere project. It is estimated that the development of Sharding-Sphere will be continued with mainly salaried developers, and we will make efforts to attract more volunteers and grow the community.

### Relationships with Other Apache Products

An automatic prober of Sharding-Sphere is introduced into SkyWalking to send APM data. Saga provided by ServiceComb is adopted by Sharding-Sphere as one of the distributed transaction processing engines. Sharding-Sphere integrates Apache Zookeeper as one of the service registration/discovery mechanisms.

### A Excessive Fascination with the Apache Brand

We acknowledge the value and reputation that the Apache brand would bring to Sharding-Sphere. However, our primary interest is in the excellent community provided by Apache Software Foundation, in which all the projects could gain stability for long-term development.

## Documentation

A complete set of Sharding-Sphere documentations is provided on shardingsphere.io in both English and Simplified Chinese.

- [English](http://shardingsphere.io/document/current/en/)
- [Chinese](http://shardingsphere.io/document/current/cn/)

## Initial Source

The project consists of three distinct codebases: core, example and document. The address of three existed git repositories are as follows:

-  <https://github.com/sharding-sphere/sharding-sphere>
-  <https://github.com/sharding-sphere/sharding-sphere-example>
-  <https://github.com/sharding-sphere/sharding-sphere-doc>

## Source and Intellectual Property Submission Plan

The codes are currently under Apache License Version 2.0 and have been verified to have no intellectual property or license issues before being released to open source by Dangdang in 2016. Dangdang will provide SGA and all committers will sign ICLA after Sharding-Sphere is accepted into the Incubator.

## External Dependencies

As all dependencies are managed using Apache Maven, none of the external libraries need to be packaged in a source distribution. All dependencies have Apache compatible licenses except MySQL (GPL-2.0) and dbunit (LGPL-2.1).

We will remove dbunit and MySQL dependencies in future. Instead of dbunit, we will choose other methods to initialize data set during test case running. MySQL JDBC driver is adopted by Sharding-Proxy to connect MySQL now; we will use socket to connect MySQL and implement MySQL protocol format later, so MySQL JDBC driver is no longer needed.

| *Dependency*                         | *License*       |
| ------------------------------------ | --------------- |
| guava                                | Apache-2.0      |
| commons-lang3                        | Apache-2.0      |
| commons-pool                         | Apache-2.0      |
| commons-dbcp                         | Apache-2.0      |
| netty                                | Apache-2.0      |
| curator                              | Apache-2.0      |
| grpc                                 | Apache-2.0      |
| lombok                               | MIT             |
| groovy                               | Apache-2.0      |
| snakeyaml                            | Apache-2.0      |
| gson                                 | Apache-2.0      |
| spring-context-support               | Apache-2.0      |
| spring-context-test                  | Apache-2.0      |
| spring-boot-starter                  | Apache-2.0      |
| spring-boot-configuration-processor  | Apache-2.0      |
| spring-boot-starter-test             | Apache-2.0      |
| slf4j                                | MIT             |
| logback                              | EPL-1.0         |
| junit                                | EPL-1.0         |
| hamcrest                             | BSD 3-clause    |
| mockito                              | MIT             |
| dbunit                               | LGPL-2.1        |
| h2                                   | MPL-2.0/EPL-1.0 |
| mysql                                | GPL-2.0         |
| postgresql                           | BSD             |
| mssql-jdbc                           | MIT             |
| HikariCP                             | Apache-2.0      |


## Required Resources

### Git Repositories

<https://github.com/sharding-sphere/sharding-sphere.git>
<https://github.com/sharding-sphere/sharding-sphere-example.git>
<https://github.com/sharding-sphere/sharding-sphere-doc.git>

### Issue Tracking

<https://github.com/sharding-sphere/sharding-sphere/issues>

### Continuous Integration tool

Travis

### Mailing Lists

[sharding@googlegroups.com](mailto:sharding+subscribe@googlegroups.com)

### Communication

Gitter <https://gitter.im/shardingsphere/Lobby>

Slack <https://sharding.slack.com>

## Initial Committers

- 张亮, Liang Zhang, [zhangliang@apache.org](mailto:zhangliang@apache.org)
- 曹昊, Hao Cao,
- 吴晟, Sheng Wu, [wusheng@apache.org](mailto:wusheng@apache.org)
- 高洪涛, Hongtao Gao, [hanahmily@apache.org](mailto:hanahmily@apache.org)
- 张永伦, Yonglun Zhang
- 潘娟, Juan Pan
- 王凯, Kai Wang
- 林嘉琦, Jiaqi Lin
- 赵俊, Jun Zhao
- 岳令, Ling Yue
- 马晓光, Xiaoguang Ma
- 陈清阳, QingYang Chen

## Affiliations

- Jingdong: Liang Zhang, Yonglun Zhang, Juan Pan, Jun Zhao, Ling Yue
- Dangdang: Hao Cao, Kai Wang, Jiaqi Lin
- Orange Finance: QingYang Chen
- Individuals: Sheng Wu, Hongtao Gao, Xiaoguang Ma

## Sponsors

### Champion

### Mentors

### Sponsoring Entity

We are expecting the Apache Incubator could sponsor Sharding-Sphere.
