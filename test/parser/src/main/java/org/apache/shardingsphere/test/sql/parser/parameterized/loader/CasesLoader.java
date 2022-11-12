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

package org.apache.shardingsphere.test.sql.parser.parameterized.loader;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.Case;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cases loader.
 */
@Getter
public abstract class CasesLoader {
    
    private final Map<String, Case> cases;
    
    public CasesLoader(final String rootDirection) {
        cases = load(rootDirection);
    }
    
    @SneakyThrows({JAXBException.class, IOException.class})
    private Map<String, Case> load(final String path) {
        File file = new File(CasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(file, path) : loadFromTargetDirectory(path);
    }
    
    private Map<String, Case> loadFromJar(final File file, final String path) throws JAXBException {
        Map<String, Case> result = new TreeMap<>();
        for (String each : TestCaseFileLoader.loadFileNamesFromJar(file, path)) {
            buildCaseMap(result, CasesLoader.class.getClassLoader().getResourceAsStream(each));
        }
        return result;
    }
    
    private Map<String, Case> loadFromTargetDirectory(final String path) throws JAXBException, FileNotFoundException {
        Map<String, Case> result = new TreeMap<>();
        for (File each : TestCaseFileLoader.loadFilesFromTargetDirectory(path)) {
            buildCaseMap(result, new FileInputStream(each));
        }
        return result;
    }
    
    /**
     * build case map.
     * @param caseMap result map
     * @param inputStream xml inputStream
     * @throws JAXBException JAXBException
     */
    protected abstract void buildCaseMap(Map<String, Case> caseMap, InputStream inputStream) throws JAXBException;
    
    /**
     * Get test parameters for junit parameterized test cases.
     *
     * @param databaseTypes database types
     * @return test parameters for junit parameterized test cases
     */
    protected abstract Collection<Object[]> getTestParameters(Collection<String> databaseTypes);
    
    /**
     * Get case value.
     *
     * @param sqlCaseId case ID
     * @param sqlCaseType SQL case type
     * @param params SQL parameters
     * @param databaseType databaseType
     * @return SQL
     */
    public abstract String getCaseValue(String sqlCaseId, SQLCaseType sqlCaseType, List<?> params, String databaseType);
    
    /**
     * Get all SQL case IDs.
     * 
     * @return all SQL case IDs
     */
    public Collection<String> getAllSQLCaseIDs() {
        return cases.keySet();
    }
}
