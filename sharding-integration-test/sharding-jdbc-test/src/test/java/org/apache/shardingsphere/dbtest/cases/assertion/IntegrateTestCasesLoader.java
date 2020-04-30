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

package org.apache.shardingsphere.dbtest.cases.assertion;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbtest.cases.assertion.dcl.DCLIntegrateTestCases;
import org.apache.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCases;
import org.apache.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCases;
import org.apache.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCases;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCases;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

/**
 * Integrate test cases loader.
 */
@Slf4j
public final class IntegrateTestCasesLoader {
    
    // TODO it better to add file prefix filed to SQLType enum
    private static final String DQL_INTEGRATE_TEST_CASES_FILE_PREFIX = "dql-integrate-test-cases";
    
    private static final String DML_INTEGRATE_TEST_CASES_FILE_PREFIX = "dml-integrate-test-cases";
    
    private static final String DDL_INTEGRATE_TEST_CASES_FILE_PREFIX = "ddl-integrate-test-cases";
    
    private static final String DCL_INTEGRATE_TEST_CASES_FILE_PREFIX = "dcl-integrate-test-cases";
    
    private static final IntegrateTestCasesLoader INSTANCE = new IntegrateTestCasesLoader();
    
    @Getter
    private final List<? extends IntegrateTestCase> dqlIntegrateTestCases;
    
    @Getter
    private final List<? extends IntegrateTestCase> dmlIntegrateTestCases;
    
    @Getter
    private final List<? extends IntegrateTestCase> ddlIntegrateTestCases;
    
    @Getter
    private final List<? extends IntegrateTestCase> dclIntegrateTestCases;
    
    @SneakyThrows
    private IntegrateTestCasesLoader() {
        dqlIntegrateTestCases = loadIntegrateTestCases(DQL_INTEGRATE_TEST_CASES_FILE_PREFIX);
        dmlIntegrateTestCases = loadIntegrateTestCases(DML_INTEGRATE_TEST_CASES_FILE_PREFIX);
        ddlIntegrateTestCases = loadIntegrateTestCases(DDL_INTEGRATE_TEST_CASES_FILE_PREFIX);
        dclIntegrateTestCases = loadIntegrateTestCases(DCL_INTEGRATE_TEST_CASES_FILE_PREFIX);
    }
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    private List<? extends IntegrateTestCase> loadIntegrateTestCases(final String filePrefix) throws IOException, URISyntaxException, JAXBException {
        URL url = IntegrateTestCasesLoader.class.getClassLoader().getResource("integrate/cases/");
        Preconditions.checkNotNull(url, "Cannot found integrate test cases.");
        return loadIntegrateTestCases(url, filePrefix);
    }
    
    private List<? extends IntegrateTestCase> loadIntegrateTestCases(final URL url, final String filePrefix) throws IOException, URISyntaxException, JAXBException {
        List<String> files = getFiles(url, filePrefix);
        Preconditions.checkNotNull(files, "Cannot found integrate test cases.");
        List<? extends IntegrateTestCase> result = new LinkedList<>();
        for (String each : files) {
            result = unmarshal(each, filePrefix).getIntegrateTestCases();
            result.forEach(testCase -> testCase.setPath(each));
        }
        return result;
    }
    
    private static List<String> getFiles(final URL url, final String filePrefix) throws IOException, URISyntaxException {
        final List<String> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
                if (file.getFileName().toString().startsWith(filePrefix) && file.getFileName().toString().endsWith(".xml")) {
                    result.add(file.toFile().getPath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    private static IntegrateTestCases unmarshal(final String integrateCasesFile, final String filePrefix) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(integrateCasesFile)) {
            if (DQL_INTEGRATE_TEST_CASES_FILE_PREFIX.equals(filePrefix)) {
                return (DQLIntegrateTestCases) JAXBContext.newInstance(DQLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
            }
            if (DML_INTEGRATE_TEST_CASES_FILE_PREFIX.equals(filePrefix)) {
                return (DMLIntegrateTestCases) JAXBContext.newInstance(DMLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
            }
            if (DDL_INTEGRATE_TEST_CASES_FILE_PREFIX.equals(filePrefix)) {
                return (DDLIntegrateTestCases) JAXBContext.newInstance(DDLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
            }
            if (DCL_INTEGRATE_TEST_CASES_FILE_PREFIX.equals(filePrefix)) {
                return (DCLIntegrateTestCases) JAXBContext.newInstance(DCLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
            }
            throw new UnsupportedOperationException(filePrefix);
        }
    }
    
    /**
     * Count all data set test cases.
     * 
     * @return count of all data set test cases
     */
    public int countAllDataSetTestCases() {
        return dqlIntegrateTestCases.size() + dmlIntegrateTestCases.size() + ddlIntegrateTestCases.size() + dclIntegrateTestCases.size();
    }
}
