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

package org.apache.shardingsphere.test.sql.parser.internal.loader;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.SQLCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.SQLCases;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL cases loader.
 */
@Getter
public final class SQLCasesLoader {
    
    private static final Pattern PARAMETER_MARKER = Pattern.compile("\\?|\\$[0-9]+");
    
    private final Map<String, SQLCase> cases;
    
    public SQLCasesLoader(final String rootDirection) {
        cases = load(rootDirection);
    }
    
    @SneakyThrows({JAXBException.class, IOException.class})
    private Map<String, SQLCase> load(final String path) {
        File file = new File(SQLCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(file, path) : loadFromTargetDirectory(path);
    }
    
    private Map<String, SQLCase> loadFromJar(final File file, final String path) throws JAXBException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (String each : SQLCaseFileLoader.loadFileNamesFromJar(file, path)) {
            buildCaseMap(result, SQLCasesLoader.class.getClassLoader().getResourceAsStream(each));
        }
        return result;
    }
    
    private Map<String, SQLCase> loadFromTargetDirectory(final String path) throws JAXBException, FileNotFoundException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (File each : SQLCaseFileLoader.loadFilesFromTargetDirectory(path)) {
            buildCaseMap(result, new FileInputStream(each));
        }
        return result;
    }
    
    private void buildCaseMap(final Map<String, SQLCase> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        SQLCases sqlCases = (SQLCases) JAXBContext.newInstance(SQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase each : sqlCases.getSqlCases()) {
            if (null == each.getDatabaseTypes()) {
                each.setDatabaseTypes(sqlCases.getDatabaseTypes());
            }
            Preconditions.checkState(!sqlCaseMap.containsKey(each.getId()), "Find duplicated SQL Case ID: %s", each.getId());
            sqlCaseMap.put(each.getId(), each);
        }
    }
    
    /**
     * Get SQL case value.
     * 
     * @param sqlCaseId SQL case id
     * @param sqlCaseType SQL case type
     * @param params parameters
     * @param databaseType database type
     * @return SQL case value
     */
    public String getCaseValue(final String sqlCaseId, final SQLCaseType sqlCaseType, final List<?> params, final String databaseType) {
        switch (sqlCaseType) {
            case Literal:
                return getLiteralSQL(getSQLFromMap(sqlCaseId, getCases()), params);
            case Placeholder:
                return getPlaceholderSQL(getSQLFromMap(sqlCaseId, getCases()));
            default:
                throw new UnsupportedOperationException(sqlCaseType.name());
        }
    }
    
    /**
     * Get test parameters.
     * 
     * @param databaseTypes database types
     * @return test parameters
     */
    public Collection<Object[]> getTestParameters(final Collection<String> databaseTypes) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCase each : getCases().values()) {
            result.addAll(getSQLTestParameters(databaseTypes, each));
        }
        return result;
    }
    
    private String getSQLFromMap(final String id, final Map<String, SQLCase> sqlCaseMap) {
        Preconditions.checkState(sqlCaseMap.containsKey(id), "Can't find SQL of ID: %s", id);
        SQLCase statement = sqlCaseMap.get(id);
        return statement.getValue();
    }
    
    private String getPlaceholderSQL(final String sql) {
        return sql;
    }
    
    private String getLiteralSQL(final String sql, final List<?> params) {
        if (null == params || params.isEmpty()) {
            return sql;
        }
        return replace(sql, params.toArray());
    }
    
    private Collection<Object[]> getSQLTestParameters(final Collection<String> databaseTypes, final SQLCase sqlCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCaseType each : SQLCaseType.values()) {
            result.addAll(getSQLTestParameters(databaseTypes, sqlCase, each));
        }
        return result;
    }
    
    private static Collection<Object[]> getSQLTestParameters(final Collection<String> databaseTypes, final SQLCase sqlCase, final SQLCaseType sqlCaseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : getDatabaseTypes(sqlCase.getDatabaseTypes())) {
            if (databaseTypes.contains(each)) {
                Object[] params = new Object[3];
                params[0] = sqlCase.getId();
                params[1] = each;
                params[2] = sqlCaseType;
                result.add(params);
            }
        }
        return result;
    }
    
    private static Collection<String> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? getAllDatabaseTypes() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private static Collection<String> getAllDatabaseTypes() {
        return Arrays.asList("H2", "MySQL", "PostgreSQL", "Oracle", "SQLServer", "SQL92", "openGauss");
    }
    
    private static String replace(final String source, final Object... replacements) {
        if (null == source || null == replacements) {
            return source;
        }
        Matcher matcher = PARAMETER_MARKER.matcher(source);
        int found = 0;
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            if (ParameterMarkerType.QUESTION.getMarker().equals(group)) {
                appendReplacement(++found, replacements, matcher, buffer);
            } else {
                int dollarMarker = Integer.parseInt(group.replace(ParameterMarkerType.DOLLAR.getMarker(), ""));
                appendReplacement(dollarMarker, replacements, matcher, buffer);
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    private static void appendReplacement(final int markerIndex, final Object[] replacements, final Matcher matcher, final StringBuffer buffer) {
        Preconditions.checkArgument(markerIndex <= replacements.length, "Missing replacement for `%s` at index `%s`", PARAMETER_MARKER.pattern(), markerIndex);
        matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacements[markerIndex - 1].toString()));
    }
}
