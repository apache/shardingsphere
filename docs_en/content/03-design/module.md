+++
toc = true
title = "Module Declaration"
weight = 3
prev = "/03-design/architecture/"
next = "/03-design/roadmap/"

+++

```
sharding-jdbc
    ├──sharding-jdbc-core                                                     Can be used directly
    ├──sharding-jdbc-core-spring                                              Can’t be used directly
    ├      ├──sharding-jdbc-core-spring-namespace                             Can be used directly
    ├      ├──sharding-jdbc-core-spring-boot                                  Can be used directly
    ├──sharding-jdbc-orchestration                                            Can be used directly
    ├──sharding-jdbc-transaction-parent                                       Can’t be used directly
    ├      ├──sharding-jdbc-transaction                                       Can be used directly
    ├      ├──sharding-jdbc-transaction-storage                               Can’t be used directly
    ├      ├──sharding-jdbc-transaction-async-job                             Can’t be used directly,Download tar packages directly, configure & startup
    ├──sharding-jdbc-plugin                                                   Can be used directly,Currently contains a custom distributed self-increasing primary key

sharding-jdbc-example                                                         
    ├──sharding-jdbc-raw-jdbc-example                                         
    ├      ├──sharding-jdbc-raw-jdbc-java-example                             
    ├      ├──sharding-jdbc-raw-jdbc-yaml-example                             
    ├──sharding-jdbc-spring-example                                           
    ├      ├──sharding-jdbc-spring-namespace-jpa-example                      
    ├      ├──sharding-jdbc-spring-namespace-mybatis-example                  
    ├──sharding-jdbc-spring-boot-example                                      
    ├      ├──sharding-jdbc-spring-boot-data-jpa-example                      
    ├      ├──sharding-jdbc-spring-boot-data-mybatis-example                  
    ├──sharding-jdbc-orchestration-example                                    
    ├      ├──sharding-jdbc-orchestration-etcd-java-example                   
    ├      ├──sharding-jdbc-orchestration-zookeeper-yaml-example              
    ├      ├──sharding-jdbc-orchestration-etcd-yaml-example                   
    ├──sharding-jdbc-orchestration-spring-example                             
    ├      ├──sharding-jdbc-orchestration-zookeeper-spring-boot-example       
    ├      ├──sharding-jdbc-orchestration-etcd-spring-boot-example            
    ├      ├──sharding-jdbc-orchestration-zookeeper-spring-namespace-example  
    ├      ├──sharding-jdbc-orchestration-etcd-spring-namespace-example       
    ├──sharding-jdbc-transaction-example                                      

sharding-jdbc-doc                                                             Can’t be used directly,please direct read the official website
```
