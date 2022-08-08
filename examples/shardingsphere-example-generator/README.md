# Example codes generator of ShardingSphere

Project uses freemarker template engine to generate example codes of ShardingSphere.

## Usage

1. Configure YAML file

File: `src/main/resources/config.yaml`

2. Generate code

Run `org.apache.shardingsphere.example.generator.ExampleGeneratorMain` to generate example codes.

Generated codes: `target/generated-sources/shardingsphere-${product}-sample`

## Configuration Item Explanation

| *Name*      | *Description*     | *Options*                                                                                                                            |
|:----------- | ----------------- |:-------------------------------------------------------------------------------------------------------------------------------------|
| product     | product           | jdbc, proxy                                                                                                                          |
| mode        | operating mode    | cluster-zookeeper, cluster-etcd, standalone                                                                                          |
| transaction | transaction type  | local, xa-atomikos, xa-narayana                                                                                                                              |
| features    | feature set       | sharding, readwrite-splitting, db-discovery, encrypt                                                                                 |
| frameworks  | framework set     | jdbc, spring-boot-starter-jdbc, spring-boot-starter-jpa, spring-boot-starter-mybatis, spring-namespace-jpa, spring-namespace-mybatis |
| host        | database host     |                                                                                                                                      |
| port        | database port     |                                                                                                                                      |
| username    | database username |                                                                                                                                      |
| password    | database password |                                                                                                                                      |
