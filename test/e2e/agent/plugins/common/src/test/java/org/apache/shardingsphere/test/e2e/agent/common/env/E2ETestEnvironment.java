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

package org.apache.shardingsphere.test.e2e.agent.common.env;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Slf4j
public final class E2ETestEnvironment {
    
    private static final E2ETestEnvironment INSTANCE = new E2ETestEnvironment();
    
    private final Properties props;
    
    private final boolean isEnvironmentPrepared;
    
    private DataSource dataSource;
    
    private boolean isInitialized;
    
    private boolean isAdaptedProxy;
    
    private String adapter;
    
    private final AtomicBoolean prepareFlag = new AtomicBoolean();
    
    private E2ETestEnvironment() {
        props = EnvironmentProperties.loadProperties("env/engine-env.properties");
        isEnvironmentPrepared = props.getProperty("it.env.value").equals(props.getProperty("it.env.type"));
        adapter = props.getProperty("it.env.adapter", "proxy");
        isAdaptedProxy = "proxy".equalsIgnoreCase(adapter);
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static E2ETestEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * Prepare environment.
     */
    public void prepareEnvironment() {
        if (!prepareFlag.compareAndSet(false, true)) {
            return;
        }
        if (isAdaptedProxy()) {
            createDataSource();
            isInitialized = null != dataSource;
        } else {
            isInitialized = waitForJdbcEnvironmentReady();
        }
    }
    
    private void createDataSource() {
        if (isEnvironmentPrepared && null == dataSource) {
            if (waitForProxyEnvironmentReady(props)) {
                dataSource = createHikariCP(props);
            }
        }
    }
    
    private boolean waitForProxyEnvironmentReady(final Properties props) {
        log.info("Proxy with agent environment initializing ...");
        try {
            Awaitility.await().atMost(2L, TimeUnit.MINUTES).pollInterval(5L, TimeUnit.SECONDS).until(() -> isProxyReady(props));
        } catch (final ConditionTimeoutException ignored) {
            log.info("Proxy with agent environment initialization failed ...");
            return false;
        }
        log.info("Proxy with agent environment initialized successfully ...");
        return true;
    }
    
    private boolean isProxyReady(final Properties props) {
        log.info("Try to connect proxy ...");
        String url = props.getProperty("proxy.url");
        String username = props.getProperty("proxy.username", "root");
        String password = props.getProperty("proxy.password", "root");
        try (
                Connection connection = DriverManager.getConnection(url, username, password);
                Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException ignored) {
            return false;
        }
        return true;
    }
    
    private DataSource createHikariCP(final Properties props) {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName("com.mysql.cj.jdbc.Driver");
        result.setJdbcUrl(props.getProperty("proxy.url"));
        result.setUsername(props.getProperty("proxy.username", "root"));
        result.setPassword(props.getProperty("proxy.password", "root"));
        result.setMaximumPoolSize(5);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
    
    private boolean waitForJdbcEnvironmentReady() {
        log.info("Jdbc project with agent environment initializing ...");
        try {
            Awaitility.await().atMost(2L, TimeUnit.MINUTES).pollInterval(5L, TimeUnit.SECONDS).until(this::isJdbcReady);
        } catch (final ConditionTimeoutException ignored) {
            log.info("Jdbc project with agent environment initialization failed ...");
            return false;
        }
        log.info("Jdbc project with agent environment initialized successfully ...");
        return true;
    }
    
    private boolean isJdbcReady() {
        log.info("Wait for the jdbc project environment to be ready ...");
        String logPath = String.join(File.separator, Paths.get("").toAbsolutePath().toString(), String.join(File.separator, "target", "jdbc", "logs", "stdout.log"));
        return new File(logPath).exists();
    }
}
