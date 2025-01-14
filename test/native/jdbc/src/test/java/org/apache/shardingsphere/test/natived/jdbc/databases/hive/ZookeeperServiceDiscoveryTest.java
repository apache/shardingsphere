/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.natived.jdbc.databases.hive;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "resource", "deprecation"})
@EnabledInNativeImage
@Testcontainers
class ZookeeperServiceDiscoveryTest {
    
    private static final int RANDOM_PORT_FIRST = InstanceSpec.getRandomPort();
    
    private static final Network NETWORK = Network.newNetwork();
    
    @Container
    private static final GenericContainer<?> ZOOKEEPER_CONTAINER = new GenericContainer<>("zookeeper:3.9.3-jre-17")
            .withNetwork(NETWORK)
            .withNetworkAliases("foo")
            .withExposedPorts(2181);
    
    /**
     * Due to the design flaw of testcontainers-java,
     * starting HiveServer2 using Zookeeper service discovery can only be done through the deprecated {@link FixedHostPortGenericContainer}.
     * See <a href="https://github.com/testcontainers/testcontainers-java/issues/9553">testcontainers/testcontainers-java#9553</a>.
     */
    @Container
    private static final GenericContainer<?> HS2_1_CONTAINER = new FixedHostPortGenericContainer<>("apache/hive:4.0.1")
            .withNetwork(NETWORK)
            .withEnv("SERVICE_NAME", "hiveserver2")
            .withEnv("SERVICE_OPTS", "-Dhive.server2.support.dynamic.service.discovery=true" + " "
                    + "-Dhive.zookeeper.quorum=" + ZOOKEEPER_CONTAINER.getNetworkAliases().get(0) + ":2181" + " "
                    + "-Dhive.server2.thrift.bind.host=0.0.0.0" + " "
                    + "-Dhive.server2.thrift.port=" + RANDOM_PORT_FIRST)
            .withFixedExposedPort(RANDOM_PORT_FIRST, RANDOM_PORT_FIRST)
            .dependsOn(ZOOKEEPER_CONTAINER);
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.database.hive.zsd.";
    
    // Due to https://issues.apache.org/jira/browse/HIVE-28317 , the `initFile` parameter of HiveServer2 JDBC Driver must be an absolute path.
    private static final String ABSOLUTE_PATH = Paths.get("src/test/resources/test-native/sql/test-native-databases-hive-iceberg.sql").toAbsolutePath().toString();
    
    private static DataSource logicDataSource;
    
    private final String jdbcUrlSuffix = ";serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2";
    
    private String jdbcUrlPrefix;
    
    @BeforeAll
    static void beforeAll() {
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url"), is(nullValue()));
    }
    
    @AfterAll
    static void afterAll() throws SQLException {
        try (Connection connection = logicDataSource.getConnection()) {
            connection.unwrap(ShardingSphereConnection.class).getContextManager().close();
        }
        NETWORK.close();
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url");
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url");
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url");
    }
    
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        jdbcUrlPrefix = "jdbc:hive2://" + ZOOKEEPER_CONTAINER.getHost() + ":" + ZOOKEEPER_CONTAINER.getMappedPort(2181) + "/";
        logicDataSource = createDataSource();
        TestShardingService testShardingService = new TestShardingService(logicDataSource);
        testShardingService.processSuccessInHive();
        HS2_1_CONTAINER.stop();
        int randomPortSecond = InstanceSpec.getRandomPort();
        try (
                GenericContainer<?> hs2SecondContainer = new FixedHostPortGenericContainer<>("apache/hive:4.0.1")
                        .withNetwork(NETWORK)
                        .withEnv("SERVICE_NAME", "hiveserver2")
                        .withEnv("SERVICE_OPTS", "-Dhive.server2.support.dynamic.service.discovery=true" + " "
                                + "-Dhive.zookeeper.quorum=" + ZOOKEEPER_CONTAINER.getNetworkAliases().get(0) + ":2181" + " "
                                + "-Dhive.server2.thrift.bind.host=0.0.0.0" + " "
                                + "-Dhive.server2.thrift.port=" + randomPortSecond)
                        .withFixedExposedPort(randomPortSecond, randomPortSecond)
                        .dependsOn(ZOOKEEPER_CONTAINER)) {
            hs2SecondContainer.start();
            extracted(hs2SecondContainer.getMappedPort(randomPortSecond));
            testShardingService.processSuccessInHive();
        }
    }
    
    private Connection openConnection() throws SQLException {
        Properties props = new Properties();
        return DriverManager.getConnection(jdbcUrlPrefix + jdbcUrlSuffix, props);
    }
    
    private DataSource createDataSource() throws SQLException {
        extracted(HS2_1_CONTAINER.getMappedPort(RANDOM_PORT_FIRST));
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/hive/zsd.yaml?placeholder-type=system_props");
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0" + ";initFile=" + ABSOLUTE_PATH + jdbcUrlSuffix);
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1" + ";initFile=" + ABSOLUTE_PATH + jdbcUrlSuffix);
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2" + ";initFile=" + ABSOLUTE_PATH + jdbcUrlSuffix);
        return new HikariDataSource(config);
    }
    
    private void extracted(final int hiveServer2Port) throws SQLException {
        String connectionString = ZOOKEEPER_CONTAINER.getHost() + ":" + ZOOKEEPER_CONTAINER.getMappedPort(2181);
        Awaitility.await().atMost(Duration.ofMinutes(2L)).ignoreExceptions().until(() -> {
            try (
                    CuratorFramework client = CuratorFrameworkFactory.builder().connectString(connectionString)
                            .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build()) {
                client.start();
                List<String> children = client.getChildren().forPath("/hiveserver2");
                assertThat(children.size(), is(1));
                return children.get(0).startsWith("serverUri=0.0.0.0:" + hiveServer2Port + ";version=4.0.1;sequence=");
            }
        });
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            openConnection().close();
            return true;
        });
        try (
                Connection connection = openConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds_0");
            statement.executeUpdate("CREATE DATABASE demo_ds_1");
            statement.executeUpdate("CREATE DATABASE demo_ds_2");
        }
    }
}
