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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.loader.strategy.TestParameterLoadStrategy;
import org.apache.shardingsphere.test.loader.summary.FileSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Test parameter loader.
 * 
 * @param <T> type of test parameter
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractTestParameterLoader<T> {
    
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
    public Collection<T> load(final URI sqlCaseURI, final URI resultURI, final String databaseType, final String reportType) {
        Collection<T> result = new LinkedList<>();
        Map<String, FileSummary> sqlCaseFileSummaries = loadStrategy.loadSQLCaseFileSummaries(sqlCaseURI).stream().collect(Collectors.toMap(FileSummary::getFileName, v -> v, (k, v) -> v));
        Map<String, FileSummary> resultFileSummaries = loadStrategy.loadSQLCaseFileSummaries(resultURI).stream().collect(Collectors.toMap(FileSummary::getFileName, v -> v, (k, v) -> v));
        for (Entry<String, FileSummary> each : sqlCaseFileSummaries.entrySet()) {
            String fileName = each.getKey();
            String sqlCaseFileContent = loadContent(URI.create(each.getValue().getAccessURI()));
            String resultFileContent = resultFileSummaries.containsKey(fileName) ? loadContent(URI.create(resultFileSummaries.get(fileName).getAccessURI())) : "";
            result.addAll(createTestParameters(fileName, sqlCaseFileContent, resultFileContent, databaseType, reportType));
        }
        return result;
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
    public abstract Collection<T> createTestParameters(String sqlCaseFileName, String sqlCaseFileContent,
                                                       String resultFileContent, String databaseType, String reportType);
    
    private String loadContent(final URI uri) {
        try (
                InputStreamReader in = new InputStreamReader(uri.toURL().openStream());
                BufferedReader reader = new BufferedReader(in)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException ex) {
            log.warn("Load failed, reason is: ", ex);
            return "";
        }
    }
}
