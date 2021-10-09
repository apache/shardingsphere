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

package org.apache.shardingsphere.infra.optimize.converter.parameterized.loader;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.optimize.converter.parameterized.jaxb.SQLNodeConvertCase;
import org.apache.shardingsphere.infra.optimize.converter.parameterized.jaxb.SQLNodeConvertCases;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * SQL node convert cases loader.
 */
public final class SQLNodeConvertCasesLoader {
    
    private static final String FILE_EXTENSION = ".xml";
    
    private final Map<String, SQLNodeConvertCase> cases;
    
    public SQLNodeConvertCasesLoader(final String rootDirection) {
        cases = load(rootDirection);
    }
    
    @SneakyThrows({JAXBException.class, IOException.class})
    private Map<String, SQLNodeConvertCase> load(final String path) {
        Map<String, SQLNodeConvertCase> result = new TreeMap<>();
        for (File each : loadFiles(path)) {
            buildCaseMap(result, new FileInputStream(each));
        }
        return result;
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private static Collection<File> loadFiles(final String path) {
        URL url = SQLNodeConvertCasesLoader.class.getClassLoader().getResource(path);
        if (null == url) {
            return Collections.emptyList();
        }
        Collection<File> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
                if (file.toString().endsWith(FILE_EXTENSION)) {
                    result.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private void buildCaseMap(final Map<String, SQLNodeConvertCase> sqlNodeConvertCaseMap, final InputStream inputStream) throws JAXBException {
        SQLNodeConvertCases sqlNodeConvertCases = (SQLNodeConvertCases) JAXBContext.newInstance(SQLNodeConvertCases.class).createUnmarshaller().unmarshal(inputStream);
        for (SQLNodeConvertCase each : sqlNodeConvertCases.getSqlNodeConvertCases()) {
            if (null == each.getDatabaseTypes()) {
                each.setDatabaseTypes(sqlNodeConvertCases.getDatabaseTypes());
            }
            Preconditions.checkState(!sqlNodeConvertCaseMap.containsKey(each.getId()), "Find duplicated SQL node convert case ID: %s", each.getId());
            sqlNodeConvertCaseMap.put(each.getId(), each);
        }
    }
    
    /**
     * Get case value.
     *
     * @param caseId case ID
     * @return case value
     */
    public String getCaseValue(final String caseId) {
        Preconditions.checkState(cases.containsKey(caseId), "Can't find SQL of ID: %s", caseId);
        return cases.get(caseId).getValue();
    }
    
    /**
     * Get test parameters for junit parameterized test cases.
     *
     * @param databaseTypes database types
     * @return test parameters for junit parameterized test cases
     */
    public Collection<Object[]> getTestParameters(final Collection<String> databaseTypes) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLNodeConvertCase each : cases.values()) {
            result.addAll(getSQLTestParameters(databaseTypes, each));
        }
        return result;
    }
    
    private Collection<Object[]> getSQLTestParameters(final Collection<String> databaseTypes, final SQLNodeConvertCase sqlNodeConvertCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : getDatabaseTypes(sqlNodeConvertCase.getDatabaseTypes())) {
            if (databaseTypes.contains(each)) {
                Object[] parameters = new Object[2];
                parameters[0] = sqlNodeConvertCase.getId();
                parameters[1] = each;
                result.add(parameters);
            }
        }
        return result;
    }
    
    private static Collection<String> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? getAllDatabaseTypes() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private static Collection<String> getAllDatabaseTypes() {
        return Arrays.asList("H2", "MySQL", "PostgreSQL", "Oracle", "SQLServer", "SQL92");
    }
}
