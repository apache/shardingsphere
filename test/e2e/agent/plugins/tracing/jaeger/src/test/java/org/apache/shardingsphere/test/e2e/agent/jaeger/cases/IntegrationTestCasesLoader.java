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

package org.apache.shardingsphere.test.e2e.agent.jaeger.cases;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
import java.util.LinkedList;
import java.util.Objects;

/**
 * Integration test cases loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegrationTestCasesLoader {
    
    private static final String FILE_EXTENSION = ".xml";
    
    private static final IntegrationTestCasesLoader INSTANCE = new IntegrationTestCasesLoader();
    
    private Collection<SpanTestCase> integrationTestCases;
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Load integration test cases.
     *
     * @param adapter adapter
     * @return integration test cases
     */
    @SneakyThrows({IOException.class, URISyntaxException.class, JAXBException.class})
    public Collection<SpanTestCase> loadIntegrationTestCases(final String adapter) {
        if (null != integrationTestCases) {
            return integrationTestCases;
        }
        integrationTestCases = new LinkedList<>();
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(String.format("cases/%s", adapter)));
        Collection<File> files = getFiles(url);
        for (File each : files) {
            integrationTestCases.addAll(unmarshal(each.getPath()).getTestCases());
        }
        return integrationTestCases;
    }
    
    private Collection<File> getFiles(final URL url) throws IOException, URISyntaxException {
        Collection<File> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
                if (file.getFileName().toString().endsWith(FILE_EXTENSION)) {
                    result.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    private IntegrationTestCases unmarshal(final String integrateCasesFile) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(integrateCasesFile)) {
            return (IntegrationTestCases) JAXBContext.newInstance(IntegrationTestCases.class).createUnmarshaller().unmarshal(reader);
        }
    }
}
