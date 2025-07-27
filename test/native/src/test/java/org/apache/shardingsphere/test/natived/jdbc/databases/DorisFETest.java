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
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtil;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@EnabledInNativeImage
@Testcontainers
class DorisFETest {
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.doris.";
    
    @SuppressWarnings("resource")
    @Container
    private final GenericContainer<?> container = new GenericContainer<>("dyrnq/doris:3.0.5")
            .withEnv("RUN_MODE", "standalone")
            .withEnv("SKIP_CHECK_ULIMIT", "true")
            .withExposedPorts(9030)
            .withStartupTimeout(Duration.ofMinutes(10L));
    
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
        ResourceUtil.closeJdbcDataSource(logicDataSource);
        System.clearProperty(systemPropKeyPrefix + "ds0.jdbc-url");
        System.clearProperty(systemPropKeyPrefix + "ds1.jdbc-url");
        System.clearProperty(systemPropKeyPrefix + "ds2.jdbc-url");
    }
    
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        jdbcUrlPrefix = "jdbc:mysql://localhost:" + container.getMappedPort(9030) + "/";
        logicDataSource = createDataSource();
        testShardingService = new TestShardingService(logicDataSource);
        initEnvironment();
        testShardingService.processSuccessWithoutTransactions();
        testShardingService.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableInDorisFE();
        testShardingService.getOrderItemRepository().createTableInDorisFE();
        testShardingService.getAddressRepository().createTableInDorisFE();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private DataSource createDataSource() throws SQLException {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            try (Connection connection = DriverManager.getConnection(jdbcUrlPrefix, "root", null)) {
                assertThat(connection.createStatement().executeQuery("SELECT `host`, `join`, `alive` FROM frontends()").next(), is(true));
                assertThat(connection.createStatement().executeQuery("SELECT `host`, `alive` FROM backends()").next(), is(true));
            }
            return true;
        });
        try (
                Connection connection = DriverManager.getConnection(jdbcUrlPrefix, "root", null);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds_0");
            statement.executeUpdate("CREATE DATABASE demo_ds_1");
            statement.executeUpdate("CREATE DATABASE demo_ds_2");
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/doris.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0");
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1");
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2");
        return new HikariDataSource(config);
    }
}
