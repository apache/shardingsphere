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

public class MySQLTest {
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private static final String DATABASE = "test";
    
    private static final String JDBC_URL = "jdbc:mysql://localhost:65107/" + DATABASE;
    
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
        System.out.println("Starting MySQL ...");
        process = new ProcessBuilder(
                "docker", "run", "--rm", "-p", "65107:3306", "-e", "MYSQL_DATABASE=" + DATABASE,
                "-e", "MYSQL_ROOT_PASSWORD=" + PASSWORD, "mysql:8.2.0-oracle")
                        .redirectOutput(new File("target/test-classes/mysql-stdout.txt"))
                        .redirectError(new File("target/test-classes/mysql-stderr.txt"))
                        .start();
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
        System.out.println("MySQL started");
    }
    
    @AfterAll
    static void tearDown() {
        if (null != process && process.isAlive()) {
            System.out.println("Shutting down MySQL");
            process.destroy();
        }
    }
    
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/databases/mysql.yaml"));
        abstractShardingCommonTest = new AbstractShardingCommonTest(dataSource);
        this.initEnvironment();
        abstractShardingCommonTest.processSuccess();
        abstractShardingCommonTest.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        abstractShardingCommonTest.getOrderRepository().createTableIfNotExistsInMySQL();
        abstractShardingCommonTest.getOrderItemRepository().createTableIfNotExistsInMySQL();
        abstractShardingCommonTest.getAddressRepository().createTableIfNotExists();
        abstractShardingCommonTest.getOrderRepository().truncateTable();
        abstractShardingCommonTest.getOrderItemRepository().truncateTable();
        abstractShardingCommonTest.getAddressRepository().truncateTable();
    }
}
