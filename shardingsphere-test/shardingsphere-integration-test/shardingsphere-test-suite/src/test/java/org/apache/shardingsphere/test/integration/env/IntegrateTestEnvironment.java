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
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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
    
    private final String activeProfile;

    private final Collection<String> adapters;
    
    private final boolean runAdditionalTestCases;
    
    private final Collection<String> scenarios;
    
    private final Map<DatabaseType, DatabaseEnvironment> databaseEnvironments;
    
    private IntegrateTestEnvironment() {
        activeProfile = loadProperties("integrate/profile.properties").getProperty("mode");
        Properties envProps = loadProperties(IntegrateTestEnvironmentType.valueFromProfileName(activeProfile).getEnvFileName());
        adapters = Splitter.on(",").trimResults().splitToList(envProps.getProperty("adapters"));
        runAdditionalTestCases = Boolean.parseBoolean(envProps.getProperty("run.additional.cases"));
        scenarios = Splitter.on(",").trimResults().splitToList(envProps.getProperty("scenarios"));
        databaseEnvironments = createDatabaseEnvironments(envProps);
    }
    
    private Properties loadProperties(final String fileName) {
        Properties result = new Properties();
        try {
            result.load(IntegrateTestEnvironment.class.getClassLoader().getResourceAsStream(fileName));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }
    
    private Map<DatabaseType, DatabaseEnvironment> createDatabaseEnvironments(final Properties envProps) {
        Collection<DatabaseType> databaseTypes = Arrays.stream(
                envProps.getProperty("databases", "H2").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toList());
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
                return new DatabaseEnvironment(databaseType, envProps.getProperty("mysql.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("mysql.port", "3306")),
                        envProps.getProperty("mysql.username", "root"), envProps.getProperty("mysql.password", ""));
            case "PostgreSQL":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("postgresql.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("postgresql.port", "5432")),
                        envProps.getProperty("postgresql.username", "postgres"), envProps.getProperty("postgresql.password", ""));
            case "SQLServer":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("sqlserver.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("sqlserver.port", "1433")),
                        envProps.getProperty("sqlserver.username", "sa"), envProps.getProperty("sqlserver.password", "Jdbc1234"));
            case "Oracle":
                return new DatabaseEnvironment(databaseType, envProps.getProperty("oracle.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("oracle.port", "1521")),
                        envProps.getProperty("oracle.username", "jdbc"), envProps.getProperty("oracle.password", "jdbc"));
            default:
                throw new UnsupportedOperationException(databaseType.getName());
        }
    }
    
    /**
     * Judge whether proxy environment.
     *
     * @return is proxy environment or not 
     */
    public boolean isProxyEnvironment() {
        return "proxy".equals(activeProfile);
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestEnvironment getInstance() {
        return INSTANCE;
    }
}
