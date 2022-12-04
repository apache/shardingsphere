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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLParserTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.strategy.TestParameterLoadStrategy;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.summary.FileSummary;
import org.apache.shardingsphere.test.it.sql.parser.external.env.SQLParserExternalITEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * External SQL parser test parameter loader.
 */
@RequiredArgsConstructor
@Slf4j
public final class ExternalSQLParserTestParameterLoader {
    
    private final TestParameterLoadStrategy loadStrategy;
    
    /**
     * Load SQL parser test parameters.
     *
     * @param sqlCaseURI SQL case URI
     * @param resultURI result URI
     * @param databaseType database type
     * @param reportType report type
     * @return loaded test parameters
     */
    public Collection<ExternalSQLParserTestParameter> load(final URI sqlCaseURI, final URI resultURI, final String databaseType, final String reportType) {
        if (!SQLParserExternalITEnvironment.getInstance().isSqlParserITEnabled()) {
            return Collections.emptyList();
        }
        Collection<ExternalSQLParserTestParameter> result = new LinkedList<>();
        Map<String, FileSummary> sqlCaseFileSummaries = loadStrategy.loadSQLCaseFileSummaries(sqlCaseURI).stream().collect(Collectors.toMap(FileSummary::getFileName, v -> v, (k, v) -> v));
        Map<String, FileSummary> resultFileSummaries = loadStrategy.loadSQLCaseFileSummaries(resultURI).stream().collect(Collectors.toMap(FileSummary::getFileName, v -> v, (k, v) -> v));
        for (Entry<String, FileSummary> entry : sqlCaseFileSummaries.entrySet()) {
            String fileName = entry.getKey();
            String sqlCaseFileContent = loadContent(URI.create(entry.getValue().getAccessURI()));
            String resultFileContent = resultFileSummaries.containsKey(fileName) ? loadContent(URI.create(resultFileSummaries.get(fileName).getAccessURI())) : "";
            result.addAll(createTestParameters(fileName, sqlCaseFileContent, resultFileContent, databaseType, reportType));
        }
        if (result.isEmpty()) {
            result.add(new ExternalSQLParserTestParameter("", databaseType, "", reportType));
        }
        return result;
    }
    
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
    
    private Collection<ExternalSQLParserTestParameter> createTestParameters(final String sqlCaseFileName,
                                                                            final String sqlCaseFileContent, final String resultFileContent, final String databaseType, final String reportType) {
        Collection<ExternalSQLParserTestParameter> result = new LinkedList<>();
        String[] rawCaseLines = sqlCaseFileContent.split("\n");
        String[] rawResultLines = resultFileContent.split("\n");
        String completedSQL = "";
        int sqlCaseEnum = 1;
        int statementLines = 0;
        int resultIndex = 0;
        boolean inProcedure = false;
        for (String each : rawCaseLines) {
            inProcedure = isInProcedure(inProcedure, each.trim());
            completedSQL = getStatement(completedSQL, each.trim(), inProcedure);
            statementLines = completedSQL.isEmpty() ? 0 : statementLines + 1;
            if (completedSQL.contains(";") && !inProcedure) {
                resultIndex = searchInResultContent(resultIndex, rawResultLines, completedSQL, statementLines);
                if (resultIndex >= rawResultLines.length || !rawResultLines[resultIndex].contains("ERROR")) {
                    String sqlCaseId = sqlCaseFileName + sqlCaseEnum;
                    result.add(new ExternalSQLParserTestParameter(sqlCaseId, databaseType, completedSQL, reportType));
                    sqlCaseEnum++;
                }
                completedSQL = "";
                statementLines = 0;
            }
        }
        return result;
    }
    
    private static boolean isInProcedure(final boolean inProcedure, final String statementLines) {
        if (statementLines.contains("{") && statementLines.contains("}")) {
            return inProcedure;
        }
        return (statementLines.contains("{") || statementLines.contains("}") || statementLines.contains("$$")) != inProcedure;
    }
    
    private static String getStatement(final String completedSQL, final String rawSQLLine, final boolean inProcedure) {
        return (rawSQLLine.isEmpty() || isComment(rawSQLLine)) && !inProcedure ? "" : completedSQL + rawSQLLine + " ";
    }
    
    private static boolean isComment(final String statement) {
        return statement.startsWith("#") || statement.startsWith("/") || statement.startsWith("--") || statement.startsWith(":") || statement.startsWith("\\");
    }
    
    private static int searchInResultContent(final int resultIndex, final String[] resultLines, final String completedSQL, final int statementLines) {
        int index = resultIndex;
        while (index < resultLines.length && !completedSQL.startsWith(resultLines[index].trim())) {
            index++;
        }
        if (index != resultLines.length) {
            return index + statementLines;
        }
        return resultIndex;
    }
}
