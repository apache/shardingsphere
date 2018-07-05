# Sharding-Sphere Proposal

## Abstract

Sharding-Sphere is a ecosystem of transport distributed database middleware, focus on data sharding, distribute transaction and database orchestration. It provides maximum compatibilities for applications by JDBC driver or database protocols proxy.

## Proposal

Sharding-Sphere has good community in China, it has many end users, including [companies and organizations](http://shardingsphere.io/community/en/company/). 

We want to bring Sharding-Sphere into Apache Software Foundation in order to build a global, diverse and stronger open source community.

Dangdang submits this proposal to donate Sharding-Sphere's source codes and all related documentations to the Apache Software Foundation. The codes are already under Apache License Version 2.0.

* Code base: https://github.com/sharding-sphere/sharding-sphere

* Web site: http://shardingsphere.io/

* Documentations: http://shardingsphere.io/document/current/en/

* Community: http://shardingsphere.io/community/en/

## Background

Sharding-Sphere started in Open Source on GitHub at year 2016. Because of data growth quickly on internet company, relational database can not afford any more, but developers and DBAs still want use it to persist core data. Sharding-Sphere is beginning with a JDBC driver to sharding data only (name as Sharding-JDBC) at Dangdang internal framework. It offers data sharding, distribute transaction and database orchestration right now. Besides JDBC, proxy with database protocol is also supported. Sharding-sidecar and elastic data migration are in roadmap.

Sharding-JDBC has won [TOP 20 most popular open source projects in China 2016](http://www.oschina.net/project/top_cn_2016). 

Because in roadmap, it should not base on JDBC only, proxy and sidecar are not JDBC based, we have already rename it to **Sharding-Sphere** by a [public vote](https://github.com/sharding-sphere/sharding-sphere/issues/788).

## Rationale

Relational database still plays a very important role on current application system. The maturity of production and surrounding ecosystem, friendliness of data query, the mastery degree of developers and DBAs, it cannot be completely replaced with NoSQL in the near future.
But current relational database cannot support cloud native very well and unfriendliness for distributed system. 

The final proposal of Sharding-Sphere is let user use distributed databases as a single database. Sharding-Sphere manage the databases scattered around the system. 
There are 3 sub-project in Sharding-Sphere, they are Sharding-JDBC, Sharding-Proxy and Sharding-Sidecar(TODO).

Sharding-JDBC uses JDBC to connect databases without redirect cost for java application, best performance for production.

Sharding-Proxy is a transport database middleware, is deployed as a stateless server, and supports MySQL protocol now. There are 3 types of NewSQL in the paper [What’s Really New with NewSQL?](https://db.cs.cmu.edu/papers/2016/pavlo-newsql-sigmodrec2016.pdf), Sharding-Proxy is a `Transparent Sharding Middleware`.

Sharding-Sidecar is a new concept, just like `data panel` in service mesh. The interaction among the applications and the databases, which is concentrated in the mesher, is as complex and orderly as a cobweb. As the point, the concept of Database Mesh is similar with Service Mesh. 
The attention of Database Mesh focuses on how to connect the distributed data-access-layer and databases together. And it pays more attention to the interaction, which means the messy interaction among the applications and databases will be effectively organized. 
By using Database Mesh, applications and databases will form a large grid system, and they just need to be put into the right position on grid system accordingly, for they are all be governed by mesher.

## Current Status

### Meritocracy

This project started from Dangdang at 2015 and opened on GitHub at 2016. At 2017 Jingdong recognized its value, and determined to sponsor. We set up the PMC team and committer team.
The project has contributors and users from many companies. The new contributors are guided, discussed and reviewed by the existed PMC members. When they are ready, PMC will start a vote to promote him/her to become a member of PMC and Committer Team. See the details [here](http://shardingsphere.io/community/en/organization/). Contributions are always welcomed and highly valued. 

### Community

Now we have set 2 development teams in JingDong and Dangdang for the project. CHINA TELECOM, Sohu, DataMan and Enniu are interesting on Sharding, We hope to grow the base of contributors by inviting all those who offer contributions and excel through the use of The Apache Way. 
Right now, we make use of github as code hosting as well as gitter for community communication.

### Core Developers

The core developers are a diverse group of experienced open source developers and team leaders.

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

Two development teams in JingDong and Dangdang will continue to work on Sharding-Sphere 100% of the time for the foreseeable future with others from the growing community as well. And Sharding-Sphere is widely used in China by many [companies and organizations](http://shardingsphere.io/community/en/03-company/). So the risk of Sharding-Sphere becoming orphaned is low.

### Inexperience with Open Source

The current core developers all work for a company that has led or contributed to many open source software projects, including [Apache SkyWalking (Incubating)](https://github.com/apache/incubator-skywalking), [Apache Dubbo (Incubator)](https://incubator.apache.org/projects/dubbo.html), CNCF OpenTracing, [Elastic-Job](https://github.com/elasticjob), and others. Therefore, there is low risk related to inexperience with open source software and processes.

### Homogenous Developers

The current core developers work across a variety of organizations including Jingdong, Dangdang and some individual developers. While CHINA TELECOM, Sohu, DataMan and Enniu are interesting on Sharding, and we plan to encourage them to contribute and invite them to work together.

### Reliance on Salaried Developers

Two of the core developers are paid by their employer to contribute to the Sharding-Sphere project. We expect that Sharding-Sphere development will continue with salaried developers, and are committed to growing the community to include non-salaried developers as well.

### Relationships with Other Apache Products

Sharding-Sphere team worked with the SkyWalking team to introduce an automatic prober of Sharding-Sphere to send performance data to SkyWalking. Zookeeper is integrated as one of the service registration/discovery mechanisms.

### A Excessive Fascination with the Apache Brand

The Sharding-Sphere community acknowledges the value and recognition that the Apache brand would bring to the Sharding-Sphere project. However, our primary interest is in the community building process and long-term stability that the Apache Software Foundation provides for its projects.

## Documentation

A complete set of Sharding-Sphere documentations is provided on shardingsphere.io in both English and Simplified Chinese.

- [English](http://shardingsphere.io/document/current/en/)
- [Chinese](http://shardingsphere.io/document/current/cn/)

## Initial Source

The project consists of 3 distinct codebases: core, example and document. These have existed as separate git repositories.

-  <https://github.com/sharding-sphere/sharding-sphere>
-  <https://github.com/sharding-sphere/sharding-sphere-example>
-  <https://github.com/sharding-sphere/sharding-sphere-doc>

## Source and Intellectual Property Submission Plan

The code is currently Apache 2.0 license, and was verified to have no intellectual property or license issues before being released to open source by Dangdang in 2016. Dangdang will provide SGA and all committers will sign ICLA after the project join to incubator. 

## External Dependencies

All dependencies are managed using Apache Maven, none of the external libraries need to be packaged in a source distribution. 
Most of dependencies have Apache compatible licenses. But mysql and dbunit use GPL-2.0 and LGPL-2.1. 

We will remove dbunit and mysql dependencies in future. 
Dbunit only for initialization data set during test case running. 
MySQL JDBC driver is using on MySQL Proxy to connect MySQL now, we will use MySQL protocol format only and use socket to connect MySQL in future, so it do not need MySQL JDBC driver any more.

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
- 李广云, Guangyun Li
- 马晓光, Xiaoguang Ma
- 刘泽剑, ZeJian Liu
- 陈清阳, QingYang Chen

## Affiliations

- Jingdong: Liang Zhang, Yonglun Zhang, Juan Pan, Jun Zhao, Ling Yue
- Dangdang: Hao Cao, Kai Wang, Jiaqi Lin
- Orange Finance: QingYang Chen
- Individuals: Sheng Wu, Hongtao Gao, Guangyun Li, ZeJian Liu, Xiaoguang Ma

## Sponsors

### Champion

### Mentors

### Sponsoring Entity

We are requesting the Apache Incubator to sponsor this project.
