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

package org.apache.shardingsphere.test.integration.cases.assertion;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.dcl.DCLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.ddl.DDLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.dml.DMLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.dql.DQLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.root.IntegrateTestCases;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Integrate test cases loader.
 */
public final class IntegrateTestCasesLoader {
    
    private static final IntegrateTestCasesLoader INSTANCE = new IntegrateTestCasesLoader();
    
    private final Map<IntegrateTestCaseType, List<IntegrateTestCaseContext>> testCaseContexts = new LinkedHashMap<>();
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get integrate test case contexts.
     * 
     * @param caseType integration test case type
     * @return integrate test case contexts
     */
    public List<IntegrateTestCaseContext> getTestCaseContexts(final IntegrateTestCaseType caseType) {
        testCaseContexts.putIfAbsent(caseType, loadIntegrateTestCaseContexts(caseType));
        return testCaseContexts.get(caseType);
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class, JAXBException.class})
    private List<IntegrateTestCaseContext> loadIntegrateTestCaseContexts(final IntegrateTestCaseType caseType) {
        URL url = IntegrateTestCasesLoader.class.getClassLoader().getResource("integrate/cases/");
        Preconditions.checkNotNull(url, "Can not find integrate test cases.");
        return loadIntegrateTestCaseContexts(url, caseType);
    }
    
    private List<IntegrateTestCaseContext> loadIntegrateTestCaseContexts(final URL url, final IntegrateTestCaseType caseType) throws IOException, URISyntaxException, JAXBException {
        List<File> files = getFiles(url, caseType);
        Preconditions.checkNotNull(files, "Can not find integrate test cases.");
        List<IntegrateTestCaseContext> result = new LinkedList<>();
        for (File each : files) {
            result.addAll(getIntegrateTestCaseContexts(each, caseType));
        }
        return result;
    }
    
    private static List<File> getFiles(final URL url, final IntegrateTestCaseType caseType) throws IOException, URISyntaxException {
        List<File> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
                if (file.getFileName().toString().startsWith(caseType.getFilePrefix()) && file.getFileName().toString().endsWith(".xml")) {
                    result.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    private List<IntegrateTestCaseContext> getIntegrateTestCaseContexts(final File file, final IntegrateTestCaseType caseType) throws IOException, JAXBException {
        return unmarshal(file.getPath(), caseType).getIntegrateTestCases().stream().map(each -> new IntegrateTestCaseContext(each, file.getParent())).collect(Collectors.toList());
    }
    
    private static IntegrateTestCases unmarshal(final String integrateCasesFile, final IntegrateTestCaseType caseType) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(integrateCasesFile)) {
            switch (caseType) {
                case DQL:
                    return (DQLIntegrateTestCases) JAXBContext.newInstance(DQLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                case DML:
                    return (DMLIntegrateTestCases) JAXBContext.newInstance(DMLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                case DDL:
                    return (DDLIntegrateTestCases) JAXBContext.newInstance(DDLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                case DCL:
                    return (DCLIntegrateTestCases) JAXBContext.newInstance(DCLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                default:
                    throw new UnsupportedOperationException(caseType.getFilePrefix());
            }
        }
    }
}
