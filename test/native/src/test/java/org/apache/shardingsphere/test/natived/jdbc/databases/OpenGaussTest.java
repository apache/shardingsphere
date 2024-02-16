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

class OpenGaussTest {
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.database.opengauss.";
    
    private static final String USERNAME = "gaussdb";
    
    private static final String PASSWORD = "openGauss@123";
    
    private static final String DATABASE = "postgres";
    
    private String jdbcUrlPrefix;
    
    private TestShardingService testShardingService;
    
    @SuppressWarnings("resource")
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException {
        try (
                GenericContainer<?> openGaussContainer = new GenericContainer<>(DockerImageName.parse("opengauss/opengauss:5.0.0"))
                        .withEnv("GS_PASSWORD", PASSWORD)
                        .withExposedPorts(5432)) {
            openGaussContainer.start();
            jdbcUrlPrefix = "jdbc:opengauss://localhost:" + openGaussContainer.getMappedPort(5432) + "/";
            DataSource dataSource = createDataSource();
            testShardingService = new TestShardingService(dataSource);
            this.initEnvironment();
            testShardingService.processSuccess();
            testShardingService.cleanEnvironment();
        }
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInPostgres();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInPostgres();
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
        Awaitility.await().atMost(Duration.ofMinutes(1)).ignoreExceptions().until(() -> {
            openConnection().close();
            return true;
        });
        try (Connection connection = openConnection()) {
            connection.createStatement().executeUpdate("CREATE DATABASE demo_ds_0;");
            connection.createStatement().executeUpdate("CREATE DATABASE demo_ds_1;");
            connection.createStatement().executeUpdate("CREATE DATABASE demo_ds_2;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/databases/opengauss.yaml?placeholder-type=system_props");
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
