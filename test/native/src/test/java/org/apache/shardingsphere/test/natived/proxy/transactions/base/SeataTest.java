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

package org.apache.shardingsphere.test.natived.proxy.transactions.base;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.HttpStatus;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.util.ProxyTestingServer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings({"SqlNoDataSourceInspection", "resource"})
@Disabled("See https://github.com/apache/incubator-seata/issues/7523 .")
@Testcontainers
class SeataTest {
    
    @Container
    private final GenericContainer<?> container = new GenericContainer<>("apache/seata-server:2.5.0")
            .withExposedPorts(8091)
            .waitingFor(Wait.forHttp("/health").forPort(8091).forStatusCode(HttpStatus.SC_OK).forResponsePredicate("\"ok\""::equals));
    
    @Container
    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.5-bookworm")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("src/test/resources/test-native/sh/postgres.sh").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/postgres.sh");
    
    private final String serviceDefaultGroupListKey = "service.default.grouplist";
    
    private ProxyTestingServer proxyTestingServer;
    
    private TestShardingService testShardingService;
    
    @BeforeEach
    void beforeEach() {
        assertThat(System.getProperty(serviceDefaultGroupListKey), is(nullValue()));
        System.setProperty(serviceDefaultGroupListKey, "127.0.0.1:" + container.getMappedPort(8091));
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(() -> {
            openConnection("test", "test", "jdbc:postgresql://127.0.0.1:" + postgresContainer.getMappedPort(5432) + "/")
                    .close();
            return true;
        });
        String absolutePath = Paths.get("src/test/resources/test-native/yaml/proxy/transactions/base").toAbsolutePath().toString();
        proxyTestingServer = new ProxyTestingServer(absolutePath);
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(() -> {
            openConnection("root", "root", "jdbc:postgresql://127.0.0.1:" + proxyTestingServer.getProxyPort() + "/postgres").close();
            return true;
        });
    }
    
    /**
     * TODO Apparently there is a real connection leak on Seata Client 2.5.0.
     */
    @AfterEach
    void afterEach() {
        Awaitility.await().pollDelay(5L, TimeUnit.SECONDS).until(() -> true);
        System.clearProperty(serviceDefaultGroupListKey);
        proxyTestingServer.close(Collections.singletonList("sharding_db"));
    }
    
    /**
     * {@link groovy.lang.Closure} related classes are not available on GraalVM Native Image.
     * This CLASS_BASE algorithm class is designed to emulate INLINE's {@code ds_${user_id % 2}}.
     * See <a href="https://github.com/oracle/graal/issues/5522">oracle/graal#5522</a> .
     *
     * @throws SQLException SQL Exception
     */
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        try (
                Connection connection = openConnection("root", "root", "jdbc:postgresql://127.0.0.1:" + proxyTestingServer.getProxyPort() + "/postgres");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE sharding_db");
        }
        try (
                Connection connection = openConnection("root", "root", "jdbc:postgresql://127.0.0.1:" + proxyTestingServer.getProxyPort() + "/sharding_db");
                Statement statement = connection.createStatement()) {
            statement.execute("REGISTER STORAGE UNIT ds_0 (URL='jdbc:postgresql://127.0.0.1:" + postgresContainer.getMappedPort(5432) + "/demo_ds_0',USER='test',PASSWORD='test'),"
                    + "ds_1 (URL='jdbc:postgresql://127.0.0.1:" + postgresContainer.getMappedPort(5432) + "/demo_ds_1',USER='test',PASSWORD='test'),"
                    + "ds_2 (URL='jdbc:postgresql://127.0.0.1:" + postgresContainer.getMappedPort(5432) + "/demo_ds_2',USER='test',PASSWORD='test')");
            statement.execute("CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='standard', SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME=CLASS_BASED,"
                    + "PROPERTIES('strategy'='STANDARD','algorithmClassName'='org.apache.shardingsphere.test.natived.commons.algorithm.ClassBasedInlineShardingAlgorithmFixture'))))");
            statement.execute("CREATE SHARDING TABLE RULE t_order (DATANODES('<LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order'),"
                    + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='SNOWFLAKE'))), t_order_item (DATANODES('<LITERAL>ds_0.t_order_item, ds_1.t_order_item, ds_2.t_order_item'),"
                    + "KEY_GENERATE_STRATEGY(COLUMN=order_item_id,TYPE(NAME='SNOWFLAKE')))");
            statement.execute("CREATE BROADCAST TABLE RULE t_address");
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:" + proxyTestingServer.getProxyPort() + "/sharding_db");
        config.setUsername("root");
        config.setPassword("root");
        DataSource dataSource = new HikariDataSource(config);
        testShardingService = new TestShardingService(dataSource);
        initEnvironment();
        testShardingService.processSuccess();
        testShardingService.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInPostgres();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInPostgres();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private Connection openConnection(final String username, final String password, final String jdbcUrl) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        return DriverManager.getConnection(jdbcUrl, props);
    }
}
