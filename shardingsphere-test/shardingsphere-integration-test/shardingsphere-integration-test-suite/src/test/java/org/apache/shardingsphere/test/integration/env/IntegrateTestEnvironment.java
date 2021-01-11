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
    
    private final boolean isEnvironmentPrepared;
    
    private final Collection<String> adapters;
    
    private final boolean runAdditionalTestCases;
    
    private final Collection<String> scenarios;
    
    private final Map<DatabaseType, Map<String, DatabaseEnvironment>> databaseEnvironments;
    
    private final Map<String, DatabaseEnvironment> proxyEnvironments;
    
    private IntegrateTestEnvironment() {
        Properties engineEnvProps = EnvironmentProperties.loadProperties("env/engine-env.properties");
        isEnvironmentPrepared = "docker".equals(engineEnvProps.getProperty("it.env.type"));
        adapters = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.adapters"));
        runAdditionalTestCases = Boolean.parseBoolean(engineEnvProps.getProperty("it.run.additional.cases"));
        scenarios = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.scenarios"));
        databaseEnvironments = createDatabaseEnvironments(engineEnvProps);
        proxyEnvironments = createProxyEnvironments();
    }
    
    private Map<DatabaseType, Map<String, DatabaseEnvironment>> createDatabaseEnvironments(final Properties envProps) {
        Collection<DatabaseType> databaseTypes = Arrays.stream(
                envProps.getProperty("it.databases", "H2").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toList());
        Map<DatabaseType, Map<String, DatabaseEnvironment>> result = new LinkedHashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            Map<String, DatabaseEnvironment> databaseEnvironments = new LinkedHashMap<>(scenarios.size(), 1);
            for (String scenario : scenarios) {
                databaseEnvironments.put(scenario, createDatabaseEnvironment(each, scenario));
                result.put(each, databaseEnvironments);
            }
        }
        return result;
    }
    
    private DatabaseEnvironment createDatabaseEnvironment(final DatabaseType databaseType, final String scenario) {
        Properties envProps = EnvironmentProperties.loadProperties(String.format("env/%s/scenario-env.properties", scenario));
        switch (databaseType.getName()) {
            case "H2":
                return new DatabaseEnvironment(databaseType, "", 0, "sa", "");
            case "MySQL":
                return new DatabaseEnvironment(databaseType, envProps.getProperty(String.format("it.%s.mysql.host", scenario), "127.0.0.1"), 
                        Integer.parseInt(envProps.getProperty(String.format("it.%s.mysql.port", scenario), "3306")),
                        envProps.getProperty(String.format("it.%s.mysql.username", scenario), "root"), 
                        envProps.getProperty(String.format("it.%s.mysql.password", scenario), ""));
            case "PostgreSQL":
                return new DatabaseEnvironment(databaseType, envProps.getProperty(String.format("it.%s.postgresql.host", scenario), "127.0.0.1"), 
                        Integer.parseInt(envProps.getProperty(String.format("it.%s.postgresql.port", scenario), "5432")),
                        envProps.getProperty(String.format("it.%s.postgresql.username", scenario), "postgres"), 
                        envProps.getProperty(String.format("it.%s.postgresql.password", scenario), ""));
            case "SQLServer":
                return new DatabaseEnvironment(databaseType, envProps.getProperty(String.format("it.%s.sqlserver.host", scenario), "127.0.0.1"), 
                        Integer.parseInt(envProps.getProperty(String.format("it.%s.sqlserver.port", scenario), "1433")),
                        envProps.getProperty(String.format("it.%s.sqlserver.username", scenario), "sa"), 
                        envProps.getProperty(String.format("it.%s.sqlserver.password", scenario), "Jdbc1234"));
            case "Oracle":
                return new DatabaseEnvironment(databaseType, envProps.getProperty(String.format("it.%s.oracle.host", scenario), "127.0.0.1"), 
                        Integer.parseInt(envProps.getProperty(String.format("it.%s.oracle.port", scenario), "1521")),
                        envProps.getProperty(String.format("it.%s.oracle.username", scenario), "jdbc"),
                        envProps.getProperty(String.format("it.%s.oracle.password", scenario), "jdbc"));
            default:
                throw new UnsupportedOperationException(databaseType.getName());
        }
    }
    
    private Map<String, DatabaseEnvironment> createProxyEnvironments() {
        Map<String, DatabaseEnvironment> result = new HashMap<>(scenarios.size(), 1);
        for (String each : scenarios) {
            result.put(each, createProxyEnvironment(each));
        }
        return result;
    }
    
    private DatabaseEnvironment createProxyEnvironment(final String scenario) {
        Properties envProps = EnvironmentProperties.loadProperties(String.format("env/%s/scenario-env.properties", scenario));
        String host = envProps.getProperty(String.format("it.%s.proxy.host", scenario), "127.0.0.1");
        int port = Integer.parseInt(envProps.getProperty(String.format("it.%s.proxy.port", scenario), "3307"));
        String username = envProps.getProperty(String.format("it.%s.proxy.username", scenario), "root");
        String password = envProps.getProperty(String.format("it.%s.proxy.password", scenario), "root");
        // TODO hard code for MySQL, should configurable
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
