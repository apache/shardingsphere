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

public class OpenGaussTest {
    
    private static final String USERNAME = "gaussdb";
    
    private static final String PASSWORD = "openGauss@123";
    
    private static final String DATABASE = "postgres";
    
    private static final String JDBC_URL = "jdbc:opengauss://localhost:62390/" + DATABASE;
    
    private static Process process;
    
    private AbstractShardingCommonTest abstractShardingCommonTest;
    
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        beforeAll();
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/databases/opengauss.yaml"));
        abstractShardingCommonTest = new AbstractShardingCommonTest(dataSource);
        this.initEnvironment();
        abstractShardingCommonTest.processSuccess();
        abstractShardingCommonTest.cleanEnvironment();
        tearDown();
    }
    
    private void initEnvironment() throws SQLException {
        abstractShardingCommonTest.getOrderRepository().createTableIfNotExistsInPostgres();
        abstractShardingCommonTest.getOrderItemRepository().createTableIfNotExistsInPostgres();
        abstractShardingCommonTest.getAddressRepository().createTableIfNotExists();
        abstractShardingCommonTest.getOrderRepository().truncateTable();
        abstractShardingCommonTest.getOrderItemRepository().truncateTable();
        abstractShardingCommonTest.getAddressRepository().truncateTable();
    }
    
    private static Connection openConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        return DriverManager.getConnection(JDBC_URL, props);
    }
    
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private static void beforeAll() throws IOException {
        System.out.println("Starting OpenGauss ...");
        process = new ProcessBuilder(
                "docker", "run", "--rm", "-p", "62390:5432", "-e", "GS_PASSWORD=" + PASSWORD,
                "opengauss/opengauss:5.0.0")
                        .redirectOutput(new File("target/opengauss-stdout.txt"))
                        .redirectError(new File("target/opengauss-stderr.txt"))
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
        System.out.println("OpenGauss started");
    }
    
    private static void tearDown() {
        if (null != process && process.isAlive()) {
            System.out.println("Shutting down OpenGauss");
            process.destroy();
        }
    }
}
