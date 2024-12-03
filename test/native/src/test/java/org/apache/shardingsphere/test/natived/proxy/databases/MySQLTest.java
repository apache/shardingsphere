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

package org.apache.shardingsphere.test.natived.proxy.databases;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.proxy.ProxyTestingServer;
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

@SuppressWarnings({"SqlNoDataSourceInspection", "SameParameterValue", "resource"})
@EnabledInNativeImage
@Testcontainers
class MySQLTest {
    
    @Container
    public static final GenericContainer<?> MYSQL_CONTAINER = new GenericContainer<>("mysql:9.1.0-oraclelinux9")
            .withEnv("MYSQL_ROOT_PASSWORD", "yourStrongPassword123!")
            .withExposedPorts(3306);
    
    private static ProxyTestingServer proxyTestingServer;
    
    private TestShardingService testShardingService;
    
    @BeforeAll
    static void beforeAll() throws SQLException {
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptionsMatching(CommunicationsException.class::isInstance).until(() -> {
            openConnection("root", "yourStrongPassword123!", "jdbc:mysql://127.0.0.1:" + MYSQL_CONTAINER.getMappedPort(3306))
                    .close();
            return true;
        });
        try (
                Connection connection = openConnection("root", "yourStrongPassword123!", "jdbc:mysql://127.0.0.1:" + MYSQL_CONTAINER.getMappedPort(3306));
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds_0");
            statement.executeUpdate("CREATE DATABASE demo_ds_1");
            statement.executeUpdate("CREATE DATABASE demo_ds_2");
        }
        String absolutePath = Paths.get("src/test/resources/test-native/yaml/proxy/databases/mysql").toAbsolutePath().toString();
        proxyTestingServer = new ProxyTestingServer(absolutePath);
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptionsMatching(CommunicationsException.class::isInstance).until(() -> {
            openConnection("root", "root", "jdbc:mysql://127.0.0.1:" + proxyTestingServer.getProxyPort()).close();
            return true;
        });
    }
    
    @AfterAll
    static void afterAll() {
        proxyTestingServer.close();
    }
    
    /**
     * {@link groovy.lang.Closure} related classes are not available on GraalVM Native Image.
     * This CLASS_BASE algorithm class is designed to emulate INLINE's {@code ds_${user_id % 2}}.
     * See <a href="https://github.com/oracle/graal/issues/5522">oracle/graal#5522</a> .
     *
     * @throws SQLException SQL Exception
     */
    @Test
    void assertShardingInLocalTransactions() throws SQLException {
        try (
                Connection connection = openConnection("root", "root", "jdbc:mysql://127.0.0.1:" + proxyTestingServer.getProxyPort());
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE sharding_db");
            statement.execute("USE sharding_db");
            statement.execute("REGISTER STORAGE UNIT ds_0 (\n"
                    + "  URL=\"jdbc:mysql://127.0.0.1:" + MYSQL_CONTAINER.getMappedPort(3306) + "/demo_ds_0\",\n"
                    + "  USER=\"root\",\n"
                    + "  PASSWORD=\"yourStrongPassword123!\"\n"
                    + "),ds_1 (\n"
                    + "  URL=\"jdbc:mysql://127.0.0.1:" + MYSQL_CONTAINER.getMappedPort(3306) + "/demo_ds_1\",\n"
                    + "  USER=\"root\",\n"
                    + "  PASSWORD=\"yourStrongPassword123!\"\n"
                    + "),ds_2 (\n"
                    + "  URL=\"jdbc:mysql://127.0.0.1:" + MYSQL_CONTAINER.getMappedPort(3306) + "/demo_ds_2\",\n"
                    + "  USER=\"root\",\n"
                    + "  PASSWORD=\"yourStrongPassword123!\"\n"
                    + ")");
            statement.execute("CREATE DEFAULT SHARDING DATABASE STRATEGY (\n"
                    + "  TYPE=\"standard\", \n"
                    + "  SHARDING_COLUMN=user_id, \n"
                    + "  SHARDING_ALGORITHM(\n"
                    + "    TYPE(\n"
                    + "      NAME=CLASS_BASED, \n"
                    + "      PROPERTIES(\n"
                    + "        \"strategy\"=\"STANDARD\",\n"
                    + "        \"algorithmClassName\"=\"org.apache.shardingsphere.test.natived.commons.algorithm.ClassBasedInlineShardingAlgorithmFixture\"\n"
                    + "      )\n"
                    + "    )\n"
                    + "  )\n"
                    + ")");
            statement.execute("CREATE SHARDING TABLE RULE t_order (\n"
                    + "  DATANODES(\"<LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order\"),\n"
                    + "  KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME=\"SNOWFLAKE\"))\n"
                    + "), t_order_item (\n"
                    + "  DATANODES(\"<LITERAL>ds_0.t_order_item, ds_1.t_order_item, ds_2.t_order_item\"),\n"
                    + "  KEY_GENERATE_STRATEGY(COLUMN=order_item_id,TYPE(NAME=\"SNOWFLAKE\"))\n"
                    + ")");
            statement.execute("CREATE BROADCAST TABLE RULE t_address");
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:" + proxyTestingServer.getProxyPort() + "/sharding_db");
        config.setUsername("root");
        config.setPassword("root");
        DataSource dataSource = new HikariDataSource(config);
        testShardingService = new TestShardingService(dataSource);
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
    
    private static Connection openConnection(final String username, final String password, final String jdbcUrl) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        return DriverManager.getConnection(jdbcUrl, props);
    }
}
