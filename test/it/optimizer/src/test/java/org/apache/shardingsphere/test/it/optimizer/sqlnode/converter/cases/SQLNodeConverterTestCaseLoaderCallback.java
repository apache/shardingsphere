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

package org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.jaxb.RootSQLNodeConverterTestCases;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.jaxb.SQLNodeConverterTestCase;
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
 * SQL node converter test case loader callback.
 */
public final class SQLNodeConverterTestCaseLoaderCallback implements CaseLoaderCallback<SQLNodeConverterTestCase> {
    
    @Override
    public Map<String, SQLNodeConverterTestCase> loadFromJar(final File jarFile, final String rootDirectory) throws JAXBException {
        Map<String, SQLNodeConverterTestCase> result = new HashMap<>(Short.MAX_VALUE, 1F);
        for (String each : CaseFileLoader.loadFileNamesFromJar(jarFile, rootDirectory)) {
            Map<String, SQLNodeConverterTestCase> testCases = createTestCases(Thread.currentThread().getContextClassLoader().getResourceAsStream(each));
            checkDuplicatedTestCases(testCases, result);
            result.putAll(testCases);
        }
        return result;
    }
    
    @Override
    public Map<String, SQLNodeConverterTestCase> loadFromDirectory(final String rootDirectory) throws IOException, JAXBException {
        Map<String, SQLNodeConverterTestCase> result = new HashMap<>(Short.MAX_VALUE, 1F);
        for (File each : CaseFileLoader.loadFilesFromDirectory(rootDirectory)) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(each.toURI()))) {
                Map<String, SQLNodeConverterTestCase> testCases = createTestCases(inputStream);
                checkDuplicatedTestCases(testCases, result);
                result.putAll(testCases);
            }
        }
        return result;
    }
    
    private Map<String, SQLNodeConverterTestCase> createTestCases(final InputStream inputStream) throws JAXBException {
        return ((RootSQLNodeConverterTestCases) JAXBContext.newInstance(RootSQLNodeConverterTestCases.class).createUnmarshaller().unmarshal(inputStream)).getTestCases();
    }
    
    private void checkDuplicatedTestCases(final Map<String, SQLNodeConverterTestCase> newTestCases, final Map<String, SQLNodeConverterTestCase> existedTestCases) {
        Collection<String> caseKeys = new HashSet<>(newTestCases.keySet());
        caseKeys.retainAll(existedTestCases.keySet());
        Preconditions.checkState(caseKeys.isEmpty(), "Find duplicated SQL Case keys: %s", caseKeys);
    }
}
