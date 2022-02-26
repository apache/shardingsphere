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

package org.apache.shardingsphere.test.integration.env.scenario.path;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertNotNull;

/**
 * Scenario data path.
 */
@RequiredArgsConstructor
public final class ScenarioDataPath {
    
    private static final String ROOT_PATH = "env/scenario";
    
    private static final String DATA_FOLDER = "data";
    
    private static final String DATABASES_FILE = "databases.xml";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String INIT_SQL_PATH = "init-sql";
    
    private static final String INIT_SQL_FILE = "init.sql";
    
    private static final String EXPECTED_INIT_SQL_FILE = "expected.init.sql";
    
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
        URL url = ScenarioDataPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist.", path), url);
        return url.getFile();
    }
    
    /**
     * Get actual init SQL files.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @return init SQL files
     */
    public Collection<String> getActualInitSQLFiles(final String databaseName, final DatabaseType databaseType) {
        Collection<String> result = new LinkedList<>();
        result.add(getInitSQLFile(Type.ACTUAL, databaseType, INIT_SQL_FILE));
        String dbInitSQLFileName = "init-" + databaseName + ".sql";
        if (isInitSQLFileExist(databaseType, dbInitSQLFileName)) {
            result.add(getInitSQLFile(Type.ACTUAL, databaseType, dbInitSQLFileName));
        }
        return result;
    }
    
    /**
     * Get expected init SQL file.
     *
     * @param databaseType database type
     * @return expected init SQL file
     */
    public String getExpectedInitSQLFile(final DatabaseType databaseType) {
        return getInitSQLFile(Type.EXPECTED, databaseType, EXPECTED_INIT_SQL_FILE);
    }
    
    private String getInitSQLFile(final Type type, final DatabaseType databaseType, final String fileName) {
        String resourceFile = getInitSQLResourceFile(type, databaseType, fileName);
        URL url = ScenarioDataPath.class.getClassLoader().getResource(resourceFile);
        assertNotNull(String.format("File `%s` must exist.", resourceFile), url);
        return url.getFile();
    }
    
    private String getInitSQLResourceFile(final Type type, final DatabaseType databaseType, final String fileName) {
        return String.join("/", getBasicPath(type), INIT_SQL_PATH, databaseType.getName().toLowerCase(), fileName);
    }
    
    private boolean isInitSQLFileExist(final DatabaseType databaseType, final String fileName) {
        return null != ScenarioDataPath.class.getClassLoader().getResource(getInitSQLResourceFile(Type.ACTUAL, databaseType, fileName));
    }
    
    /**
     * Get init SQL resource path.
     *
     * @param type data type
     * @param databaseType database type
     * @return init SQL resource path
     */
    public String getInitSQLResourcePath(final Type type, final DatabaseType databaseType) {
        return String.join("/", "", getBasicPath(type), INIT_SQL_PATH, databaseType.getName().toLowerCase());
    }
    
    private String getBasicPath(final Type type) {
        return String.join("/", ROOT_PATH, scenario, DATA_FOLDER, type.name().toLowerCase());
    }
    
    /**
     * Data type.
     */
    public enum Type {
        ACTUAL, EXPECTED
    }
}
