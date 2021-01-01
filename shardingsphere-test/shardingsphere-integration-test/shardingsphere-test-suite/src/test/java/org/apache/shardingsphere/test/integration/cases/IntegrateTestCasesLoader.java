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

package org.apache.shardingsphere.test.integration.cases;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;

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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Integrate test cases loader.
 */
public final class IntegrateTestCasesLoader {
    
    private static final IntegrateTestCasesLoader INSTANCE = new IntegrateTestCasesLoader();
    
    private final Map<IntegrateTestCaseType, Collection<IntegrateTestCaseContext>> testCaseContexts = new LinkedHashMap<>();
    
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
    public Collection<IntegrateTestCaseContext> getTestCaseContexts(final IntegrateTestCaseType caseType) {
        testCaseContexts.putIfAbsent(caseType, loadIntegrateTestCaseContexts(caseType));
        return testCaseContexts.get(caseType);
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class, JAXBException.class})
    private Collection<IntegrateTestCaseContext> loadIntegrateTestCaseContexts(final IntegrateTestCaseType caseType) {
        URL url = IntegrateTestCasesLoader.class.getClassLoader().getResource("integrate/cases/");
        Preconditions.checkNotNull(url, "Can not find integrate test cases.");
        return loadIntegrateTestCaseContexts(url, caseType);
    }
    
    private Collection<IntegrateTestCaseContext> loadIntegrateTestCaseContexts(final URL url, final IntegrateTestCaseType caseType) throws IOException, URISyntaxException, JAXBException {
        Collection<File> files = getFiles(url, caseType);
        Preconditions.checkNotNull(files, "Can not find integrate test cases.");
        Collection<IntegrateTestCaseContext> result = new LinkedList<>();
        for (File each : files) {
            result.addAll(getIntegrateTestCaseContexts(each));
        }
        return result;
    }
    
    private static Collection<File> getFiles(final URL url, final IntegrateTestCaseType caseType) throws IOException, URISyntaxException {
        Collection<File> result = new LinkedList<>();
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
    
    private Collection<IntegrateTestCaseContext> getIntegrateTestCaseContexts(final File file) throws IOException, JAXBException {
        return unmarshal(file.getPath()).getTestCases().stream().map(each -> new IntegrateTestCaseContext(each, file.getParent())).collect(Collectors.toList());
    }
    
    private static IntegrateTestCases unmarshal(final String integrateCasesFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(integrateCasesFile)) {
            return (IntegrateTestCases) JAXBContext.newInstance(IntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
        }
    }
}
