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

package org.apache.shardingsphere.dbtest.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.dbtest.env.datasource.DatabaseEnvironment;
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
        activeProfile = loadActiveProfile();
        Properties prop = new Properties();
        try {
            prop.load(IntegrateTestEnvironment.class.getClassLoader().getResourceAsStream(getEnvironmentProperties()));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        runAdditionalTestCases = Boolean.parseBoolean(prop.getProperty("run.additional.cases"));
        ruleTypes = Splitter.on(",").trimResults().splitToList(prop.getProperty("rule.types"));
        databaseTypes = new LinkedList<>();
        for (String each : prop.getProperty("databases", "H2").split(",")) {
            databaseTypes.add(DatabaseTypes.getActualDatabaseType(each.trim()));
        }
        databaseEnvironments = new HashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            switch (each.getName()) {
                case "H2":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, "", 0, "sa", ""));
                    break;
                case "MySQL":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, prop.getProperty("mysql.host", "127.0.0.1"), Integer.parseInt(prop.getProperty("mysql.port", "3306")),
                        prop.getProperty("mysql.username", "root"), prop.getProperty("mysql.password", "")));
                    break;
                case "PostgreSQL":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, prop.getProperty("postgresql.host", "127.0.0.1"), Integer.parseInt(prop.getProperty("postgresql.port", "5432")),
                        prop.getProperty("postgresql.username", "postgres"), prop.getProperty("postgresql.password", "")));
                    break;
                case "SQLServer":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, prop.getProperty("sqlserver.host", "127.0.0.1"), Integer.parseInt(prop.getProperty("sqlserver.port", "1433")),
                        prop.getProperty("sqlserver.username", "sa"), prop.getProperty("sqlserver.password", "Jdbc1234")));
                    break;
                case "Oracle":
                    databaseEnvironments.put(each, new DatabaseEnvironment(each, prop.getProperty("oracle.host", "127.0.0.1"), Integer.parseInt(prop.getProperty("oracle.port", "1521")),
                        prop.getProperty("oracle.username", "jdbc"), prop.getProperty("oracle.password", "jdbc")));
                    break;
                default:
                    break;
            }
        }
    }
    
    private String loadActiveProfile() {
        Properties prop = new Properties();
        try {
            prop.load(IntegrateTestEnvironment.class.getClassLoader().getResourceAsStream("integrate/profile.properties"));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        return prop.getProperty("mode");
    }
    
    private String getEnvironmentProperties() {
        if ("jdbc-ci".equals(activeProfile)) {
            return "integrate/env-jdbc-ci.properties";
        }
        if ("proxy".equals(activeProfile)) {
            return "integrate/env-proxy.properties";
        }
        return "integrate/env-jdbc-local.properties";
    }
    
    /**
     * Is proxy environment.
     *
     * @return true or false
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
