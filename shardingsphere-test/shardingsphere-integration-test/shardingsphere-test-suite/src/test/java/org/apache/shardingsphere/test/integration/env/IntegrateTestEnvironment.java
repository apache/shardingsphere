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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Integrate test running environment.
 */
@Getter
public final class IntegrateTestEnvironment {
    
    private static final IntegrateTestEnvironment INSTANCE = new IntegrateTestEnvironment();
    
    private final boolean runAdditionalTestCases;
    
    private final Collection<String> ruleTypes;
    
    private final Collection<DatabaseType> databaseTypes;
    
    private final Map<DatabaseType, DatabaseEnvironment> databaseEnvironments;
    
    private final String activeProfile;
    
    private IntegrateTestEnvironment() {
        activeProfile = loadProperties("integrate/profile.properties").getProperty("mode");
        Properties envProps = loadProperties(IntegrateTestEnvironmentType.valueFromProfileName(activeProfile).getEnvFileName());
        runAdditionalTestCases = Boolean.parseBoolean(envProps.getProperty("run.additional.cases"));
        ruleTypes = Splitter.on(",").trimResults().splitToList(envProps.getProperty("rule.types"));
        databaseTypes = new LinkedList<>();
        for (String each : envProps.getProperty("databases", "H2").split(",")) {
            databaseTypes.add(DatabaseTypeRegistry.getActualDatabaseType(each.trim()));
        }
        databaseEnvironments = new HashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            switch (each.getName()) {
                case "H2":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, "", 0, "sa", ""));
                    break;
                case "MySQL":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, envProps.getProperty("mysql.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("mysql.port", "3306")),
                        envProps.getProperty("mysql.username", "root"), envProps.getProperty("mysql.password", "")));
                    break;
                case "PostgreSQL":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, envProps.getProperty("postgresql.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("postgresql.port", "5432")),
                        envProps.getProperty("postgresql.username", "postgres"), envProps.getProperty("postgresql.password", "")));
                    break;
                case "SQLServer":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, envProps.getProperty("sqlserver.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("sqlserver.port", "1433")),
                        envProps.getProperty("sqlserver.username", "sa"), envProps.getProperty("sqlserver.password", "Jdbc1234")));
                    break;
                case "Oracle":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, envProps.getProperty("oracle.host", "127.0.0.1"), Integer.parseInt(envProps.getProperty("oracle.port", "1521")),
                        envProps.getProperty("oracle.username", "jdbc"), envProps.getProperty("oracle.password", "jdbc")));
                    break;
                default:
                    break;
            }
        }
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
