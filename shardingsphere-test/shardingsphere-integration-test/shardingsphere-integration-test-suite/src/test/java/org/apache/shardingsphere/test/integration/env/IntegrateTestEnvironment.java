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

package org.apache.shardingsphere.test.integration.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Integrate test running environment.
 */
@Getter
public final class IntegrateTestEnvironment {
    
    private static final IntegrateTestEnvironment INSTANCE = new IntegrateTestEnvironment();
    
    private final Collection<String> adapters;
    
    private final boolean runAdditionalTestCases;
    
    private final Collection<String> scenarios;
    
    private final Map<DatabaseType, DatabaseEnvironment> databaseEnvironments;

    private final Map<String, DatabaseEnvironment> proxyEnvironments;
    
    private IntegrateTestEnvironment() {
        Properties envProps = loadProperties();
        adapters = Splitter.on(",").trimResults().splitToList(envProps.getProperty("it.adapters"));
        runAdditionalTestCases = Boolean.parseBoolean(envProps.getProperty("it.run.additional.cases"));
        scenarios = Splitter.on(",").trimResults().splitToList(envProps.getProperty("it.scenarios"));
        databaseEnvironments = createDatabaseEnvironments(envProps);
        proxyEnvironments = createProxyEnvironments(envProps);
    }
    
    @SuppressWarnings("AccessOfSystemProperties")
    private Properties loadProperties() {
        Properties result = new Properties();
        try {
            result.load(IntegrateTestEnvironment.class.getClassLoader().getResourceAsStream("env/env.properties"));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    private Map<DatabaseType, DatabaseEnvironment> createDatabaseEnvironments(final Properties envProps) {
        Collection<DatabaseType> databaseTypes = Arrays.stream(
                envProps.getProperty("it.databases", "H2").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toList());
        Map<DatabaseType, DatabaseEnvironment> result = new LinkedHashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            result.put(each, createDatabaseEnvironment(each, envProps));
        }
        return result;
    }
    
    private DatabaseEnvironment createDatabaseEnvironment(final DatabaseType databaseType, final Properties envProps) {
        switch (databaseType.getName()) {
            case "H2":
                return new DatabaseEnvironment(databaseType, "", 0, "sa", "");
            case "MySQL":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("it.mysql.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("it.mysql.port", "3306")),
                        envProps.getProperty("it.mysql.username", "root"), envProps.getProperty("it.mysql.password", ""));
            case "PostgreSQL":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("it.postgresql.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("it.postgresql.port", "5432")),
                        envProps.getProperty("it.postgresql.username", "postgres"), envProps.getProperty("it.postgresql.password", ""));
            case "SQLServer":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("it.sqlserver.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("it.sqlserver.port", "1433")),
                        envProps.getProperty("it.sqlserver.username", "sa"), envProps.getProperty("it.sqlserver.password", "Jdbc1234"));
            case "Oracle":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("it.oracle.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("it.oracle.port", "1521")),
                        envProps.getProperty("it.oracle.username", "jdbc"), envProps.getProperty("it.oracle.password", "jdbc"));
            default:
                throw new UnsupportedOperationException(databaseType.getName());
        }
    }
    
    private Map<String, DatabaseEnvironment> createProxyEnvironments(final Properties envProps) {
        Map<String, DatabaseEnvironment> result = new HashMap<>(scenarios.size(), 1);
        for (String each : scenarios) {
            // TODO hard code for MySQL, should configurable
            result.put(each, createProxyEnvironment(envProps, each));
        }
        return result;
    }
    
    private DatabaseEnvironment createProxyEnvironment(final Properties envProps, final String scenario) {
        String host = envProps.getProperty(String.format("it.proxy.%s.host", scenario), "127.0.0.1");
        int port = Integer.parseInt(envProps.getProperty(String.format("it.proxy.%s.port", scenario), "3307"));
        String username = envProps.getProperty(String.format("it.proxy.%s.username", scenario), "root");
        String password = envProps.getProperty(String.format("it.proxy.%s.password", scenario), "root");
        return new DatabaseEnvironment(new MySQLDatabaseType(), host, port, username, password);
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestEnvironment getInstance() {
        if (INSTANCE.adapters.contains("proxy")) {
            for (String each : INSTANCE.scenarios) {
                waitForProxyReady(each);
            }
        }
        return INSTANCE;
    }
    
    private static void waitForProxyReady(final String scenario) {
        int retryCount = 0;
        while (!isProxyReady(scenario) && retryCount < 30) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    @SuppressWarnings("CallToDriverManagerGetConnection")
    private static boolean isProxyReady(final String scenario) {
        DatabaseEnvironment dbEnv = INSTANCE.proxyEnvironments.get(scenario);
        try (Connection connection = DriverManager.getConnection(dbEnv.getURL(scenario), dbEnv.getUsername(), dbEnv.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException ignore) {
            return false;
        }
        return true;
    }
}
