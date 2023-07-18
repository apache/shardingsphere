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

package org.apache.shardingsphere.test.e2e.env.runtime.scenario.path;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;

import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Scenario data path.
 */
@RequiredArgsConstructor
public final class ScenarioDataPath {
    
    private static final String ROOT_PATH = "env/scenario";
    
    private static final String DATA_PATH = "data";
    
    private static final String DATABASES_FILE = "databases.xml";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String INIT_SQL_PATH = "init-sql";
    
    private static final String BASIC_INIT_SQL_FILE = "init.sql";
    
    private final String scenario;
    
    /**
     * Get databases file.
     * 
     * @param type data type
     * @return databases file
     */
    public String getDatabasesFile(final Type type) {
        return getFile(type, DATABASES_FILE);
    }
    
    /**
     * Get data set file.
     *
     * @param type data type
     * @return data set file
     */
    public String getDataSetFile(final Type type) {
        return getFile(type, DATASET_FILE);
    }
    
    private String getFile(final Type type, final String fileName) {
        String path = String.join("/", getBasicPath(type), fileName);
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        assertNotNull(url, String.format("File `%s` must exist.", path));
        return url.getFile();
    }
    
    /**
     * Get init SQL file.
     *
     * @param type data type
     * @param databaseType database type
     * @return expected init SQL file
     */
    public String getInitSQLFile(final Type type, final DatabaseType databaseType) {
        String initSQLFileName = String.join("-", "01", type.name().toLowerCase(), BASIC_INIT_SQL_FILE);
        String initSQLResourceFile = String.join("/", getInitSQLResourcePath(type, databaseType), initSQLFileName);
        URL url = Thread.currentThread().getContextClassLoader().getResource(initSQLResourceFile);
        assertNotNull(url, String.format("File `%s` must exist.", initSQLResourceFile));
        return url.getFile();
    }
    
    /**
     * Find actual init SQL file by database name.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @return actual init SQL file
     */
    public Optional<String> findActualDatabaseInitSQLFile(final String databaseName, final DatabaseType databaseType) {
        return isActualDatabaseInitSQLFileExisted(databaseName, databaseType) ? Optional.of(getActualDatabaseInitSQLFile(databaseName, databaseType)) : Optional.empty();
    }
    
    private boolean isActualDatabaseInitSQLFileExisted(final String databaseName, final DatabaseType databaseType) {
        String initSQLResourceFile = getActualDatabaseInitSQLResourceFile(databaseName, databaseType);
        return null != Thread.currentThread().getContextClassLoader().getResource(initSQLResourceFile);
    }
    
    private String getActualDatabaseInitSQLResourceFile(final String databaseName, final DatabaseType databaseType) {
        String initSQLFileName = String.join("-", Type.ACTUAL.name().toLowerCase(), databaseName, BASIC_INIT_SQL_FILE);
        return String.join("/", getInitSQLResourcePath(Type.ACTUAL, databaseType), initSQLFileName);
    }
    
    private String getActualDatabaseInitSQLFile(final String databaseName, final DatabaseType databaseType) {
        String resourceFile = getActualDatabaseInitSQLResourceFile(databaseName, databaseType);
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceFile);
        assertNotNull(url, String.format("File `%s` must exist.", resourceFile));
        return url.getFile();
    }
    
    /**
     * Get init SQL resource path.
     *
     * @param type data type
     * @param databaseType database type
     * @return init SQL resource path
     */
    public String getInitSQLResourcePath(final Type type, final DatabaseType databaseType) {
        return String.join("/", getBasicPath(type), INIT_SQL_PATH, databaseType.getType().toLowerCase());
    }
    
    private String getBasicPath(final Type type) {
        return String.join("/", ROOT_PATH, scenario, DATA_PATH, type.name().toLowerCase());
    }
    
    /**
     * Data type.
     */
    public enum Type {
        ACTUAL, EXPECTED
    }
}
