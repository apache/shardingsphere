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

package org.apache.shardingsphere.test.natived.jdbc.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Cannot use testcontainers-java style jdbcURL for Clickhouse Server due to unresolved
 * <a href="https://github.com/testcontainers/testcontainers-java/issues/8736">testcontainers/testcontainers-java#8736</a>.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "resource"})
@EnabledInNativeImage
@Testcontainers
class ClickHouseTest {
    
    @Container
    private final GenericContainer<?> container = new GenericContainer<>("clickhouse/clickhouse-server:24.11.1.2557")
            .withExposedPorts(8123);
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.clickhouse.";
    
    private DataSource logicDataSource;
    
    private String jdbcUrlPrefix;
    
    @BeforeEach
    void beforeEach() {
        assertThat(System.getProperty(systemPropKeyPrefix + "ds0.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(systemPropKeyPrefix + "ds1.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(systemPropKeyPrefix + "ds2.jdbc-url"), is(nullValue()));
    }
    
    @AfterEach
    void afterEach() throws SQLException {
        try (Connection connection = logicDataSource.getConnection()) {
            ContextManager contextManager = connection.unwrap(ShardingSphereConnection.class).getContextManager();
            for (StorageUnit each : contextManager.getStorageUnits(DefaultDatabase.LOGIC_NAME).values()) {
                each.getDataSource().unwrap(HikariDataSource.class).close();
            }
            contextManager.close();
        }
        System.clearProperty(systemPropKeyPrefix + "ds0.jdbc-url");
        System.clearProperty(systemPropKeyPrefix + "ds1.jdbc-url");
        System.clearProperty(systemPropKeyPrefix + "ds2.jdbc-url");
    }
    
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        jdbcUrlPrefix = "jdbc:ch://localhost:" + container.getMappedPort(8123) + "/";
        logicDataSource = createDataSource();
        TestShardingService testShardingService = new TestShardingService(logicDataSource);
        testShardingService.processSuccessInClickHouse();
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "default");
        props.setProperty("password", "");
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName, props);
    }
    
    private DataSource createDataSource() throws SQLException {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            openConnection("default").close();
            return true;
        });
        try (
                Connection connection = openConnection("default");
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds_0");
            statement.executeUpdate("CREATE DATABASE demo_ds_1");
            statement.executeUpdate("CREATE DATABASE demo_ds_2");
        }
        Stream.of("demo_ds_0", "demo_ds_1", "demo_ds_2").parallel().forEach(this::initTable);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/clickhouse.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0");
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1");
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2");
        return new HikariDataSource(config);
    }
    
    /**
     * ClickHouse does not support `AUTO_INCREMENT`,
     * refer to <a href="https://github.com/ClickHouse/ClickHouse/issues/56228">ClickHouse/ClickHouse#56228</a> .
     * TODO The {@code shardingsphere-parser-sql-clickhouse} module needs to be fixed to use SQL like `create table`,
     *  `truncate table` and `drop table`.
     *
     * @param databaseName database name
     * @throws RuntimeException SQL exception
     */
    private void initTable(final String databaseName) {
        try (
                Connection connection = openConnection(databaseName);
                Statement statement = connection.createStatement()) {
            statement.execute("create table IF NOT EXISTS t_order\n"
                    + "(\n"
                    + "    order_id   Int64 NOT NULL,\n"
                    + "    order_type Int32,\n"
                    + "    user_id    Int32 NOT NULL,\n"
                    + "    address_id Int64 NOT NULL,\n"
                    + "    status     VARCHAR(50)\n"
                    + ") engine = MergeTree\n"
                    + "      primary key (order_id)\n"
                    + "      order by (order_id)");
            statement.execute("create table IF NOT EXISTS t_order_item\n"
                    + "(\n"
                    + "    order_item_id Int64 NOT NULL,\n"
                    + "    order_id      Int64 NOT NULL,\n"
                    + "    user_id       Int32 NOT NULL,\n"
                    + "    phone         VARCHAR(50),\n"
                    + "    status        VARCHAR(50)\n"
                    + ") engine = MergeTree\n"
                    + "      primary key (order_item_id)\n"
                    + "      order by (order_item_id)");
            statement.execute("CREATE TABLE IF NOT EXISTS t_address\n"
                    + "(\n"
                    + "    address_id   BIGINT NOT NULL,\n"
                    + "    address_name VARCHAR(100) NOT NULL,\n"
                    + "    PRIMARY      KEY (address_id)\n"
                    + ")");
            statement.execute("TRUNCATE TABLE t_order");
            statement.execute("TRUNCATE TABLE t_order_item");
            statement.execute("TRUNCATE TABLE t_address");
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
