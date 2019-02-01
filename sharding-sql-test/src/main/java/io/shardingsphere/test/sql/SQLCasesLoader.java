/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.test.sql;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * SQL test cases loader.
 * 
 * @author zhangliang 
 */
public class SQLCasesLoader {
    
    private static final SQLCasesLoader INSTANCE = new SQLCasesLoader();
    
    protected Map<String, SQLCase> supportedSQLCaseMap;
    
    protected Map<String, SQLCase> unsupportedSQLCaseMap;
    
    private final Map<String, SQLCase> parseErrorSQLCaseMap;
    
    protected SQLCasesLoader() {
        supportedSQLCaseMap = loadSQLCases("sql");
        unsupportedSQLCaseMap = loadSQLCases("unsupported_sql");
        parseErrorSQLCaseMap = loadSQLCases("parse_error_sql");
    }
    
    /**
     * Get singleton instance.
     * 
     * @return singleton instance
     */
    public static SQLCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Switch SQL case.
     * 
     * @param path path
     */
    public void switchSQLCase(final String path) {
        supportedSQLCaseMap = loadSQLCases(path);
    }
    
    @SneakyThrows
    protected static Map<String, SQLCase> loadSQLCases(final String path) {
        File file = new File(SQLCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadSQLCasesFromJar(path, file) : loadSQLCasesFromTargetDirectory(path);
    }
    
    private static Map<String, SQLCase> loadSQLCasesFromJar(final String path, final File file) throws IOException, JAXBException {
        Map<String, SQLCase> result = new TreeMap<>();
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
    
    private static Map<String, SQLCase> loadSQLCasesFromTargetDirectory(final String path) {
        Map<String, SQLCase> result = new TreeMap<>();
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
    
    @SneakyThrows
    private static void loadSQLCasesFromDirectory(final Map<String, SQLCase> sqlStatementMap, final File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return;
            }
            for (File each : files) {
                loadSQLCasesFromDirectory(sqlStatementMap, each);
            }
        } else {
            fillSQLMap(sqlStatementMap, new FileInputStream(file));
        }
    }
    
    private static void fillSQLMap(final Map<String, SQLCase> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        SQLCases sqlCases = (SQLCases) JAXBContext.newInstance(SQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase each : sqlCases.getSqlCases()) {
            if (null == each.getDatabaseTypes()) {
                each.setDatabaseTypes(sqlCases.getDatabaseTypes());
            }
            if (null != sqlCases.getNamespace()) {
                each.setId(sqlCases.getNamespace() + "." + each.getId());
            }
            sqlCaseMap.put(each.getId(), each);
        }
    }
    
    /**
     * Get supported SQL.
     *
     * @param sqlCaseId SQL case ID
     * @param sqlCaseType SQL case type
     * @param parameters SQL parameters
     * @return SQL
     */
    public String getSupportedSQL(final String sqlCaseId, final SQLCaseType sqlCaseType, final List<?> parameters) {
        return getSQL(supportedSQLCaseMap, sqlCaseId, sqlCaseType, parameters);
    }
    
    /**
     * Get unsupported SQL.
     * 
     * @param sqlCaseId SQL case ID
     * @param sqlCaseType SQL case type
     * @param parameters SQL parameters
     * @return SQL
     */
    public String getUnsupportedSQL(final String sqlCaseId, final SQLCaseType sqlCaseType, final List<?> parameters) {
        return getSQL(unsupportedSQLCaseMap, sqlCaseId, sqlCaseType, parameters);
    }
    
    /**
     * Get SQLParsingException error's SQL.
     *
     * @param sqlCaseId SQL case ID
     * @param sqlCaseType SQL case type
     * @param parameters SQL parameters
     * @return SQL
     */
    public String getSQLParsingErrorSQL(final String sqlCaseId, final SQLCaseType sqlCaseType, final List<?> parameters) {
        return getSQL(parseErrorSQLCaseMap, sqlCaseId, sqlCaseType, parameters);
    }
    
    private String getSQL(final Map<String, SQLCase> sqlCaseMap, final String sqlCaseId, final SQLCaseType sqlCaseType, final List<?> parameters) {
        switch (sqlCaseType) {
            case Literal:
                return getLiteralSQL(getSQLFromMap(sqlCaseId, sqlCaseMap), parameters);
            case Placeholder:
                return getPlaceholderSQL(getSQLFromMap(sqlCaseId, sqlCaseMap));
            default:
                throw new UnsupportedOperationException(sqlCaseType.name());
        }
    }
    
    private String getSQLFromMap(final String id, final Map<String, SQLCase> sqlCaseMap) {
        Preconditions.checkState(sqlCaseMap.containsKey(id), "Can't find SQL of id: " + id);
        SQLCase statement = sqlCaseMap.get(id);
        return statement.getValue();
    }
    
    private String getPlaceholderSQL(final String sql) {
        return sql.replace("%%", "%").replace("'%'", "'%%'");
    }
    
    private String getLiteralSQL(final String sql, final List<?> parameters) {
        if (null == parameters || parameters.isEmpty()) {
            return sql;
        }
        return String.format(sql.replace("?", "%s"), parameters.toArray()).replace("%%", "%").replace("'%'", "'%%'");
    }
    
    /**
     * Get test parameters for junit parameterized test case for supported SQL.
     *
     * @param allDatabaseTypes all database types
     * @param enumType enum type
     * @return test parameters for junit parameterized test case for supported SQL
     */
    public Collection<Object[]> getSupportedSQLTestParameters(final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType) {
        return getTestParameters(supportedSQLCaseMap, allDatabaseTypes, enumType);
    }
    
    /**
     * Get test parameters for junit parameterized test case for unsupported SQL.
     *
     * @param allDatabaseTypes all database types
     * @param enumType enum type
     * @return test parameters for junit parameterized test case for unsupported SQL
     */
    public Collection<Object[]> getUnsupportedSQLTestParameters(final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType) {
        return getTestParameters(unsupportedSQLCaseMap, allDatabaseTypes, enumType);
    }
    
    /**
     * Get test parameters for junit parameterized test case for sql parsing error SQL.
     *
     * @param allDatabaseTypes all database types
     * @param enumType enum type
     * @return test parameters for junit parameterized test case for parsing error SQL
     */
    public Collection<Object[]> getSQLParsingErrorTestParameters(final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType) {
        return getTestParameters(parseErrorSQLCaseMap, allDatabaseTypes, enumType);
    }
    
    private Collection<Object[]> getTestParameters(final Map<String, SQLCase> sqlCaseMap, final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCase each : sqlCaseMap.values()) {
            result.addAll(getTestParameters(allDatabaseTypes, enumType, each));
        }
        return result;
    }
    
