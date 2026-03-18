# ShardingSphere-example

Example for 1.x please see tags in `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

Example for 2.x or 3.x or 4.x please see tags in `https://github.com/apache/shardingsphere-example/tree/${tag}`

**Notices**

- *The `shardingsphere-jdbc-example-generator` module is a brand-new sample experience module.*

- *Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise, readwrite-splitting example will query empty data from the replica.*

## Using `master` branch

Please make sure some dependencies from [Apache ShardingSphere](https://github.com/apache/shardingsphere) has been installed since examples depend on that.
if you are a newbie for Apache ShardingSphere, you could prepare the dependencies as following: 

download and install [Apache ShardingSphere](https://github.com/apache/shardingsphere):

```bash
## download source code
git clone https://github.com/apache/shardingsphere.git

## compile source code
cd shardingsphere
./mvnw clean install -P-dev,release,all
```

## Module design

### project structure

```
shardingsphere-example
  ├── shardingsphere-jdbc-example-generator
  ├── shardingsphere-parser-example
  └── src/resources
          └── manual_schema.sql
```

## Available Examples

| Example                                                                         | Description                                                                    |
|---------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| [ShardingSphere-JDBC Examples](shardingsphere-jdbc-example-generator/README.md) | Generate the examples by configuration and show how to use ShardingSphere-JDBC |
