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

package org.apache.shardingsphere.test.loader;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.loader.strategy.TestParameterLoadStrategy;
import org.apache.shardingsphere.test.loader.summary.FileSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Test parameter loader.
 * 
 * @param <T> type of test parameter
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractTestParameterLoader<T> {
    
    private static final int DEFAULT_DOWNLOAD_THREADS = 4;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(DEFAULT_DOWNLOAD_THREADS);
    
    private final TestParameterLoadStrategy loadStrategy;
    
    /**
     * Load test parameters.
     *
     * @param sqlCaseURI SQL case URI
     * @param resultURI result URI
     * @param databaseType database type
     * @param reportType report type
     * @return loaded test parameters
     */
    @SneakyThrows
    public Collection<T> load(final URI sqlCaseURI, final URI resultURI, final String databaseType, final String reportType) {
        Collection<T> result = new LinkedList<>();
        Map<String, List<String>> sqlCaseFileContents = downloadAllBySummary(sqlCaseURI);
        Map<String, List<String>> resultFileContents = downloadAllBySummary(resultURI);
        for (Entry<String, List<String>> each : sqlCaseFileContents.entrySet()) {
            String fileName = each.getKey();
            List<String> sqlCaseFileContent = each.getValue();
            List<String> resultFileContent = resultFileContents.getOrDefault(fileName, Lists.newArrayList());
            result.addAll(createTestParameters(fileName, sqlCaseFileContent, resultFileContent, databaseType, reportType));
        }
        return result;
    }
    
    private Map<String, List<String>> downloadAllBySummary(final URI sqlCaseURI) throws InterruptedException {
        Map<String, List<String>> contents = new ConcurrentHashMap<>();
        Collection<FileSummary> fileSummaries = loadStrategy.loadSQLCaseFileSummaries(sqlCaseURI);
        executorService.invokeAll(fileSummaries.stream()
                .map(summary -> (Callable<Object>) () -> contents.put(summary.getFileName(), loadContent(URI.create(summary.getAccessURI()))))
                .collect(Collectors.toList()));
        return contents;
    }
    
    /**
     * Create test parameters.
     * 
     * @param sqlCaseFileName SQL case file name
     * @param sqlCaseFileContent SQL case file content
     * @param resultFileContent result file content
     * @param databaseType database type
     * @param reportType report type
     * @return test parameters
     */
    public abstract Collection<T> createTestParameters(String sqlCaseFileName, List<String> sqlCaseFileContent, List<String> resultFileContent, String databaseType, String reportType);
    
    private List<String> loadContent(final URI uri) {
        try (
                InputStreamReader in = new InputStreamReader(uri.toURL().openStream());
                BufferedReader reader = new BufferedReader(in)) {
            return reader.lines().collect(Collectors.toList());
        } catch (final IOException ex) {
            log.warn("Load failed, reason is: ", ex);
            return Lists.newArrayList();
        }
    }
}
