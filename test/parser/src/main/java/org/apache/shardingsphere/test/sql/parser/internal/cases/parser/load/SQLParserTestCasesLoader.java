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

package org.apache.shardingsphere.test.sql.parser.internal.cases.parser.load;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.jaxb.RootSQLParserTestCases;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.cases.CaseFileLoader;

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
 * SQL parser test cases loader.
 */
public final class SQLParserTestCasesLoader {
    
    /**
     * Load SQL parser test cases.
     * 
     * @param rootDirectory root directory
     * @return loaded cases
     */
    @SneakyThrows({JAXBException.class, IOException.class})
    public Map<String, SQLParserTestCase> load(final String rootDirectory) {
        File file = new File(SQLParserTestCasesLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile() ? loadFromJar(rootDirectory, file) : loadFromDirectory(rootDirectory);
    }
    
    private Map<String, SQLParserTestCase> loadFromJar(final String rootDirectory, final File file) throws JAXBException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (String each : CaseFileLoader.loadFileNamesFromJar(file, rootDirectory)) {
            Map<String, SQLParserTestCase> sqlParserTestCases = createSQLParserTestCases(SQLParserTestCasesLoader.class.getClassLoader().getResourceAsStream(each));
            checkDuplicate(result, sqlParserTestCases);
            result.putAll(sqlParserTestCases);
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> loadFromDirectory(final String rootDirectory) throws IOException, JAXBException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (File each : CaseFileLoader.loadFilesFromDirectory(rootDirectory)) {
            try (FileInputStream fileInputStream = new FileInputStream(each)) {
                Map<String, SQLParserTestCase> sqlParserTestCases = createSQLParserTestCases(fileInputStream);
                checkDuplicate(result, sqlParserTestCases);
                result.putAll(sqlParserTestCases);
            }
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> createSQLParserTestCases(final InputStream inputStream) throws JAXBException {
        return ((RootSQLParserTestCases) JAXBContext.newInstance(RootSQLParserTestCases.class).createUnmarshaller().unmarshal(inputStream)).getAllSQLParserTestCases();
    }
    
    private void checkDuplicate(final Map<String, SQLParserTestCase> existedSQLParserTestCases, final Map<String, SQLParserTestCase> newSQLParserTestCases) {
        Collection<String> existedSQLParserTestCaseIds = new HashSet<>(existedSQLParserTestCases.keySet());
        existedSQLParserTestCaseIds.retainAll(newSQLParserTestCases.keySet());
        Preconditions.checkState(existedSQLParserTestCaseIds.isEmpty(), "Find duplicated SQL Case IDs: %s", existedSQLParserTestCaseIds);
    }
}
