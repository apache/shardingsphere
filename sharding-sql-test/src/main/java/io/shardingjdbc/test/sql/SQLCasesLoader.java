/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.test.sql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * SQL test cases loader.
 * 
 * @author zhangliang 
 */
public final class SQLCasesLoader {
    
    private static final SQLCasesLoader INSTANCE = new SQLCasesLoader();
    
    private final Map<String, SQLCase> sqlCaseMap;
    
    private final Map<String, SQLCase> unsupportedSQLCaseMap;
    
    private SQLCasesLoader() {
        sqlCaseMap = loadSQLCases("sql");
        unsupportedSQLCaseMap = loadSQLCases("unsupported_sql");
    }
    
    /**
     * Get singleton instance.
     * 
     * @return singleton instance
     */
    public static SQLCasesLoader getInstance() {
        return INSTANCE;
    }
    
    private static Map<String, SQLCase> loadSQLCases(final String path) {
        File file = new File(SQLCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        try {
            return file.isFile() ? loadSQLCasesFromJar(path, file) : loadSQLCasesFromTargetDirectory(path);
        } catch (final IOException | JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Map<String, SQLCase> loadSQLCasesFromJar(final String path, final File file) throws IOException, JAXBException {
        Map<String, SQLCase> result = new HashMap<>(65536, 1);
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && name.endsWith(".xml")) {
                    fillSQLMap(result, SQLCasesLoader.class.getClassLoader().getResourceAsStream(name));
                }
            }
        }
        return result;
    }
    
    private static Map<String, SQLCase> loadSQLCasesFromTargetDirectory(final String path) throws FileNotFoundException, JAXBException {
        Map<String, SQLCase> result = new HashMap<>(65536, 1);
        URL url = SQLCasesLoader.class.getClassLoader().getResource(path);
        if (null == url) {
            return result;
        }
        File filePath = new File(url.getPath());
        if (!filePath.exists()) {
            return result;
        }
        File[] files = filePath.listFiles();
        if (null == files) {
            return result;
        }
        for (File each : files) {
            loadSQLCasesFromDirectory(result, each);
        }
        return result;
    }
    
    private static void loadSQLCasesFromDirectory(final Map<String, SQLCase> sqlStatementMap, final File file) throws FileNotFoundException, JAXBException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return;
            }
            for (File each : files) {
                fillSQLMap(sqlStatementMap, new FileInputStream(each));
            }
        } else {
            fillSQLMap(sqlStatementMap, new FileInputStream(file));
        }
    }
    
    private static void fillSQLMap(final Map<String, SQLCase> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        SQLCases sqlCases = (SQLCases) JAXBContext.newInstance(SQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase each : sqlCases.getSqlCases()) {
            sqlCaseMap.put(each.getId(), each);
        }
    }
    
    /**
     * Get all SQL cases.
     * 
     * @return all SQL cases
     */
    public Collection<SQLCase> getAllSQLCases() {
        return sqlCaseMap.values();
    }
    
    /**
     * Get all unsupported SQL cases.
     *
     * @return all unsupported SQL cases
     */
    public Collection<SQLCase> getAllUnsupportedSQLCases() {
        return unsupportedSQLCaseMap.values();
    }
    
    /**
     * Get SQL.
     * @param id SQL ID
     * @return SQL
     */
    public String getSQL(final String id) {
        return getSQLFromMap(id, sqlCaseMap);
    }
    
    /**
     * Get unsupported SQL.
     * @param id SQL ID
     * @return SQL
     */
    public String getUnsupportedSQL(final String id) {
        return getSQLFromMap(id, unsupportedSQLCaseMap);
    }
    
    private String getSQLFromMap(final String id, final Map<String, SQLCase> sqlCaseMap) {
        Preconditions.checkState(sqlCaseMap.containsKey(id), "Can't find SQL of id: " + id);
        SQLCase statement = sqlCaseMap.get(id);
        return statement.getValue();
    }
    
    /**
     * Get database types.
     * 
     * @param id SQL ID
     * @return database types
     */
    public Collection<String> getDatabaseTypes(final String id) {
        Preconditions.checkState(sqlCaseMap.containsKey(id), "Can't find SQL of id: " + id);
        String databaseTypes = sqlCaseMap.get(id).getDatabaseTypes();
        return Strings.isNullOrEmpty(databaseTypes) ? Collections.<String>emptyList() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
}
