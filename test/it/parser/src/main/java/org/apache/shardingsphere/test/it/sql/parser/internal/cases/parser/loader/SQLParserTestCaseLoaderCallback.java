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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.loader;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.RootSQLParserTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseFileLoader;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseLoaderCallback;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * SQL parser test case loader callback.
 */
public final class SQLParserTestCaseLoaderCallback implements CaseLoaderCallback<SQLParserTestCase> {
    
    @Override
    public Map<String, SQLParserTestCase> loadFromJar(final File jarFile, final String rootDirectory) throws JAXBException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1F);
        for (String each : CaseFileLoader.loadFileNamesFromJar(jarFile, rootDirectory)) {
            Map<String, SQLParserTestCase> testCases = createTestCases(Thread.currentThread().getContextClassLoader().getResourceAsStream(each));
            checkDuplicatedTestCases(testCases, result);
            result.putAll(testCases);
        }
        return result;
    }
    
    @Override
    public Map<String, SQLParserTestCase> loadFromDirectory(final String rootDirectory) throws IOException, JAXBException {
        Map<String, SQLParserTestCase> result = new HashMap<>(Short.MAX_VALUE, 1F);
        for (File each : CaseFileLoader.loadFilesFromDirectory(rootDirectory)) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(each.toURI()))) {
                Map<String, SQLParserTestCase> testCases = createTestCases(inputStream);
                checkDuplicatedTestCases(testCases, result);
                result.putAll(testCases);
            }
        }
        return result;
    }
    
    private Map<String, SQLParserTestCase> createTestCases(final InputStream inputStream) throws JAXBException {
        return ((RootSQLParserTestCases) JAXBContext.newInstance(RootSQLParserTestCases.class).createUnmarshaller().unmarshal(inputStream)).getAllCases();
    }
    
    private void checkDuplicatedTestCases(final Map<String, SQLParserTestCase> newTestCases, final Map<String, SQLParserTestCase> existedTestCases) {
        Collection<String> caseIds = new HashSet<>(newTestCases.keySet());
        caseIds.retainAll(existedTestCases.keySet());
        Preconditions.checkState(caseIds.isEmpty(), "Find duplicated SQL Case IDs: %s", caseIds);
    }
}
