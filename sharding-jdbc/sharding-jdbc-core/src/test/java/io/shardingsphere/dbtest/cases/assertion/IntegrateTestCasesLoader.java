/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.cases.assertion;

import com.google.common.base.Preconditions;
import io.shardingsphere.dbtest.cases.assertion.dcl.DCLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dcl.DCLIntegrateTestCases;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCases;
import io.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCases;
import io.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCases;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCases;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Integrate test cases loader.
 *
 * @author zhangliang
 * @author panjuan
 */
@Slf4j
public final class IntegrateTestCasesLoader {
    
    private static final String DQL_INTEGRATE_TEST_CASES_FILE_PREFIX = "dql-integrate-test-cases";
    
    private static final String DML_INTEGRATE_TEST_CASES_FILE_PREFIX = "dml-integrate-test-cases";
    
    private static final String DDL_INTEGRATE_TEST_CASES_FILE_PREFIX = "ddl-integrate-test-cases";
    
    private static final String DCL_INTEGRATE_TEST_CASES_FILE_PREFIX = "dcl-integrate-test-cases";
    
    private static final IntegrateTestCasesLoader INSTANCE = new IntegrateTestCasesLoader();
    
    private final Map<String, IntegrateTestCase> dqlIntegrateTestCaseMap;
    
    private final Map<String, IntegrateTestCase> dmlIntegrateTestCaseMap;
    
    private final Map<String, IntegrateTestCase> ddlIntegrateTestCaseMap;
    
    private final Map<String, IntegrateTestCase> dclIntegrateTestCaseMap;
    
    @SneakyThrows
    private IntegrateTestCasesLoader() {
        dqlIntegrateTestCaseMap = loadIntegrateTestCases(DQL_INTEGRATE_TEST_CASES_FILE_PREFIX);
        dmlIntegrateTestCaseMap = loadIntegrateTestCases(DML_INTEGRATE_TEST_CASES_FILE_PREFIX);
        ddlIntegrateTestCaseMap = loadIntegrateTestCases(DDL_INTEGRATE_TEST_CASES_FILE_PREFIX);
        dclIntegrateTestCaseMap = loadIntegrateTestCases(DCL_INTEGRATE_TEST_CASES_FILE_PREFIX);
    }
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    private Map<String, IntegrateTestCase> loadIntegrateTestCases(final String filePrefix) throws IOException, URISyntaxException, JAXBException {
        URL url = IntegrateTestCasesLoader.class.getClassLoader().getResource("integrate/cases/");
        Preconditions.checkNotNull(url, "Cannot found integrate test cases.");
        return new HashMap<>(loadIntegrateTestCases(url, filePrefix));
    }
    
    private Map<String, IntegrateTestCase> loadIntegrateTestCases(final URL url, final String filePrefix) throws IOException, URISyntaxException, JAXBException {
        List<String> files = getFiles(url, filePrefix);
        Preconditions.checkNotNull(files, "Cannot found integrate test cases.");
        Map<String, IntegrateTestCase> result = new HashMap<>(Short.MAX_VALUE, 1);
        for (String each : files) {
            result.putAll(new HashMap<>(loadIntegrateTestCases(each, unmarshal(each, filePrefix).getIntegrateTestCases())));
        }
        return result;
    }
    
    private Map<String, IntegrateTestCase> loadIntegrateTestCases(final String file, final List<? extends IntegrateTestCase> integrateTestCases) {
        Map<String, IntegrateTestCase> result = new HashMap<>(integrateTestCases.size(), 1);
        for (IntegrateTestCase each : integrateTestCases) {
            result.put(each.getSqlCaseId(), each);
            each.setPath(file);
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
    
    private static IntegrateTestCases unmarshal(final String assertFilePath, final String filePrefix) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(assertFilePath)) {
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
     * Get DQL integrate test case.
     * 
     * @param sqlCaseId SQL case ID
     * @return DQL integrate test case
     */
    public DQLIntegrateTestCase getDQLIntegrateTestCase(final String sqlCaseId) {
        // TODO resume when transfer finished
//        Preconditions.checkState(dqlIntegrateTestCaseMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        // TODO remove when transfer finished
        if (!dqlIntegrateTestCaseMap.containsKey(sqlCaseId)) {
            log.warn("Have not finishSuccess case `{}`", sqlCaseId);
        }
        return (DQLIntegrateTestCase) dqlIntegrateTestCaseMap.get(sqlCaseId);
    }
    
    /**
     * Get DML integrate test case.
     *
     * @param sqlCaseId SQL case ID
     * @return DQL integrate test case
     */
    public DMLIntegrateTestCase getDMLIntegrateTestCase(final String sqlCaseId) {
        // TODO resume when transfer finished
        //        Preconditions.checkState(dmlIntegrateTestCaseMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        // TODO remove when transfer finished
        if (!dmlIntegrateTestCaseMap.containsKey(sqlCaseId)) {
            log.warn("Have not finishSuccess case `{}`", sqlCaseId);
        }
        return (DMLIntegrateTestCase) dmlIntegrateTestCaseMap.get(sqlCaseId);
    }
    
    /**
     * Get DDL integrate test case.
     *
     * @param sqlCaseId SQL case ID
     * @return DDL integrate test case
     */
    public DDLIntegrateTestCase getDDLIntegrateTestCase(final String sqlCaseId) {
        // TODO resume when transfer finished
        //        Preconditions.checkState(ddlIntegrateTestCaseMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        // TODO remove when transfer finished
        if (!ddlIntegrateTestCaseMap.containsKey(sqlCaseId)) {
            log.warn("Have not finishSuccess case `{}`", sqlCaseId);
        }
        return (DDLIntegrateTestCase) ddlIntegrateTestCaseMap.get(sqlCaseId);
    }
    
    /**
     * Get DCL integrate test case.
     *
     * @param sqlCaseId SQL case ID
     * @return DCL integrate test case
     */
    public DCLIntegrateTestCase getDCLIntegrateTestCase(final String sqlCaseId) {
        // TODO resume when transfer finished
        //        Preconditions.checkState(ddlIntegrateTestCaseMap.containsKey(sqlCaseId), "Can't find SQL of id: " + sqlCaseId);
        // TODO remove when transfer finished
        if (!dclIntegrateTestCaseMap.containsKey(sqlCaseId)) {
            log.warn("Have not finishSuccess case `{}`", sqlCaseId);
        }
        return (DCLIntegrateTestCase) dclIntegrateTestCaseMap.get(sqlCaseId);
    }
    
    /**
     * Count all data set test cases.
     * 
     * @return count of all data set test cases
     */
    public int countAllDataSetTestCases() {
        return dqlIntegrateTestCaseMap.size() + dmlIntegrateTestCaseMap.size() + ddlIntegrateTestCaseMap.size() + dclIntegrateTestCaseMap.size();
    }
}
