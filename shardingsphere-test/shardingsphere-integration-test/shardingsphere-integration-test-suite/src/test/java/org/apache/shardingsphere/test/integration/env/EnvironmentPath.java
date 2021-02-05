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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Environment path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentPath {
    
    private static final String ROOT_PATH = "env";
    
    private static final String DATABASES_FILE = "databases.xml";
    
    private static final String INIT_SQL_FILE = "init.sql";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String RULES_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    /**
     * Assert scenario directory existed.
     * 
     * @param scenario scenario
     */
    public static void assertScenarioDirectoryExisted(final String scenario) {
        String scenarioDirectory = String.join("/", ROOT_PATH, scenario);
        URL url = EnvironmentPath.class.getClassLoader().getResource(scenarioDirectory);
        assertNotNull(String.format("Scenario directory `%s` must exist.", scenarioDirectory), url);
    }
    
    /**
     * Get databases file.
     * 
     * @param scenario scenario
     * @return databases file
     */
    public static String getDatabasesFile(final String scenario) {
        return getFile(scenario, DATABASES_FILE);
    }
    
    /**
     * Get init SQL file.
     *
     * @param databaseType database type
     * @param scenario scenario
     * @return init SQL file
     */
    public static String getInitSQLFile(final DatabaseType databaseType, final String scenario) {
        return getFile(databaseType, scenario, INIT_SQL_FILE);
    }
    
    /**
     * Get data set file.
     *
     * @param scenario scenario
     * @return data set file
     */
    public static String getDataSetFile(final String scenario) {
        return getFile(scenario, DATASET_FILE);
    }
    
    /**
     * Get rules configuration file.
     *
     * @param scenario scenario
     * @return rules configuration file
     */
    public static String getRulesConfigurationFile(final String scenario) {
        return getFile(scenario, RULES_CONFIG_FILE);
    }
    
    /**
     * Get authority file.
     *
     * @param scenario scenario
     * @return authority file
     */
    public static String getAuthorityFile(final String scenario) {
        return getFile(scenario, AUTHORITY_FILE);
    }
    
    private static String getFile(final String scenario, final String fileName) {
        String path = String.join("/", ROOT_PATH, scenario, fileName);
        URL url = EnvironmentPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
    
    private static String getFile(final DatabaseType databaseType, final String scenario, final String fileName) {
        String path = String.join("/", ROOT_PATH, scenario, "init-sql", databaseType.getName().toLowerCase(), fileName);
        URL url = EnvironmentPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
}
