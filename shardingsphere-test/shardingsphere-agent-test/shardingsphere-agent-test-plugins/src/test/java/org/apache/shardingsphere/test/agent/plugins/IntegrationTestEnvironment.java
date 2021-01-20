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

package org.apache.shardingsphere.test.agent.plugins;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.h2.tools.RunScript;

import static org.junit.Assert.assertNotNull;

@Getter
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private static final String URL = "jdbc:mysql://127.0.0.1:33070?serverTimezone=UTC&useSSL=false&useLocalSessionState=true&characterEncoding=utf-8";
    
    private static final String USER_NAME = "root";
    
    private static final String PASSWORD = "";
    
    private DataSource dataSource;
    
    @SneakyThrows
    private IntegrationTestEnvironment() {
        waitForEnvironmentReady();
        dataSource = createHikariCP();
        File file = new File(getFile());
        executeSQLScript(dataSource, file);
    }
    
    private static String getFile() {
        String path = String.join("/", "env", "init.sql");
        java.net.URL url = IntegrationTestEnvironment.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
    
    private static void executeSQLScript(final DataSource dataSource, final File file) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection();
             FileReader reader = new FileReader(file)) {
            RunScript.execute(connection, reader);
        }
    }
    
    private static DataSource createHikariCP() {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl(URL);
        result.setUsername(USER_NAME);
        result.setPassword(PASSWORD);
        result.setMaximumPoolSize(5);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
    
    private void waitForEnvironmentReady() {
        int retryCount = 0;
        while (!isProxyReady() && retryCount < 30) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    private boolean isProxyReady() {
        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException ignore) {
            return false;
        }
        return true;
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
}
