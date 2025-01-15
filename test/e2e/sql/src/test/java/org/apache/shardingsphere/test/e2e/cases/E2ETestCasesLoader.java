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

package org.apache.shardingsphere.test.e2e.cases;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.cases.casse.E2ETestCaseContext;
import org.apache.shardingsphere.test.e2e.framework.type.SQLCommandType;

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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * E2E test cases loader.
 */
public final class E2ETestCasesLoader {
    
    private static final String FILE_EXTENSION = ".xml";
    
    private static final E2ETestCasesLoader INSTANCE = new E2ETestCasesLoader();
    
    private final Map<SQLCommandType, Collection<E2ETestCaseContext>> testCaseContexts = new LinkedHashMap<>();
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static E2ETestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get E2E test case contexts.
     *
     * @param sqlCommandType SQL command type
     * @return E2E test case contexts
     */
    public Collection<E2ETestCaseContext> getTestCaseContexts(final SQLCommandType sqlCommandType) {
        return testCaseContexts.computeIfAbsent(sqlCommandType, this::loadE2ETestCaseContexts);
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class, JAXBException.class})
    private Collection<E2ETestCaseContext> loadE2ETestCaseContexts(final SQLCommandType sqlCommandType) {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("cases/"));
        return loadE2ETestCaseContexts(url, sqlCommandType);
    }
    
    private Collection<E2ETestCaseContext> loadE2ETestCaseContexts(final URL url, final SQLCommandType sqlCommandType) throws IOException, URISyntaxException, JAXBException {
        Collection<File> files = getFiles(url, sqlCommandType);
        Preconditions.checkNotNull(files, "Can not find E2E test cases.");
        Collection<E2ETestCaseContext> result = new LinkedList<>();
        for (File each : files) {
            result.addAll(getE2ETestCaseContexts(each));
        }
        return result;
    }
    
    private Collection<File> getFiles(final URL url, final SQLCommandType sqlCommandType) throws IOException, URISyntaxException {
        Collection<File> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
                if (file.getFileName().toString().startsWith(sqlCommandType.getFilePrefix()) && file.getFileName().toString().endsWith(FILE_EXTENSION)) {
                    result.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    private Collection<E2ETestCaseContext> getE2ETestCaseContexts(final File file) throws IOException, JAXBException {
        return unmarshal(file.getPath()).getTestCases().stream().map(each -> new E2ETestCaseContext(each, getParentPath(file))).collect(Collectors.toList());
    }
    
    private String getParentPath(final File file) {
        return file.getParent().replaceAll("(/cases/.+)/cases/.+", "$1");
    }
    
    private E2ETestCases unmarshal(final String e2eCasesFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(e2eCasesFile)) {
            return (E2ETestCases) JAXBContext.newInstance(E2ETestCases.class).createUnmarshaller().unmarshal(reader);
        }
    }
}
