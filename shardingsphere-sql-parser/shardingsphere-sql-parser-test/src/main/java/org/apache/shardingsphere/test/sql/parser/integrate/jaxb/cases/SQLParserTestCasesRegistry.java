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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * SQL parser test cases registry.
 */
public final class SQLParserTestCasesRegistry {
    
    private final Map<String, SQLParserTestCase> sqlParserTestCases;
    
    public SQLParserTestCasesRegistry(final String rootDirectory) {
        sqlParserTestCases = load(rootDirectory);
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private Map<String, SQLParserTestCase> load(final String directory) {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        File file = new File(SQLParserTestCasesRegistry.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        if (file.isFile()) {
            for (InputStream each : loadInputStreamsFromJar(directory, file)) {
                putAll(getSQLParserTestCases(each), result);
            }
        } else {
            URL url = SQLParserTestCasesRegistry.class.getClassLoader().getResource(directory);
            Preconditions.checkNotNull(url, "Can not find SQL parser test cases.");
            File filePath = new File(url.toURI().getPath());
            File[] files = filePath.listFiles();
            if (null == files) {
                return result;
            }
            for (File each : files) {
                putAll(loadTestCasesFromDirectory(each), result);
            }
        }
        return result;
    }
    
    private Collection<InputStream> loadInputStreamsFromJar(final String path, final File file) throws IOException {
        Collection<InputStream> result = new LinkedList<>();
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && name.endsWith(".xml")) {
                    result.add(SQLParserTestCasesRegistry.class.getClassLoader().getResourceAsStream(name));
                }
            }
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> loadTestCasesFromDirectory(final File file) throws IOException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File each : files) {
                    putAll(loadTestCasesFromDirectory(each), result);
                }
            }
        } else {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                putAll(getSQLParserTestCases(fileInputStream), result);
            }
        }
        return result;
    }
    
    private void putAll(final Map<String, SQLParserTestCase> sqlParserTestCases, final Map<String, SQLParserTestCase> target) {
        Collection<String> sqlParserTestCaseIds = new HashSet<>(sqlParserTestCases.keySet());
        sqlParserTestCaseIds.retainAll(target.keySet());
        Preconditions.checkState(sqlParserTestCaseIds.isEmpty(), "Find duplicated SQL Case IDs: %s", sqlParserTestCaseIds);
        target.putAll(sqlParserTestCases);
    }
    
    private Map<String, SQLParserTestCase> getSQLParserTestCases(final InputStream inputStream) {
        try {
            return ((SQLParserTestCases) JAXBContext.newInstance(SQLParserTestCases.class).createUnmarshaller().unmarshal(inputStream)).getAllSQLParserTestCases();
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Get SQL parser test case.
     * 
     * @param sqlCaseId SQL case ID
     * @return SQL parser test case
     */
    public SQLParserTestCase get(final String sqlCaseId) {
        Preconditions.checkState(sqlParserTestCases.containsKey(sqlCaseId), "Can not find SQL of id: %s", sqlCaseId);
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
