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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Getter
@Slf4j
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final Properties props;
    
    private final boolean isEnvironmentPrepared;
    
    private DataSource dataSource;
    
    private IntegrationTestEnvironment() {
        props = EnvironmentProperties.loadProperties("env/engine-env.properties");
        isEnvironmentPrepared = props.getProperty("it.env.value").equals(props.getProperty("it.env.type"));
        if (isEnvironmentPrepared) {
            waitForEnvironmentReady(props);
            dataSource = createHikariCP(props);
        }
    }
    
    private static DataSource createHikariCP(final Properties props) {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl(props.getProperty("proxy.url"));
        result.setUsername(props.getProperty("proxy.username", "root"));
        result.setPassword(props.getProperty("proxy.password", "root"));
        result.setMaximumPoolSize(5);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
    
    private void waitForEnvironmentReady(final Properties props) {
        log.info("wait begin proxy environment");
        int retryCount = 0;
        while (!isProxyReady(props) && retryCount < Integer.parseInt(props.getProperty("proxy.retry", "30"))) {
            try {
                Thread.sleep(Long.parseLong(props.getProperty("proxy.waitMs", "1000")));
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    private boolean isProxyReady(final Properties props) {
        String url = props.getProperty("proxy.url");
        String username = props.getProperty("proxy.username", "root");
        String password = props.getProperty("proxy.password", "root");
        try (
                Connection connection = DriverManager.getConnection(url, username, password);
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
