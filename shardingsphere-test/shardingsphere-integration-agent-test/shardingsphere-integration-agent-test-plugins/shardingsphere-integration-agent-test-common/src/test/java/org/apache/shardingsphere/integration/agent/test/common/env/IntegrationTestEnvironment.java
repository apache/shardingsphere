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

package org.apache.shardingsphere.integration.agent.test.common.env;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final boolean isEnvironmentPrepared;
    
    private DataSource dataSource;
    
    private Properties engineEnvProps;
    
    @SneakyThrows
    private IntegrationTestEnvironment() {
        engineEnvProps = EnvironmentProperties.loadProperties("env/engine-env.properties");
        isEnvironmentPrepared = engineEnvProps.getProperty("it.env.value").equals(engineEnvProps.getProperty("it.env.type"));
        if (isEnvironmentPrepared) {
            waitForEnvironmentReady(engineEnvProps);
            dataSource = createHikariCP(engineEnvProps);
        }
    }
    
    private static DataSource createHikariCP(final Properties engineEnvProps) {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl(engineEnvProps.getProperty("proxy.url"));
        result.setUsername(engineEnvProps.getProperty("proxy.username", "root"));
        result.setPassword(engineEnvProps.getProperty("proxy.password", "root"));
        result.setMaximumPoolSize(5);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
    
    private void waitForEnvironmentReady(final Properties engineEnvProps) {
        log.info("wait begin proxy environment");
        int retryCount = 0;
        while (!isProxyReady(engineEnvProps) && retryCount < Integer.parseInt(engineEnvProps.getProperty("proxy.retry", "30"))) {
            try {
                Thread.sleep(Long.parseLong(engineEnvProps.getProperty("proxy.waitMs", "1000")));
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    private boolean isProxyReady(final Properties engineEnvProps) {
        String url = engineEnvProps.getProperty("proxy.url");
        String username = engineEnvProps.getProperty("proxy.username", "root");
        String password = engineEnvProps.getProperty("proxy.password", "root");
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException ignore) {
            return false;
        }
        log.info(" it proxy environment success");
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
