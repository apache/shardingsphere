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
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.clickhouse.ClickHouseContainer;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Cannot use testcontainers-java style jdbcURL for Clickhouse Server due to unresolved
 * <a href="https://github.com/testcontainers/testcontainers-java/issues/8736">testcontainers/testcontainers-java#8736</a>.
 */
@SuppressWarnings("SqlNoDataSourceInspection")
@EnabledInNativeImage
@Testcontainers
class ClickHouseTest {
    
    @Container
    public static final ClickHouseContainer CONTAINER = new ClickHouseContainer("clickhouse/clickhouse-server:24.6.2.17");
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.database.clickhouse.";
    
    private String jdbcUrlPrefix;
    
    private TestShardingService testShardingService;
    
    @BeforeAll
    static void beforeAll() {
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url"), is(nullValue()));
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url"), is(nullValue()));
    }
    
    @AfterAll
    static void afterAll() {
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url");
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url");
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url");
    }
    
    /**
     * TODO Need to fix `shardingsphere-parser-sql-clickhouse` module to use {@link TestShardingService#cleanEnvironment()}
     *      after {@link TestShardingService#processSuccessInClickHouse()}.
     */
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        jdbcUrlPrefix = "jdbc:ch://localhost:" + CONTAINER.getMappedPort(8123) + "/";
        DataSource dataSource = createDataSource();
        testShardingService = new TestShardingService(dataSource);
        assertDoesNotThrow(() -> testShardingService.processSuccessInClickHouse());
    }
    
    /**
     * TODO Need to fix `shardingsphere-parser-sql-clickhouse` module to use `initEnvironment()`
     * before {@link TestShardingService#processSuccessInClickHouse()}.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    @SuppressWarnings("unused")
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInClickHouse();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInClickHouse();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", CONTAINER.getUsername());
        props.setProperty("password", CONTAINER.getPassword());
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName, props);
    }
    
    private DataSource createDataSource() throws SQLException {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            openConnection(CONTAINER.getDatabaseName()).close();
            return true;
        });
        try (
                Connection connection = openConnection(CONTAINER.getDatabaseName());
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds_0");
            statement.executeUpdate("CREATE DATABASE demo_ds_1");
            statement.executeUpdate("CREATE DATABASE demo_ds_2");
        }
        Stream.of("demo_ds_0", "demo_ds_1", "demo_ds_2").parallel().forEach(this::initTable);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/clickhouse.yaml?placeholder-type=system_props");
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0");
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1");
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2");
        return new HikariDataSource(config);
    }
    
    private void initTable(final String databaseName) {
        try (
                Connection connection = openConnection(databaseName);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("create table IF NOT EXISTS t_order\n"
                    + "(\n"
                    + "    order_id   Int64 NOT NULL DEFAULT rand(),\n"
                    + "    order_type Int32,\n"
                    + "    user_id    Int32 NOT NULL,\n"
                    + "    address_id Int64 NOT NULL,\n"
                    + "    status     String\n"
                    + ") engine = MergeTree\n"
                    + "      primary key (order_id)\n"
                    + "      order by (order_id)");
            statement.executeUpdate("create table IF NOT EXISTS t_order_item\n"
                    + "(\n"
                    + "    order_item_id Int64 NOT NULL DEFAULT rand(),\n"
                    + "    order_id      Int64 NOT NULL,\n"
                    + "    user_id       Int32 NOT NULL,\n"
                    + "    phone         String,\n"
                    + "    status        String\n"
                    + ") engine = MergeTree\n"
                    + "      primary key (order_item_id)\n"
                    + "      order by (order_item_id)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS t_address\n"
                    + "(\n"
                    + "    address_id   BIGINT NOT NULL,\n"
                    + "    address_name VARCHAR(100) NOT NULL,\n"
                    + "    PRIMARY      KEY (address_id)\n"
                    + ")");
            statement.executeUpdate("TRUNCATE TABLE t_order");
            statement.executeUpdate("TRUNCATE TABLE t_order_item");
            statement.executeUpdate("TRUNCATE TABLE t_address");
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
