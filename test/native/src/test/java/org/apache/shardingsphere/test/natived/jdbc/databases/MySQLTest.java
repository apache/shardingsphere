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
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unable to use `org.testcontainers:mysql:1.19.3` under GraalVM Native Image.
 * Background comes from <a href="https://github.com/testcontainers/testcontainers-java/issues/7954">testcontainers/testcontainers-java#7954</a>.
 */
class MySQLTest {
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.database.mysql.";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private static final String DATABASE = "test";
    
    private String jdbcUrlPrefix;
    
    private TestShardingService testShardingService;
    
    @SuppressWarnings("resource")
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException {
        try (
                GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("mysql:8.2.0-oracle"))
                        .withEnv("MYSQL_DATABASE", DATABASE)
                        .withEnv("MYSQL_ROOT_PASSWORD", PASSWORD)
                        .withExposedPorts(3306)) {
            container.start();
            jdbcUrlPrefix = "jdbc:mysql://localhost:" + container.getMappedPort(3306) + "/";
            DataSource dataSource = createDataSource();
            testShardingService = new TestShardingService(dataSource);
            initEnvironment();
            testShardingService.processSuccess();
            testShardingService.cleanEnvironment();
        }
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInMySQL();
        testShardingService.getAddressRepository().createTableIfNotExists();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private Connection openConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        return DriverManager.getConnection(jdbcUrlPrefix + DATABASE, props);
    }
    
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private DataSource createDataSource() {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptionsMatching(e -> e instanceof CommunicationsException)
                .until(() -> {
                    openConnection().close();
                    return true;
                });
        try (Connection connection = openConnection()) {
            connection.createStatement().executeUpdate("CREATE DATABASE demo_ds_0;");
            connection.createStatement().executeUpdate("CREATE DATABASE demo_ds_1;");
            connection.createStatement().executeUpdate("CREATE DATABASE demo_ds_2;");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/databases/mysql.yaml?placeholder-type=system_props");
        try {
            assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url"), is(nullValue()));
            assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url"), is(nullValue()));
            assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url"), is(nullValue()));
            System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0");
            System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1");
            System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2");
            return new HikariDataSource(config);
        } finally {
            System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url");
            System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url");
            System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url");
        }
    }
}
