+++ 
title = "ShardingSphere-JDBC Driver Released: A JDBC Driver That Requires No Code Modifications"
weight = 65
chapter = true 
+++

## Background
`ShardingSphereDataSourceFactory` is the most basic user API of [Apache ShardingSphere
JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc), and it is used to transform users' rule configuration objects and generate the standard implementation of the `DataSource`.

It also provides `YamlShardingSphereDataSourceFactory` for `YAML` configuration, as well as the custom namespace for the Spring and `Spring Boot Starter`.

`DataSource` is a standard Java JDBC interface. Engineers can use it to further create JDBC-compliant `Connection`, `Statement`, `PreparedStatement`, `ResultSet`, and other familiar standard objects. It is fully consistent with the implementation of the JDBC interface so there's no difference when engineers use Apache ShardingSphere-JDBC or use a native JDBC. Additionally, it can also transparently interface with various [ORM](https://stackoverflow.com/questions/1279613/what-is-an-orm-how-does-it-work-and-how-should-i-use-one) frameworks.

## Pain Points
Although the standard JDBC interface can be fully adapted during the development process, creating a `DataSource` through the ShardingSphere API can change the way the engineer originally load the database driver.

Although only a small part (one line) of startup code needs to be modified, it actually adds additional development costs for systems that would like to migrate smoothly to ShardingSphere. And for the system that cannot master the source code (such as an external sourcing system), it is truly difficult to use ShardingSphere.

[ShardingSphere](https://shardingsphere.apache.org/) has always lacked a JDBC-driven implementation largely due to its design. Java-configured ShardingSphere-JDBC can achieve programmable flexibility, but JDBC’s Driver interface does not provide much room for additional configuration.

The configuration flexibility of ShardingSphere will be greatly restricted through sole `URL` and `Properties`. Although `YAML` configuration can be better adapted to Driver’s `URL` and is more readable, it belongs to the category of static configuration and is obviously less flexible than dynamic configuration.

Therefore, ShardingSphere-JDBC uses a similar strategy of a database connection pool, bypassing the limitations of the JDBC standard interface and directly exposing the `DataSource` to the user.

However, it is almost an unbreakable barrier not to change a single line of code, which is the biggest pain point while improving ShardingSphere-JDBC’s ease of use.

## Opportunity
As [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) is developed ( the ShardingSphere client), its two major ecosystem features emerged, namely hybrid deployment and [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/).

The lightweight and high-performance features of ShardingSphere-JDBC make it more suitable for application-oriented [CRU](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete)D operations. The ease of use and compatibility of ShardingSphere-Proxy makes it more suitable for DDL operations oriented toward database management and control. The two clients can be used together to complement each other, to provide a new and more complete architecture solution.

With both programming and SQL capabilities, DistSQL strikes the perfect balance between flexibility and ease of use. In an architectural model where the configuration properties of ShardingSphere-JDBC are significantly reduced, it is the best solution to use JDBC URL to connect to the governance center and DistSQL for the configuration operations.

DistSQL ensures security which Java and YAML are short of, and it naturally supports permission control and SQL auditing, and other high-order capabilities. With DistSQL, DBAs (database administrators) can operate and maintain database clusters with great ease.

## Implementation
After the preconditions were met, ShardingSphere-JDBC version 5.1.2 took the opportunity to provide a JDBC driver that can be used only through configuration changes, without requiring engineers to modify the code.

**Driver class name**

`org.apache.shardingsphere.driver.ShardingSphereDriver`

**URL configuration description**

- Prefix: `jdbc:shardingsphere:`
- Config files: `xxx.yaml`, its format is consistent with that of `YAML` configuration.
- Load rule of config files:
If there’s no prefix, the configuration file is loaded from an absolute path (AP).

`classpath`: the prefix indicates loading the configuration file from the classpath.

## Procedure
**Using native drivers**

```java
Class.forName("org.apache.shardingsphere.driver.ShardingSphereDriver");
String standardJdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = DriverManager.getConnection(standardJdbcUrl);
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...        }
    }
}
```
**Using the database connection pool**

```java
String driverClassName = "org.apache.shardingsphere.driver.ShardingSphereDriver";
String standardJdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";// take HikariCP as an example HikariDataSource dataSource = new HikariDataSource();
dataSource.setDriverClassName(driverClassName);
dataSource.setJdbcUrl(standardJdbcUrl);

String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...        }
    }
}
```

**Reference**

- [JDBC driver](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/jdbc-driver/)

## Conclusion

ShardingSphere-JDBC Driver officially makes ShardingSphere easier to use than ever before.

In the coming future, the JDBC driver can be further simplified by providing the governance center address directly in the `URL`. Apache ShardingSphere has made great strides towards diversified distributed clusters.

**Relevant Links:**

[GitHub issue](https://github.com/apache/shardingsphere/issues)

[Contributor Guide](https://shardingsphere.apache.org/community/en/involved/)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Chinese Community
](https://community.sphere-ex.com/)

**Author**
**Zhang Liang**

**Github:** @terrymanu

Zhang Liang, the founder & CEO of [SphereEx](https://www.sphere-ex.com/), served as the head of the architecture and database team of many large well-known Internet enterprises. He is enthusiastic about open source and is the founder and PMC chair of Apache ShardingSphere, [ElasticJob](https://shardingsphere.apache.org/elasticjob/), and other well-known open-source projects.

He is a member of the [Apache Software Foundation](https://www.apache.org/), a [Microsoft MVP](https://mvp.microsoft.com/), [Tencent Cloud TVP](https://cloud.tencent.com/tvp), and [Huawei Cloud MVP](https://developer.huaweicloud.com/mvp) and has more than 10 years of experience in the field of architecture and database. He advocates for elegant code, and has made great achievements in distributed database technology and academic research. He has served as a producer and speaker at dozens of major domestic and international industry and technology summits, including ApacheCon, QCon, AWS summit, DTCC, SACC, and DTC. In addition, he has published the book “Future Architecture: From Service to Cloud Native” as well as the paper “Apache ShardingSphere: A Holistic and Pluggable Platform for Data Sharding” published at this year’s ICDE, a top conference in the database field.
