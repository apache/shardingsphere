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

package org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.SQLParserTestCases;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.integrate.loader.TestCaseFileLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * SQL parser test cases registry.
 */
public final class SQLParserTestCasesRegistry {
    
    private final Map<String, SQLParserTestCase> sqlParserTestCases;
    
    public SQLParserTestCasesRegistry(final String rootDirectory) {
        sqlParserTestCases = load(rootDirectory);
    }
    
    @SneakyThrows({JAXBException.class, IOException.class})
    private Map<String, SQLParserTestCase> load(final String directory) {
        File file = new File(SQLParserTestCasesRegistry.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(directory, file) : loadFromTargetDirectory(directory);
    }
    
    private Map<String, SQLParserTestCase> loadFromJar(final String directory, final File file) throws JAXBException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (String each : TestCaseFileLoader.loadFileNamesFromJar(file, directory)) {
            Map<String, SQLParserTestCase> sqlParserTestCaseMap = createSQLParserTestCases(SQLParserTestCasesRegistry.class.getClassLoader().getResourceAsStream(each));
            checkDuplicate(result, sqlParserTestCaseMap);
            result.putAll(sqlParserTestCaseMap);
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> loadFromTargetDirectory(final String directory) throws IOException, JAXBException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (File each : TestCaseFileLoader.loadFilesFromTargetDirectory(directory)) {
            try (FileInputStream fileInputStream = new FileInputStream(each)) {
                Map<String, SQLParserTestCase> sqlParserTestCaseMap = createSQLParserTestCases(fileInputStream);
                checkDuplicate(result, sqlParserTestCaseMap);
                result.putAll(sqlParserTestCaseMap);
            }
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> createSQLParserTestCases(final InputStream inputStream) throws JAXBException {
        return ((SQLParserTestCases) JAXBContext.newInstance(SQLParserTestCases.class).createUnmarshaller().unmarshal(inputStream)).getAllSQLParserTestCases();
    }
    
    private void checkDuplicate(final Map<String, SQLParserTestCase> existedSQLParserTestCaseMap, final Map<String, SQLParserTestCase> newSQLParserTestCaseMap) {
        Collection<String> existedSQLParserTestCaseIds = new HashSet<>(existedSQLParserTestCaseMap.keySet());
        existedSQLParserTestCaseIds.retainAll(newSQLParserTestCaseMap.keySet());
        Preconditions.checkState(existedSQLParserTestCaseIds.isEmpty(), "Find duplicated SQL Case IDs: %s", existedSQLParserTestCaseIds);
    }
    
    /**
     * Get SQL parser test case.
     * 
     * @param sqlCaseId SQL case ID
     * @return SQL parser test case
     */
    public SQLParserTestCase get(final String sqlCaseId) {
        Preconditions.checkState(sqlParserTestCases.containsKey(sqlCaseId), "Can not find SQL of ID: %s", sqlCaseId);
        return sqlParserTestCases.get(sqlCaseId);
    }
    
    /**
     * Get all SQL case IDs.
     *
     * @return all SQL case IDs
     */
    public Collection<String> getAllSQLCaseIDs() {
        return sqlParserTestCases.keySet();
    }
}
