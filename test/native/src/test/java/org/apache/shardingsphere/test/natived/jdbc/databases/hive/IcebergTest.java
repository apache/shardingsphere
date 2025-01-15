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
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@EnabledInNativeImage
@Testcontainers
class IcebergTest {
    
    @SuppressWarnings("resource")
    @Container
    public static final GenericContainer<?> CONTAINER = new GenericContainer<>("apache/hive:4.0.1")
            .withEnv("SERVICE_NAME", "hiveserver2")
            .withExposedPorts(10000);
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.database.hive.iceberg.";
    
    // Due to https://issues.apache.org/jira/browse/HIVE-28317 , the `initFile` parameter of HiveServer2 JDBC Driver must be an absolute path.
    private static final String ABSOLUTE_PATH = Paths.get("src/test/resources/test-native/sql/test-native-databases-hive-iceberg.sql").toAbsolutePath().toString();
    
    private static DataSource logicDataSource;
    
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
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url");
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url");
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url");
    }
    
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        jdbcUrlPrefix = "jdbc:hive2://localhost:" + CONTAINER.getMappedPort(10000) + "/";
        logicDataSource = createDataSource();
        TestShardingService testShardingService = new TestShardingService(logicDataSource);
        testShardingService.processSuccessInHive();
    }
    
    private Connection openConnection() throws SQLException {
        Properties props = new Properties();
        return DriverManager.getConnection(jdbcUrlPrefix, props);
    }
    
    private DataSource createDataSource() throws SQLException {
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
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/hive/iceberg.yaml?placeholder-type=system_props");
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0" + ";initFile=" + ABSOLUTE_PATH);
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1" + ";initFile=" + ABSOLUTE_PATH);
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2" + ";initFile=" + ABSOLUTE_PATH);
        return new HikariDataSource(config);
    }
}
