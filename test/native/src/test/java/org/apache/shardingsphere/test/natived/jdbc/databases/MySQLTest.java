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

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unable to use `org.testcontainers:mysql:1.20.0` under GraalVM Native Image.
 * Background comes from <a href="https://github.com/testcontainers/testcontainers-java/issues/7954">testcontainers/testcontainers-java#7954</a>.
 */
@EnabledInNativeImage
@Testcontainers
class MySQLTest {
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.mysql.";
    
    private final String password = "example";
    
    @SuppressWarnings("resource")
    @Container
    private final GenericContainer<?> container = new GenericContainer<>("mysql:9.4.0-oraclelinux9")
            .withEnv("MYSQL_ROOT_PASSWORD", password)
            .withExposedPorts(3306);
    
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
    void assertShardingInLocalTransactions() throws SQLException {
        jdbcUrlPrefix = "jdbc:mysql://localhost:" + container.getMappedPort(3306) + "/";
        logicDataSource = createDataSource();
        testShardingService = new TestShardingService(logicDataSource);
        initEnvironment();
        testShardingService.processSuccess();
        testShardingService.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInMySQL();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private Connection openConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", password);
        return DriverManager.getConnection(jdbcUrlPrefix, props);
    }
    
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private DataSource createDataSource() throws SQLException {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptionsMatching(CommunicationsException.class::isInstance).until(() -> {
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
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/mysql.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0");
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1");
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2");
        return new HikariDataSource(config);
    }
}
