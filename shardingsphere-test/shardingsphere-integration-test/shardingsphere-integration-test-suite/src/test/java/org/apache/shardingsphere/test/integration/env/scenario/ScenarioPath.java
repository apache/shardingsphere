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

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.scenario.ScenarioDataPath.Type;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;

/**
 * Scenario path.
 */
public final class ScenarioPath {
    
    private static final String ROOT_PATH = "env/scenario";
    
    private static final String VERIFICATION_DATABASES_FILE = "data/verification/databases.xml";
    
    private static final String RULE_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    private final String scenario;
    
    private final ScenarioDataPath dataPath;
    
    public ScenarioPath(final String scenario) {
        this.scenario = scenario;
        dataPath = new ScenarioDataPath(scenario);
    }
    
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
        return dataPath.getDatabasesFile(Type.ACTUAL);
    }
    
    /**
     * Get verification databases file.
     *
     * @return verification databases file
     */
    public Optional<String> getVerificationDatabasesFile() {
        return isFileExist(VERIFICATION_DATABASES_FILE) ? Optional.of(dataPath.getDatabasesFile(Type.VERIFICATION)) : Optional.empty();
    }
    
    /**
     * Get init SQL files.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @return init SQL files
     */
    public Collection<String> getInitSQLFiles(final String databaseName, final DatabaseType databaseType) {
        return dataPath.getActualInitSQLFiles(databaseName, databaseType);
    }
    
    /**
     * Get verification init SQL file.
     * 
     * @param databaseType database type
     * @return verification init SQL file
     */
    public String getVerificationInitSQLFile(final DatabaseType databaseType) {
        return dataPath.getVerificationInitSQLFile(databaseType);
    }
    
    /**
     * Get init SQL resource path.
     *
     * @param databaseType database type
     * @return init SQL resource path
     */
    public String getInitSQLResourcePath(final DatabaseType databaseType) {
        return dataPath.getInitSQLResourcePath(Type.ACTUAL, databaseType);
    }
    
    /**
     * Get verification init SQL resource path.
     *
     * @param databaseType database type
     * @return verification init SQL resource path
     */
    public String getVerificationInitSQLResourcePath(final DatabaseType databaseType) {
        return dataPath.getInitSQLResourcePath(Type.VERIFICATION, databaseType);
    }
    
    /**
     * Get data set file.
     *
     * @return data set file
     */
    public String getDataSetFile() {
        return dataPath.getDataSetFile(Type.ACTUAL);
    }
    
    /**
     * Get verification data set file.
     *
     * @return verification data set file
     */
    public String getVerificationDataSetFile() {
        return dataPath.getDataSetFile(Type.VERIFICATION);
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
    
    private boolean isFileExist(final String fileName) {
        return null != ScenarioPath.class.getClassLoader().getResource(String.join("/", ROOT_PATH, scenario, fileName));
    }
}
