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
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertNotNull;

/**
 * Scenario path.
 */
@RequiredArgsConstructor
public final class ScenarioPath {
    
    private static final String ROOT_PATH = "env/scenario";
    
    private static final String DATABASES_FILE = "databases.xml";
    
    private static final String INIT_SQL_PATH = "init-sql";
    
    private static final String INIT_SQL_FILE = "init.sql";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String RULE_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    private final String scenario;
    
    /**
     * Check folder exist.
     */
    public void checkFolderExist() {
        String scenarioDirectory = String.join("/", ROOT_PATH, scenario);
        assertNotNull(String.format("Scenario folder `%s` must exist.", scenarioDirectory), ScenarioPath.class.getClassLoader().getResource(scenarioDirectory));
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
     * Get init SQL files.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @return init SQL files
     */
    public Collection<String> getInitSQLFiles(final String databaseName, final DatabaseType databaseType) {
        Collection<String> result = new LinkedList<>();
        result.add(getInitSQLFile(databaseType, INIT_SQL_FILE));
        String dbInitSQLFileName = "init-" + databaseName + ".sql";
        if (isInitSQLFileExist(databaseType, dbInitSQLFileName)) {
            result.add(getInitSQLFile(databaseType, dbInitSQLFileName));
        }
        return result;
    }
    
    private String getInitSQLFile(final DatabaseType databaseType, final String fileName) {
        String resourceFile = getInitSQLResourceFile(databaseType, fileName);
        URL url = ScenarioPath.class.getClassLoader().getResource(resourceFile);
        assertNotNull(String.format("File `%s` must exist.", resourceFile), url);
        return url.getFile();
    }
    
    private String getInitSQLResourceFile(final DatabaseType databaseType, final String fileName) {
        return String.join("/", ROOT_PATH, scenario, INIT_SQL_PATH, databaseType.getName().toLowerCase(), fileName);
    }
    
    private boolean isInitSQLFileExist(final DatabaseType databaseType, final String fileName) {
        return null != ScenarioPath.class.getClassLoader().getResource(getInitSQLResourceFile(databaseType, fileName));
    }
    
    /**
     * Get init SQL resource path.
     *
     * @param databaseType database type
     * @return init SQL resource path
     */
    public String getInitSQLResourcePath(final DatabaseType databaseType) {
        return String.join("/", "", ROOT_PATH, scenario, INIT_SQL_PATH, databaseType.getName().toLowerCase());
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
     * Get rule configuration file.
     *
     * @return rule configuration file
     */
    public String getRuleConfigurationFile() {
        return getFile(RULE_CONFIG_FILE);
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
        URL url = ScenarioPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
}
