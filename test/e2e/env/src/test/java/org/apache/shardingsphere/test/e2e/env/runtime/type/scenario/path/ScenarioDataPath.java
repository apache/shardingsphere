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

package org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Scenario data path.
 */
public final class ScenarioDataPath {
    
    private static final String DATABASES_FILE = "databases.xml";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String INIT_SQL_PATH = "init-sql";
    
    private static final String BASIC_INIT_SQL_FILE = "init.sql";
    
    private final String scenarioDirectory;
    
    public ScenarioDataPath(final String scenario, final Type type) {
        scenarioDirectory = String.join("/", "env", "scenario", scenario, "data", type.name().toLowerCase());
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
     * Get data set file.
     *
     * @return data set file
     */
    public String getDataSetFile() {
        return getFile(DATASET_FILE);
    }
    
    private String getFile(final String fileName) {
        String path = String.join("/", scenarioDirectory, fileName);
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        assertNotNull(url, String.format("File `%s` must exist.", path));
        return url.getFile();
    }
    
    /**
     * Find target init SQL file by database name.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @return target init SQL file
     */
    public Optional<String> findTargetDatabaseInitSQLFile(final String databaseName, final DatabaseType databaseType) {
        return isTargetDatabaseInitSQLFileExisted(databaseName, databaseType) ? Optional.of(getTargetDatabaseInitSQLFile(databaseName, databaseType)) : Optional.empty();
    }
    
    private boolean isTargetDatabaseInitSQLFileExisted(final String databaseName, final DatabaseType databaseType) {
        return null != Thread.currentThread().getContextClassLoader().getResource(getTargetDatabaseInitSQLResourceFile(databaseName, databaseType));
    }
    
    private String getTargetDatabaseInitSQLFile(final String databaseName, final DatabaseType databaseType) {
        String resourceFile = getTargetDatabaseInitSQLResourceFile(databaseName, databaseType);
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceFile);
        assertNotNull(url, String.format("File `%s` must exist.", resourceFile));
        return url.getFile();
    }
    
    private String getTargetDatabaseInitSQLResourceFile(final String databaseName, final DatabaseType databaseType) {
        String initSQLFileName = String.join("-", Type.TARGETS.name().toLowerCase(), databaseName, BASIC_INIT_SQL_FILE);
        return String.join("/", getInitSQLResourcePath(databaseType), initSQLFileName);
    }
    
    /**
     * Get init SQL resource path.
     *
     * @param databaseType database type
     * @return init SQL resource path
     */
    public String getInitSQLResourcePath(final DatabaseType databaseType) {
        return String.join("/", scenarioDirectory, INIT_SQL_PATH, databaseType.getType().toLowerCase());
    }
    
    /**
     * Data type.
     */
    public enum Type {
        ACTUAL, EXPECTED, TARGETS
    }
}
