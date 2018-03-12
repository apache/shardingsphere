# Sharding Proposal

## Abstract

Sharding is a distributed database middleware, focus on data sharding, read-write splitting, BASE transaction and database orchestration. It provides maximum compatibilities for applications by JDBC driver or database protocols proxy.

## Proposal

Sharding has good community in China, it has many end users, including [companies and organizations](http://shardingjdbc.io/community/en/04-company/). 

We want to bring Sharding into Apache Software Foundation in order to build a global, diverse and stronger open source community.

Dangdang submits this proposal to donate Sharding's source codes and all related documentations to the Apache Software Foundation. The codes are already under Apache License Version 2.0.

* Code base: https://github.com/shardingjdbc
  
* Web site: http://shardingjdbc.io/
  
* Documentations: http://shardingjdbc.io/document/en/
  
* Community: http://shardingjdbc.io/community/en/

## Background

Sharding started in Open Source on GitHub at year 2016. Beginning with a JDBC driver to sharding data only (name as Sharding-JDBC), Right now, it offers data sharding, read-write splitting, BASE transaction and database orchestration. Besides JDBC, proxy with database protocol is also supported, and sidecar feature is in our roadmap.

Sharding-JDBC has won [TOP 20 most popular open source projects in China 2016](http://www.oschina.net/project/top_cn_2016). 

Because in our roadmap, it should not base on JDBC only, proxy and sidecar features are not JDBC based, we want to rename it to **Sharding** after it accepted by Apache Software Foundation.

## Rationale

Relational database still plays a very important role on current application system. The maturity of production and surrounding ecosystem, friendliness of data query, the mastery degree of developers and DBAs, it cannot be completely replaced with NoSQL or NewSQL in the near future.
But current relational database cannot support cloud native very well, for distributed system unfriendliness. 

The final proposal of Sharding is let user use distributed databases as a single database. Sharding uses mesher to manage the databases scattered around the system. 
The interaction among the applications and the databases, which is concentrated in the mesher, is as complex and orderly as a cobweb. As the point, the concept of Database Mesh is similar like Service Mesh. 
The attention of Database Mesh is focused on how to connect the distributed data-access-layer and databases together. And it pays more attention to the interaction, which means the messy interaction among the applications and databases will be effectively organized. 
By using Database Mesh, applications and databases will form a large grid system, and they just need to be put into the right position on grid system accordingly, for they are all be governed by mesher.

## Current Status

### Meritocracy

This project started from Dangdang at year 2015 and opened on GitHub at year 2016. At year 2017 Jingdong recognized its value, and determined to sponsor. We set up the PMC team and committer team.
The project has contributors and users from many companies. The new contributors are guided, discussed and reviewed by the existed PMC members. When they are ready, PMC will start a vote to promote him/her to become a member of PMC and Committer Team. See the details [here](http://shardingjdbc.io/community/en/02-contribute/). Contributions are always welcomed. 

### Community

Now we have set 2 development teams on JingDong and Dangdang for project. CHINA TELECOM, Sohu, DataMan and Enniu are interesting on Sharding, We hope to grow the base of contributors by inviting all those who offer contributions and excel through the use of The Apache Way. 
Right now, we make use of github as code hosting as well as gitter for community communication.

### Core Developers

The core developers are a diverse group of experienced open source developers and team leaders.

#### PMC members

* 张亮, Liang Zhang, @terrymanu Java and architect expert, Jingdong

* 曹昊, Hao Cao, @haocao Senior Architect, Dangdang

* 吴晟, Sheng Wu, @wu-sheng APM and tracing expert, Apache SkyWalking(incubator) creator & PMC member

* 高洪涛, Hongtao Gao, @hanahmily Database and APM expert, Apache SkyWalking(incubator) PMC member

#### Committer members

* 李广云, Guangyun Li, @guangyun1013 Java Expert, Antfin

* 王文斌, Wenbin Wang, @YunaiV Technical manager, Kangda

* 朱政科, Zhengke Zhu, @CharlesMaster Senior engineer, Hangzhou Enniu Network Technology Company Limited

* 马晓光, Xiaoguang Ma, @ma-xiao-guang-64 Senior engineer, huimai365

* 刘泽剑, ZeJian Liu, @qiankunshe IT Manager, ZeDaYiSheng

## Known Risks

### Language and Culture

### Orphaned products

### Inexperience with Open Source

### Homogenous Developers

### Reliance on Salaried Developers

### Relationships with Other Apache Products

### A Excessive Fascination with the Apache Brand

## Documentation

A complete set of Sharding documentations is provided on shardingjdbc.io in both English and Simplified Chinese.

* [English](http://shardingjdbc.io/document/en/)

* [Chinese](http://shardingjdbc.io/document/cn/)

## Initial Source

The project consists of five distinct codebases: JDBC driver, Proxy, Opentracing adapter, example and document. These have existed as separate git repositories.

* https://github.com/shardingjdbc/sharding-jdbc

* https://github.com/shardingjdbc/sharding-opentracing

* https://github.com/shardingjdbc/sharding-example

* https://github.com/shardingjdbc/sharding-doc

## External Dependencies

All dependencies are managed using Apache Maven, none of the external libraries need to be packaged in a source distribution. 
Most of dependencies all have Apache compatible licenses. But mysql and dbunit use GPL-2.0 and LGPL-2.1. 

We will remove dbunit and mysql dependencies in future. 
Dbunit only for initialization data set during test case running. 
MySQL JDBC driver is using on MySQL Proxy to connect MySQL now, we will mock MySQL protocol to connect MySQL in future and do not need MySQL driver any more.

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

https://github.com/shardingjdbc/sharding-jdbc.git

https://github.com/shardingjdbc/sharding-jdbc-opentracing.git

https://github.com/shardingjdbc/sharding-jdbc-example.git

https://github.com/shardingjdbc/sharding-jdbc-doc.git

### Issue Tracking

https://github.com/shardingjdbc/sharding-jdbc/issues

### Continuous Integration tool

Travis

### Other Resources

gitter: https://gitter.im/Sharding-JDBC/shardingjdbc

## Initial Committers

* 张亮, Liang Zhang, zhangliang@apache.org

* 曹昊, Hao Cao, 

* 吴晟, Sheng Wu, 

* 高洪涛, Hongtao Gao, 

* 李广云, Guangyun Li, 

* 王文斌, Wenbin Wang, 

* 朱政科, Zhengke Zhu, 

* 马晓光, Xiaoguang Ma, 

* 刘泽剑, ZeJian Liu, 

## Affiliations

* Jingdong: Liang Zhang

* Dangdang: Hao Cao

* Enniu: Zhengke Zhu

* Individuals: Sheng Wu, Hongtao Gao, Guangyun Li, Wenbin Wang, ZeJian Liu, Xiaoguang Ma

## Sponsors

### Champion

### Mentors

### Sponsoring Entity

We are requesting the Apache Incubator to sponsor this project.
