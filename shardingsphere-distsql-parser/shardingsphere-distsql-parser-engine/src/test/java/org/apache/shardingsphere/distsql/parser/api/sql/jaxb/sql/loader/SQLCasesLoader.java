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

package org.apache.shardingsphere.distsql.parser.api.sql.jaxb.sql.loader;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.sql.SQLCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.sql.SQLCases;
import org.apache.shardingsphere.distsql.parser.api.sql.loader.TestCaseFileLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL test cases loader.
 */
public final class SQLCasesLoader {
    
    private final Map<String, SQLCase> sqlCases;
    
    public SQLCasesLoader(final String rootDirection) {
        sqlCases = load(rootDirection);
    }
    
    @SneakyThrows({JAXBException.class, IOException.class})
    private Map<String, SQLCase> load(final String path) {
        File file = new File(SQLCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(file, path) : loadFromTargetDirectory(path);
    }

    private Map<String, SQLCase> loadFromJar(final File file, final String path) throws JAXBException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (String each : TestCaseFileLoader.loadFileNamesFromJar(file, path)) {
            fillSQLMap(result, SQLCasesLoader.class.getClassLoader().getResourceAsStream(each));
        }
        return result;
    }
    
    private Map<String, SQLCase> loadFromTargetDirectory(final String path) throws JAXBException, FileNotFoundException {
        Map<String, SQLCase> result = new TreeMap<>();
        for (File each : TestCaseFileLoader.loadFilesFromTargetDirectory(path)) {
            fillSQLMap(result, new FileInputStream(each));
        }
        return result;
    }
    
    private void fillSQLMap(final Map<String, SQLCase> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        SQLCases sqlCases = (SQLCases) JAXBContext.newInstance(SQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLCase each : sqlCases.getSqlCases()) {
            Preconditions.checkState(!sqlCaseMap.containsKey(each.getId()), "Find duplicated SQL Case ID: %s", each.getId());
            sqlCaseMap.put(each.getId(), each);
        }
    }

    /**
     * Get SQL.
     *
     * @param sqlCaseId SQL case ID
     * @return SQL
     */
    public String getSQL(final String sqlCaseId) {
        return getSQLFromMap(sqlCaseId, sqlCases);
    }
    
    private String getSQLFromMap(final String id, final Map<String, SQLCase> sqlCaseMap) {
        Preconditions.checkState(sqlCaseMap.containsKey(id), "Can't find SQL of ID: %s", id);
        SQLCase statement = sqlCaseMap.get(id);
        return statement.getValue();
    }

    /**
     * Get test parameters for junit parameterized test cases.
     *
     * @return test parameters for junit parameterized test cases
     */
    public Collection<Object[]> getSQLTestParameters() {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCase each : sqlCases.values()) {
            Object[] parameters = new Object[1];
            parameters[0] = each.getId();
            result.add(parameters);
        }
        return result;
    }

    /**
     * Replaces each substring of this string that matches the literal target sequence with
     * literal replacements one by one.
     *
     * @param source       The source string need to be replaced
     * @param target       The sequence of char values to be replaced
     * @param replacements Array of replacement
     * @return The resulting string
     * @throws IllegalArgumentException When replacements is not enough to replace found target.
     */
    private static String replace(final String source, final CharSequence target, final Object... replacements) {
        if (null == source || null == replacements) {
            return source;
        }
        Matcher matcher = Pattern.compile(target.toString(), Pattern.LITERAL).matcher(source);
        int found = 0;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            found++;
            if (found > replacements.length) {
                throw new IllegalArgumentException(
                        String.format("Missing replacement for '%s' at [%s].", target, found));
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacements[found - 1].toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Get all SQL case IDs.
     * 
     * @return all SQL case IDs
     */
    public Collection<String> getAllSQLCaseIDs() {
        return sqlCases.keySet();
    }
}