    private Collection<Object[]> getTestParameters(final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType, final SQLCase sqlCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCaseType each : SQLCaseType.values()) {
            if (each == SQLCaseType.Placeholder && !Strings.isNullOrEmpty(sqlCase.getSqlType()) && !("dql".equals(sqlCase.getSqlType()) || "dml".equals(sqlCase.getSqlType()))) {
                continue;
            }
            result.addAll(getTestParameters(sqlCase, allDatabaseTypes, enumType, each));
        }
        return result;
    }
    
    private static Collection<Object[]> getTestParameters(
            final SQLCase sqlCase, final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType, final SQLCaseType sqlCaseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (Enum each : getDatabaseTypes(sqlCase.getDatabaseTypes(), allDatabaseTypes, enumType)) {
            Object[] parameters = new Object[3];
            parameters[0] = sqlCase.getId();
            parameters[1] = each;
            parameters[2] = sqlCaseType;
            result.add(parameters);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private static Collection<? extends Enum> getDatabaseTypes(final String databaseTypes, final Collection<? extends Enum> allDatabaseTypes, final Class<? extends Enum> enumType) {
        if (Strings.isNullOrEmpty(databaseTypes)) {
            return allDatabaseTypes;
        }
        Set<Enum> result = new HashSet<>(allDatabaseTypes.size());
        for (String each : databaseTypes.split(",")) {
            result.add(Enum.valueOf(enumType, each));
        }
        return result;
    }
    
    /**
     * Count all supported SQL cases.
     *
     * @return count of all supported SQL cases
     */
    public int countAllSupportedSQLCases() {
        return supportedSQLCaseMap.size();
    }
}
