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

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.natived.jdbc.commons.FileTestUtils;
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

/**
 * Unable to use `org.testcontainers:mysql:1.19.3` under GraalVM Native Image.
 * Background comes from <a href="https://github.com/testcontainers/testcontainers-java/issues/7954">testcontainers/testcontainers-java#7954</a>.
 */
class MySQLTest {
    
    private static final Integer MYSQL_PORT_ON_HOST = 60107;
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private static final String DATABASE = "test";
    
    private static final String JDBC_URL = "jdbc:mysql://localhost:" + MYSQL_PORT_ON_HOST + "/" + DATABASE;
    
    private TestShardingService testShardingService;
    
    @SuppressWarnings("resource")
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        try (
                GenericContainer<?> mySQLContainer = new GenericContainer<>(DockerImageName.parse("mysql:8.2.0-oracle"))
                        .withEnv("MYSQL_DATABASE", DATABASE)
                        .withEnv("MYSQL_ROOT_PASSWORD", PASSWORD)
                        .withCreateContainerCmdModifier(
                                cmd -> cmd.withHostConfig(new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(MYSQL_PORT_ON_HOST), new ExposedPort(3306)))))) {
            mySQLContainer.start();
            beforeAll();
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/databases/mysql.yaml"));
            testShardingService = new TestShardingService(dataSource);
            this.initEnvironment();
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
    
    private static Connection openConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        return DriverManager.getConnection(JDBC_URL, props);
    }
    
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private void beforeAll() {
        Awaitility.await().atMost(Duration.ofMinutes(1)).ignoreExceptionsMatching(e -> e instanceof CommunicationsException)
                .until(() -> {
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
    }
}
