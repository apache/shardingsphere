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

package org.apache.shardingsphere.test.integration.env.scenario;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Scenario environment path.
 */
@RequiredArgsConstructor
public final class ScenarioEnvironmentPath {
    
    private static final String ROOT_PATH = "env/scenario";
    
    private static final String DATABASES_FILE = "databases.xml";
    
    private static final String INIT_SQL_FILE = "init.sql";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String RULES_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    private final String scenario;
    
    /**
     * Check directory exist.
     */
    public void checkDirectoryExist() {
        String scenarioDirectory = String.join("/", ROOT_PATH, scenario);
        URL url = ScenarioEnvironmentPath.class.getClassLoader().getResource(scenarioDirectory);
        assertNotNull(String.format("Scenario directory `%s` must exist.", scenarioDirectory), url);
    }
    
    /**
     * Get databases file.
     * 
     * @return databases file
     */
    public String getDatabasesFile() {
        return getFile(DATABASES_FILE);
    }
    
    /**
     * Get init SQL resource path.
     *
     * @param databaseType database type
     * @return init SQL resource path
     */
    public String getInitSQLResourcePath(final DatabaseType databaseType) {
        return String.join("/", "", ROOT_PATH, scenario, "init-sql", databaseType.getName().toLowerCase());
    }
    
    /**
     * Get init SQL file.
     *
     * @param databaseType database type
     * @return init SQL file
     */
    public String getInitSQLFile(final DatabaseType databaseType) {
        return getFile(databaseType, INIT_SQL_FILE);
    }
    
    /**
     * Get init SQL file.
     *
     * @param databaseType database type
     * @param fileName file name
     * @return init SQL file
     */
    public String getInitSQLFile(final DatabaseType databaseType, final String fileName) {
        return getFile(databaseType, fileName);
    }
    
    /**
     * Get data set file.
     *
     * @return data set file
     */
    public String getDataSetFile() {
        return getFile(DATASET_FILE);
    }
    
    /**
     * Get rules configuration file.
     *
     * @return rules configuration file
     */
    public String getRulesConfigurationFile() {
        return getFile(RULES_CONFIG_FILE);
    }
    
    /**
     * Get authority file.
     *
     * @return authority file
     */
    public String getAuthorityFile() {
        return getFile(AUTHORITY_FILE);
    }
    
    private String getFile(final String fileName) {
        String path = String.join("/", ROOT_PATH, scenario, fileName);
        URL url = ScenarioEnvironmentPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
    
    private String getFile(final DatabaseType databaseType, final String fileName) {
        String path = getPath(databaseType, fileName);
        URL url = ScenarioEnvironmentPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
    
    private String getPath(final DatabaseType databaseType, final String fileName) {
        return String.join("/", ROOT_PATH, scenario, "init-sql", databaseType.getName().toLowerCase(), fileName);
    }
    
    /**
     * check SQL file exist.
     * 
     * @param databaseType database type
     * @param fileName file name
     * @return weather SQL file exist or not
     */
    public boolean checkSQLFileExist(final DatabaseType databaseType, final String fileName) {
        return null != ScenarioEnvironmentPath.class.getClassLoader().getResource(getPath(databaseType, fileName));
    }
}
