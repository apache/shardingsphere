+++ 
title = "1st Academic Paper by Apache ShardingSphere Accepted by ICDE, A Top-Level Database Conference"
weight = 47
chapter = true 
+++

![Image description](https://miro.medium.com/max/700/0*7LHt7QNpp9ltJNm_)

## ICDE Accepted Apache ShardingSphere’s First Academic Paper

Recently, “Apache ShardingSphere: A Holistic and Pluggable Platform for Data Sharding”, co-authored by the Apache ShardingSphere community, SphereEx Lab and the Department of Computer Science of Chongqing University, has been accepted by [The International Conference on Data Engineering (ICDE)](https://icde2022.ieeecomputer.my/), one of the top international conferences on data engineering and databases. This conference paper is the first one focusing on data sharding in the database industry.

ICDE is a top-level academic conference on databases and data mining, as well as the flagship conference of the [Institute of Electrical and Electronics Engineers (IEEE)](https://www.ieee.org/), the world’s largest technical professional organization for the advancement of technology. ICDE, ACM SIGMOD Conference and International Conference on Very Large Data Bases are known as the top three data management and database conferences in the world.

This paper’s publication represents the culmination of 5-year of R&D and implementation testing for Apache ShardingSphere guided by its Database Plus development concept. The acceptance of this paper stands as a testament of the acknowledgment of Database Plus, Apache ShardingSphere’s enhanced computing engine and plugin-oriented architecture by the academic community.

Thanks to the acceptance of this paper, our community is proud to announce that our efforts have led to innovative breakthroughs in core database technologies.

In this paper, the co-authors propose that: considering that today relational databases that are not scalable and efficient enough to solve high concurrency problems still dominate online transaction processing and that NewSQL databases could lead to additional training costs, the database middleware Apache ShardingSphere becomes a very friendly and efficient way to connect and manage multiple databases.

## Apache ShardingSphere: A Scientific Development Path

> Why Did We Choose to Improve the Database Ecosystem Rather Than Creating a New Database?

The aim of Apache ShardingSphere is to better utilize the computing and storage capabilities of relational databases in distributed scenarios, rather than implementing a new type of relational database.

So far, as relational databases are still the best choice for complete transactions, they are often used to handle online transaction processing. However, the relational database is designed for a single machine. In other words, at the initial stage developers did not take big data into account. In short, relational databases are less scalable to efficiently solve high concurrency problems.

In this context, NewSQL came into being. However, New means this type of database is developed from scratch. Although it is suitable for current application scenarios, it has not been tested in enough real production environments, and for enterprise maintainers, it has a steep learning curve.

Apache ShardingSphere just released its official version 5.1.0. With this update, Apache ShardingSphere has also shifted its positioning to Database Plus with the desire to build the standard and ecosystem above different databases. Apart from enhanced capabilities, the Database Plus architecture also opens up a highly scalable database ecosystem for developers and users.

> Apache ShardingSphere’s Multiple Features

- Pluggable
Apache ShardingSphere adopts a “microkernel + three-layer plugin oriented” model — making the kernel, functional components and the whole ecosystem pluggable and scalable. Developers can leverage our plugins to configure all or some of ShardingSphere’s features according to their needs. Under the Apache ShardingSphere architecture, all features can be assembled into the ShardingSphere system just like building blocks. You can either use them separately or combine them together as needed.

However, in developers’ standard-oriented SPI programming, the kernel is not affected by the incremental functions, therefore multiple functional modules of ShardingSphere can be flexibly combined in real application scenarios, leaving discretion for our users.

Addtionally, Apache ShardingSphere is designed on the basis of SPI (Service Provider Interface, a service discovery mechanism provided by Java JDK) among other design patterns. Therefore, various types of databases, functions, and sharding algorithms can be easily added, removed or combined as needed.

Among them, as the primary development direction of ShardingSphere, data sharding has always been one of the most important features for us. For many users, data sharding was one of the reasons why they chose ShardingSphere. Generally speaking, performing data sharding on relational databases and removing the limitation of the storage capacity of a standalone machine by means of horizontal data sharding is an ideal solution for enterprises.

- High Performance
The paper mentioned that Chongqing University and SphereEx conducted multiple rounds of tests under Sysbench and TPCC testing scenarios, verifying that, using the same configuration, the performance of Apache ShardingSphere is superior to most sharding systems and new database architectures. Detailed records are shown in the following graphs:

![Sysbench test result](https://miro.medium.com/max/700/0*I_SUNauGQnVAxeBT)
![Comparison with Distributed Systems in Different Scenarios (TPCC)](https://miro.medium.com/max/700/0*X9YfmCPhECRUtheA)


## NConclusion

Apache ShardingSphere has been adopted and tested by hundreds of enterprises, from the Internet, gaming, banking, insurance & securities, manufacturing, telecommunications, new retail sectors to the public sector, etc., and has been tested in multiple production scenarios.

With its first paper being included in ICDE, Apache ShardingSphere’s concept has now been recognized by the academic community. The paper will provide new solutions for the development of the database industry and the improvement of data processing efficiency.

It also lays a solid theoretical foundation for the application of distributed capabilities in relational databases, the popularization of plugin architecture and the creation of an ecosystem layer above the fragmented database’s basic services.

We hope this paper and Apache ShardingSphere’s exploration can inspire developers and scholars inthe database field to build databases into a data service platform that is closer to enterprise business scenarios.