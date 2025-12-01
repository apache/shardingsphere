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
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "resource"})
@EnabledInNativeImage
@Testcontainers
class SystemSchemasTest {
    
    @AutoClose
    private final Network network = Network.newNetwork();
    
    @Container
    @AutoClose
    private final GenericContainer<?> postgres = new GenericContainer<>("postgres:17.5-bookworm")
            .withEnv("POSTGRES_PASSWORD", "example")
            .withNetwork(network)
            .withNetworkAliases("some-postgres");
    
    @Container
    @AutoClose
    private final GenericContainer<?> hs2 = new GenericContainer<>("ghcr.io/linghengqian/hive:4.0.1-all-in-one")
            .withEnv("SERVICE_NAME", "hiveserver2")
            .withEnv("DB_DRIVER", "postgres")
            .withEnv("SERVICE_OPTS", "-Djavax.jdo.option.ConnectionDriverName=org.postgresql.Driver" + " "
                    + "-Djavax.jdo.option.ConnectionURL=jdbc:postgresql://some-postgres:5432/postgres" + " "
                    + "-Djavax.jdo.option.ConnectionUserName=postgres" + " "
                    + "-Djavax.jdo.option.ConnectionPassword=example")
            .withNetwork(network)
            .withExposedPorts(10000)
            .dependsOn(postgres);
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.hive.hms.";
    
    private DataSource logicDataSource;
    
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
    
    @Test
    void assertShardingInLocalTransactions() throws SQLException, IOException, InterruptedException {
        ExecResult initResult = hs2.execInContainer(
                "/opt/hive/bin/schematool", "-initSchema",
                "-dbType", "hive",
                "-metaDbType", "postgres",
                "-url", "jdbc:hive2://localhost:10000/default");
        assertThat(initResult.getStdout(), is("Initializing the schema to: 4.0.0\n"
                + "Metastore connection URL:\t jdbc:hive2://localhost:10000/default\n"
                + "Metastore connection Driver :\t org.apache.hive.jdbc.HiveDriver\n"
                + "Metastore connection User:\t APP\n"
                + "Starting metastore schema initialization to 4.0.0\n"
                + "Initialization script hive-schema-4.0.0.hive.sql\n"
                + "Initialization script completed\n"));
        jdbcUrlPrefix = "jdbc:hive2://localhost:" + hs2.getMappedPort(10000) + "/";
        logicDataSource = createDataSource();
        testShardingService = new TestShardingService(logicDataSource);
        initEnvironment();
        testShardingService.processSuccessInHive();
        testShardingService.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private DataSource createDataSource() throws SQLException {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            DriverManager.getConnection(jdbcUrlPrefix).close();
            return true;
        });
        try (
                Connection connection = DriverManager.getConnection(jdbcUrlPrefix);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE demo_ds_0");
            statement.execute("CREATE DATABASE demo_ds_1");
            statement.execute("CREATE DATABASE demo_ds_2");
        }
        Stream.of("demo_ds_0", "demo_ds_1", "demo_ds_2").parallel().forEach(this::initTable);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/hive/system-schemas.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0");
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1");
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2");
        return new HikariDataSource(config);
    }
    
    /**
     * TODO `shardingsphere-parser-sql-engine-hive` module does not support `set`, `create table` statements yet,
     *  we always need to execute the following Hive Session-level SQL in the current {@link javax.sql.DataSource}.
     * Hive does not support `AUTO_INCREMENT`,
     * refer to <a href="https://issues.apache.org/jira/browse/HIVE-6905">HIVE-6905</a>.
     *
     * @param databaseName database name
     * @throws RuntimeException SQL exception
     */
    private void initTable(final String databaseName) {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrlPrefix + databaseName);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL,order_type INT,user_id INT NOT NULL,address_id BIGINT NOT NULL,status string,"
                    + "PRIMARY KEY (order_id) disable novalidate) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2')");
            statement.execute("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL,order_id BIGINT NOT NULL,user_id INT NOT NULL,phone string,status string,"
                    + "PRIMARY KEY (order_item_id) disable novalidate) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2')");
            statement.execute("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL,address_name string NOT NULL,"
                    + "PRIMARY KEY (address_id) disable novalidate) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2')");
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
