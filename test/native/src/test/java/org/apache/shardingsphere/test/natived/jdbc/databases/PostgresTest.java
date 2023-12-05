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

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.natived.jdbc.commons.AbstractShardingCommonTest;
import org.apache.shardingsphere.test.natived.jdbc.commons.FileTestUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

public class PostgresTest {
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private static final String DATABASE = "test";
    
    private static final String JDBC_URL = "jdbc:postgresql://localhost:49965/" + DATABASE;
    
    private static Process process;
    
    private AbstractShardingCommonTest abstractShardingCommonTest;
    
    private static Connection openConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        return DriverManager.getConnection(JDBC_URL, props);
    }
    
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @BeforeAll
    static void beforeAll() throws IOException {
        System.out.println("Starting PostgreSQL ...");
        process = new ProcessBuilder(
                "docker", "run", "--rm", "-p", "49965:5432", "-e", "POSTGRES_DB=" + DATABASE, "-e", "POSTGRES_USER=" + USERNAME,
                "-e", "POSTGRES_PASSWORD=" + PASSWORD, "postgres:16.1-bookworm")
                        .redirectOutput(new File("target/test-classes/postgres-stdout.txt"))
                        .redirectError(new File("target/test-classes/postgres-stderr.txt"))
                        .start();
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
        System.out.println("PostgreSQL started");
    }
    
    @AfterAll
    static void tearDown() {
        if (process != null && process.isAlive()) {
            System.out.println("Shutting down PostgreSQL");
            process.destroy();
        }
    }
    
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/databases/postgresql.yaml"));
        abstractShardingCommonTest = new AbstractShardingCommonTest(dataSource);
        this.initEnvironment();
        abstractShardingCommonTest.processSuccess();
        abstractShardingCommonTest.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        abstractShardingCommonTest.getOrderRepository().createTableIfNotExistsInPostgres();
        abstractShardingCommonTest.getOrderItemRepository().createTableIfNotExistsInPostgres();
        abstractShardingCommonTest.getAddressRepository().createTableIfNotExists();
        abstractShardingCommonTest.getOrderRepository().truncateTable();
        abstractShardingCommonTest.getOrderItemRepository().truncateTable();
        abstractShardingCommonTest.getAddressRepository().truncateTable();
    }
}
