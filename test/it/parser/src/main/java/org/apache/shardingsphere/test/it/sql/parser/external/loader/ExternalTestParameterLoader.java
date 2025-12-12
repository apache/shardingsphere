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

package org.apache.shardingsphere.test.it.sql.parser.external.loader;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.env.ExternalEnvironmentContext;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.strategy.ExternalTestParameterLoadStrategy;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.summary.FileSummary;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * External test parameter loader.
 */
@RequiredArgsConstructor
public final class ExternalTestParameterLoader {
    
    private static final String TOKEN_KEY = "it.github.token";
    
    private final ExternalTestParameterLoadStrategy loadStrategy;
    
    private final ExternalTestParameterLoadTemplate loadTemplate;
    
    /**
     * Load test parameters.
     *
     * @param sqlCaseURI SQL case URI
     * @param resultURI result URI
     * @param databaseType database type
     * @param reportType report type
     * @return loaded test parameters
     */
    public Stream<ExternalSQLTestParameter> load(final URI sqlCaseURI, final URI resultURI, final String databaseType, final String reportType) {
        return load(sqlCaseURI, resultURI, databaseType, reportType, null);
    }
    
    /**
     * Load test parameters.
     *
     * @param sqlCaseURI SQL case URI
     * @param resultURI result URI
     * @param databaseType database type
     * @param reportType report type
     * @param caseRegex case regex
     * @return loaded test parameters
     */
    public Stream<ExternalSQLTestParameter> load(final URI sqlCaseURI, final URI resultURI, final String databaseType, final String reportType, final String caseRegex) {
        Collection<FileSummary> sqlCaseFileSummaries = loadStrategy.loadSQLCaseFileSummaries(sqlCaseURI);
        Collection<FileSummary> resultFileSummaries = loadStrategy.loadSQLCaseFileSummaries(resultURI);
        Map<String, FileSummary> resultFileSummaryMap =
                resultFileSummaries.stream().collect(Collectors.toMap(fileSummary -> Files.getNameWithoutExtension(fileSummary.getFileName()), Function.identity()));
        return sqlCaseFileSummaries.stream().filter(each -> StringUtils.isEmpty(caseRegex) || each.getFileName().matches(caseRegex)).flatMap(each -> {
            List<String> sqlCaseFileContent = loadContent(URI.create(each.getAccessURI()));
            String fileName = Files.getNameWithoutExtension(each.getFileName());
            Optional<FileSummary> resultFileSummary = Optional.ofNullable(resultFileSummaryMap.get(fileName));
            List<String> resultFileContent = resultFileSummary.map(summary -> loadContent(URI.create(summary.getAccessURI()))).orElse(Collections.emptyList());
            return loadTemplate.load(fileName, sqlCaseFileContent, resultFileContent, databaseType, reportType).stream();
        });
    }
    
    @SneakyThrows(IOException.class)
    private List<String> loadContent(final URI uri) {
        URLConnection urlConnection = uri.toURL().openConnection();
        String githubToken = ExternalEnvironmentContext.getInstance().getValue(TOKEN_KEY);
        if (!Strings.isNullOrEmpty(githubToken)) {
            urlConnection.setRequestProperty("Authorization", "Bearer " + githubToken);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}
