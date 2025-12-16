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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
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
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unable to use `org.testcontainers:presto:1.21.0` under GraalVM Native Image.
 * Background comes from <a href="https://github.com/testcontainers/testcontainers-java/issues/8657">testcontainers/testcontainers-java#8657</a>.
 */
@SuppressWarnings({"resource", "SqlNoDataSourceInspection"})
@EnabledInNativeImage
@Testcontainers
class PrestoTest {
    
    private final String systemPropKeyPrefix = "fixture.test-native.yaml.database.presto.";
    
    private String baseJdbcUrl;
    
    @Container
    private final GenericContainer<?> container = new GenericContainer<>("prestodb/presto:0.296")
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("src/test/resources/test-native/properties/presto-iceberg.properties").toAbsolutePath()),
                    "/opt/presto-server/etc/catalog/iceberg.properties")
            .waitingFor(Wait.forHttp("/v1/info/state").forPort(8080).forResponsePredicate("\"ACTIVE\""::equals));
    
    private DataSource logicDataSource;
    
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
        baseJdbcUrl = "jdbc:presto://localhost:" + container.getMappedPort(8080) + "/iceberg";
        logicDataSource = createDataSource();
        TestShardingService testShardingService = new TestShardingService(logicDataSource);
        testShardingService.processSuccessWithoutTransactions();
        testShardingService.cleanEnvironment();
    }
    
    private DataSource createDataSource() throws SQLException {
        Awaitility.await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            DriverManager.getConnection(baseJdbcUrl, "test", null).close();
            return true;
        });
        try (
                Connection con = DriverManager.getConnection(baseJdbcUrl, "test", null);
                Statement stmt = con.createStatement()) {
            stmt.execute("CREATE SCHEMA iceberg.demo_ds_0");
            stmt.execute("CREATE SCHEMA iceberg.demo_ds_1");
            stmt.execute("CREATE SCHEMA iceberg.demo_ds_2");
        }
        Stream.of("demo_ds_0", "demo_ds_1", "demo_ds_2").forEach(this::initSchema);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/presto.yaml?placeholder-type=system_props");
        System.setProperty(systemPropKeyPrefix + "ds0.jdbc-url", baseJdbcUrl + "/demo_ds_0");
        System.setProperty(systemPropKeyPrefix + "ds1.jdbc-url", baseJdbcUrl + "/demo_ds_1");
        System.setProperty(systemPropKeyPrefix + "ds2.jdbc-url", baseJdbcUrl + "/demo_ds_2");
        return new HikariDataSource(config);
    }
    
    /**
     * TODO `shardingsphere-parser-sql-engine-presto` module does not support `create table` and `truncate table` statements yet.
     * Presto Iceberg Connector does not support AUTO_INCREMENT columns.
     * Presto Iceberg Connector does not support Primary Key constraints.
     *
     * @param schemaName schema name
     * @throws RuntimeException Runtime exception
     */
    private void initSchema(final String schemaName) {
        try (
                Connection con = DriverManager.getConnection(baseJdbcUrl + "/" + schemaName, "test", null);
                Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL,order_type INTEGER,user_id INTEGER NOT NULL,address_id BIGINT NOT NULL,status VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL,order_id BIGINT NOT NULL,user_id INT NOT NULL,phone VARCHAR(50),status VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL,address_name VARCHAR(100) NOT NULL)");
            stmt.execute("TRUNCATE TABLE t_order");
            stmt.execute("TRUNCATE TABLE t_order_item");
            stmt.execute("TRUNCATE TABLE t_address");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
