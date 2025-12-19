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
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "resource", "deprecation"})
@EnabledInNativeImage
@Testcontainers
class ZookeeperServiceDiscoveryTest {
    
    @AutoClose
    private final Network network = Network.newNetwork();
    
    @Container
    @AutoClose
    private final GenericContainer<?> zookeeperContainer = new GenericContainer<>("zookeeper:3.9.4-jre-17")
            .withNetwork(network)
            .withNetworkAliases("foo")
            .withExposedPorts(2181);
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.hive.zsd.";
    
    private DataSource logicDataSource;
    
    private final String jdbcUrlSuffix = ";serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2";
    
    private String jdbcUrlPrefix;
    
    private TestShardingService testShardingService;
    
    @BeforeEach
    void beforeEach() {
        assertThat(System.getProperty(systemPropKeyPrefix + "ds0.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(systemPropKeyPrefix + "ds1.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(systemPropKeyPrefix + "ds2.jdbc-url"), is(nullValue()));
    }
    
    @AfterEach
    void afterEach() throws SQLException {
        ResourceUtils.closeJdbcDataSource(logicDataSource);
        System.clearProperty(systemPropKeyPrefix + "ds0.jdbc-url");
        System.clearProperty(systemPropKeyPrefix + "ds1.jdbc-url");
        System.clearProperty(systemPropKeyPrefix + "ds2.jdbc-url");
    }
    
    /**
     * Due to the design flaw of testcontainers-java,
     * starting HiveServer2 using Zookeeper service discovery can only be done through the deprecated {@link FixedHostPortGenericContainer}.
     * See <a href="https://github.com/testcontainers/testcontainers-java/issues/9553">testcontainers/testcontainers-java#9553</a>.
     *
     * @throws SQLException SQL exception
     */
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        int randomPortFirst = InstanceSpec.getRandomPort();
        try (
                GenericContainer<?> hs2Container = new FixedHostPortGenericContainer<>("apache/hive:4.0.1")
                        .withNetwork(network)
                        .withEnv("SERVICE_NAME", "hiveserver2")
                        .withEnv("SERVICE_OPTS", "-Dhive.server2.support.dynamic.service.discovery=true" + " "
                                + "-Dhive.zookeeper.quorum=" + zookeeperContainer.getNetworkAliases().get(0) + ":2181" + " "
                                + "-Dhive.server2.thrift.bind.host=0.0.0.0" + " "
                                + "-Dhive.server2.thrift.port=" + randomPortFirst)
                        .withFixedExposedPort(randomPortFirst, randomPortFirst)
                        .dependsOn(zookeeperContainer)) {
            hs2Container.start();
            jdbcUrlPrefix = "jdbc:hive2://" + zookeeperContainer.getHost() + ":" + zookeeperContainer.getMappedPort(2181) + "/";
            logicDataSource = createLogicDataSource(hs2Container.getMappedPort(randomPortFirst));
            testShardingService = new TestShardingService(logicDataSource);
            initEnvironment();
            testShardingService.processSuccessInHive();
        }
        int randomPortSecond = InstanceSpec.getRandomPort();
        try (
                GenericContainer<?> hs2Container = new FixedHostPortGenericContainer<>("apache/hive:4.0.1")
                        .withNetwork(network)
                        .withEnv("SERVICE_NAME", "hiveserver2")
                        .withEnv("SERVICE_OPTS", "-Dhive.server2.support.dynamic.service.discovery=true" + " "
                                + "-Dhive.zookeeper.quorum=" + zookeeperContainer.getNetworkAliases().get(0) + ":2181" + " "
                                + "-Dhive.server2.thrift.bind.host=0.0.0.0" + " "
                                + "-Dhive.server2.thrift.port=" + randomPortSecond)
                        .withFixedExposedPort(randomPortSecond, randomPortSecond)
                        .dependsOn(zookeeperContainer)) {
            hs2Container.start();
            initRealDatabase(hs2Container.getMappedPort(randomPortSecond));
            initEnvironment();
            testShardingService.processSuccessInHive();
            testShardingService.cleanEnvironment();
        }
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createIcebergTableInHiveServer2();
        testShardingService.getOrderItemRepository().createIcebergTableInHiveServer2();
        testShardingService.getAddressRepository().createIcebergTableInHiveServer2();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private DataSource createLogicDataSource(final Integer hiveServer2Port) throws SQLException {
        initRealDatabase(hiveServer2Port);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/hive/zsd.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0" + jdbcUrlSuffix);
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1" + jdbcUrlSuffix);
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2" + jdbcUrlSuffix);
        return new HikariDataSource(config);
    }
    
    private void initRealDatabase(final int hiveServer2Port) throws SQLException {
        String connectString = zookeeperContainer.getHost() + ":" + zookeeperContainer.getMappedPort(2181);
        Awaitility.await().atMost(Duration.ofMinutes(2L)).ignoreExceptions().until(() -> {
            try (
                    CuratorFramework client = CuratorFrameworkFactory.builder().connectString(connectString).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build()) {
                client.start();
                List<String> children = client.getChildren().forPath("/hiveserver2");
                assertThat(children.size(), is(1));
                return children.get(0).startsWith("serverUri=0.0.0.0:" + hiveServer2Port + ";version=4.0.1;sequence=");
            }
        });
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            DriverManager.getConnection(jdbcUrlPrefix + jdbcUrlSuffix).close();
            return true;
        });
        try (
                Connection connection = DriverManager.getConnection(jdbcUrlPrefix + jdbcUrlSuffix);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE demo_ds_0");
            statement.execute("CREATE DATABASE demo_ds_1");
            statement.execute("CREATE DATABASE demo_ds_2");
        }
    }
}
