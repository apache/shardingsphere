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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLCasesLoader {
    
    private static final Map<String, SQLCase> STATEMENT_MAP;
    
    private static final Map<String, SQLCase> UNSUPPORTED_STATEMENT_MAP;
    
    static {
        STATEMENT_MAP = loadSQLCases("sql");
        UNSUPPORTED_STATEMENT_MAP = loadSQLCases("sql/unsupported");
    }
    
    private static Map<String, SQLCase> loadSQLCases(final String path) {
        File file = new File(SQLCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        try {
            return file.isFile() ? loadSQLCasesFromJar(path, file) : loadSQLCasesFromTargetFolder(path);
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
                    fillStatementMap(result, SQLCasesLoader.class.getClassLoader().getResourceAsStream(name));
                }
            }
        }
        return result;
    }
    
    private static Map<String, SQLCase> loadSQLCasesFromTargetFolder(final String path) throws FileNotFoundException, JAXBException {
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
            loadSQLCasesFromFolder(result, each);
        }
        return result;
    }
    
    private static void loadSQLCasesFromFolder(final Map<String, SQLCase> sqlStatementMap, final File file) throws FileNotFoundException, JAXBException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return;
            }
            for (File each : files) {
                fillStatementMap(sqlStatementMap, new FileInputStream(each));
            }
        } else {
            fillStatementMap(sqlStatementMap, new FileInputStream(file));
        }
    }
    
    private static void fillStatementMap(final Map<String, SQLCase> result, final InputStream inputStream) throws JAXBException {
        SQLCases statements = (SQLCases) JAXBContext.newInstance(SQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase statement : statements.getSqlCases()) {
            result.put(statement.getId(), statement);
        }
    }
    
    /**
     * Get unsupported SQL test cases.
     * 
     * @return unsupported SQL test cases
     */
    public static Collection<SQLCase> getUnsupportedSqlStatements() {
        return UNSUPPORTED_STATEMENT_MAP.values();
    }
    
    /**
     * Get SQL.
     * @param sqlId SQL ID
     * @return SQL
     */
    public static String getSql(final String sqlId) {
        checkSqlId(sqlId);
        SQLCase statement = STATEMENT_MAP.get(sqlId);
        return statement.getValue();
    }
    
    /**
     * Get database types.
     * 
     * @param sqlId SQL ID
     * @return database types
     */
    public static Collection<String> getDatabaseTypes(final String sqlId) {
        checkSqlId(sqlId);
        String databaseTypes = STATEMENT_MAP.get(sqlId).getDatabaseTypes();
        return Strings.isNullOrEmpty(databaseTypes) ? Collections.<String>emptyList() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private static void checkSqlId(final String sqlId) {
        if (null == sqlId || !STATEMENT_MAP.containsKey(sqlId)) {
            throw new RuntimeException("Can't find sql of id:" + sqlId);
        }
    }
}
