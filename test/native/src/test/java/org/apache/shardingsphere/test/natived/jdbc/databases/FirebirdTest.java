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
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
import org.awaitility.Awaitility;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.PageSizeConstants;
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
import java.time.Duration;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@EnabledInNativeImage
@Testcontainers
class FirebirdTest {
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.firebird.";
    
    private final String password = "masterkey";
    
    @SuppressWarnings("resource")
    @Container
    private final GenericContainer<?> container = new GenericContainer<>("firebirdsql/firebird:5.0.3")
            .withEnv("FIREBIRD_ROOT_PASSWORD", password)
            .withEnv("FIREBIRD_USER", "alice")
            .withEnv("FIREBIRD_PASSWORD", password)
            .withEnv("FIREBIRD_DATABASE", "mirror.fdb")
            .withEnv("FIREBIRD_DATABASE_DEFAULT_CHARSET", "UTF8")
            .withExposedPorts(3050);
    
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
    void assertShardingInLocalTransactions() throws Exception {
        jdbcUrlPrefix = "jdbc:firebird://localhost:" + container.getMappedPort(3050) + "//var/lib/firebird/data/";
        logicDataSource = createDataSource();
        testShardingService = new TestShardingService(logicDataSource);
        initEnvironment();
        testShardingService.processSuccess();
        testShardingService.cleanEnvironmentInFirebird();
    }
    
    /**
     * Docker Image `firebirdsql/firebird` cannot use `TRUNCATE TABLE`.
     * See <a href="https://github.com/FirebirdSQL/firebird/issues/2892">FirebirdSQL/firebird#2892</a>.
     *
     * @throws SQLException SQL Exception
     */
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableInFirebird();
        testShardingService.getOrderItemRepository().createTableInFirebird();
        testShardingService.getAddressRepository().createTableInFirebird();
    }
    
    private Connection openConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "alice");
        props.setProperty("password", password);
        return DriverManager.getConnection(jdbcUrlPrefix + "mirror.fdb", props);
    }
    
    /**
     * Due to <a href="https://github.com/FirebirdSQL/jaybird/issues/629">FirebirdSQL/jaybird#629</a>,
     * the SQL statement `Create Database` cannot be executed on the Firebird JDBC driver.
     * Unit testing requires the use of {@link org.firebirdsql.management.FBManager}.
     *
     * @return Data Source
     * @throws Exception Exception
     * @see org.firebirdsql.management.FBManager
     */
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private DataSource createDataSource() throws Exception {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            openConnection().close();
            return true;
        });
        try (FBManager fbManager = new FBManager()) {
            fbManager.setServer("localhost");
            fbManager.setUserName("alice");
            fbManager.setPassword(password);
            fbManager.setFileName("/var/lib/firebird/data/mirror.fdb");
            fbManager.setPageSize(PageSizeConstants.SIZE_16K);
            fbManager.setDefaultCharacterSet("UTF8");
            fbManager.setPort(container.getMappedPort(3050));
            fbManager.start();
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_0.fdb", "alice", password);
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_1.fdb", "alice", password);
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_2.fdb", "alice", password);
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/firebird.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", jdbcUrlPrefix + "demo_ds_0.fdb");
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", jdbcUrlPrefix + "demo_ds_1.fdb");
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", jdbcUrlPrefix + "demo_ds_2.fdb");
        return new HikariDataSource(config);
    }
}
