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
import org.apache.shardingsphere.test.integration.env.props.DatabaseScenarioProperties;
import org.apache.shardingsphere.test.integration.env.props.EnvironmentProperties;

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
 * Integration test running environment.
 */
@Getter
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final boolean isEnvironmentPrepared;
    
    private final Collection<String> adapters;
    
    private final boolean runAdditionalTestCases;
    
    private final Collection<String> scenarios;
    
    private final Map<DatabaseType, Map<String, DatabaseEnvironment>> databaseEnvironments;
    
    private final Map<String, DatabaseEnvironment> proxyEnvironments;
    
    private IntegrationTestEnvironment() {
        Properties engineEnvProps = EnvironmentProperties.loadProperties("env/engine-env.properties");
        isEnvironmentPrepared = "docker".equals(engineEnvProps.getProperty("it.env.type"));
        adapters = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.adapters"));
        runAdditionalTestCases = Boolean.parseBoolean(engineEnvProps.getProperty("it.run.additional.cases"));
        scenarios = getScenarios(engineEnvProps);
        Map<String, DatabaseScenarioProperties> databaseProps = getDatabaseScenarioProperties();
        databaseEnvironments = createDatabaseEnvironments(getDatabaseTypes(engineEnvProps), databaseProps);
        proxyEnvironments = createProxyEnvironments(databaseProps);
        if (isEnvironmentPrepared) {
            for (String each : scenarios) {
                waitForEnvironmentReady(each);
            }
        }
    }
    
    private Collection<String> getScenarios(final Properties engineEnvProps) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.scenarios"));
        for (String each : result) {
            EnvironmentPath.assertScenarioDirectoryExisted(each);
        }
        return result;
    }
    
    private Map<String, DatabaseScenarioProperties> getDatabaseScenarioProperties() {
        Map<String, DatabaseScenarioProperties> result = new HashMap<>(scenarios.size(), 1);
        for (String each : scenarios) {
            result.put(each, new DatabaseScenarioProperties(each, EnvironmentProperties.loadProperties(String.format("env/%s/scenario-env.properties", each))));
        }
        return result;
    }
    
    private Collection<DatabaseType> getDatabaseTypes(final Properties engineEnvProps) {
        return Arrays.stream(engineEnvProps.getProperty("it.databases", "H2").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toList());
    }
    
    private Map<DatabaseType, Map<String, DatabaseEnvironment>> createDatabaseEnvironments(final Collection<DatabaseType> databaseTypes, final Map<String, DatabaseScenarioProperties> databaseProps) {
        Map<DatabaseType, Map<String, DatabaseEnvironment>> result = new LinkedHashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            Map<String, DatabaseEnvironment> databaseEnvironments = new LinkedHashMap<>(scenarios.size(), 1);
            for (String scenario : scenarios) {
                databaseEnvironments.put(scenario, createDatabaseEnvironment(each, databaseProps.get(scenario)));
                result.put(each, databaseEnvironments);
            }
        }
        return result;
    }
    
    private DatabaseEnvironment createDatabaseEnvironment(final DatabaseType databaseType, final DatabaseScenarioProperties databaseProps) {
        if ("H2".equals(databaseType.getName())) {
            return new DatabaseEnvironment(databaseType, "", 0, "sa", ""); 
        }
        return new DatabaseEnvironment(databaseType, databaseProps.getDatabaseHost(databaseType), 
                databaseProps.getDatabasePort(databaseType), databaseProps.getDatabaseUsername(databaseType), databaseProps.getDatabasePassword(databaseType));
    }
    
    private Map<String, DatabaseEnvironment> createProxyEnvironments(final Map<String, DatabaseScenarioProperties> databaseProps) {
        Map<String, DatabaseEnvironment> result = new HashMap<>(scenarios.size(), 1);
        for (String each : scenarios) {
            result.put(each, createProxyEnvironment(databaseProps.get(each)));
        }
        return result;
    }
    
    private DatabaseEnvironment createProxyEnvironment(final DatabaseScenarioProperties databaseProps) {
        // TODO hard code for MySQL, should configurable
        return new DatabaseEnvironment(new MySQLDatabaseType(), databaseProps.getProxyHost(), databaseProps.getProxyPort(), databaseProps.getProxyUsername(), databaseProps.getProxyPassword());
    }
    
    private void waitForEnvironmentReady(final String scenario) {
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
    private boolean isProxyReady(final String scenario) {
        DatabaseEnvironment dbEnv = proxyEnvironments.get(scenario);
        try (Connection connection = DriverManager.getConnection(dbEnv.getURL(scenario), dbEnv.getUsername(), dbEnv.getPassword());
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
