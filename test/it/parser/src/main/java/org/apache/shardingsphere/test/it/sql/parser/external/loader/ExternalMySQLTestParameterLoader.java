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

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLParserTestParameter;
import org.apache.shardingsphere.test.loader.AbstractTestParameterLoader;
import org.apache.shardingsphere.test.loader.strategy.TestParameterLoadStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * External MySQL SQL parser test parameter loader.
 */
public final class ExternalMySQLTestParameterLoader extends AbstractTestParameterLoader<ExternalSQLParserTestParameter> {

    private static final int DELIMITER_COMMAND_LENGTH = "DELIMITER".length();

    public ExternalMySQLTestParameterLoader(final TestParameterLoadStrategy loadStrategy) {
        super(loadStrategy);
    }
    
    /**
     * Create external SQL parser test parameters.
     * 
     * @param sqlCaseFileName SQL case file name
     * @param sqlCaseFileContent SQL case file content
     * @param resultFileContent result file content
     * @param databaseType database type
     * @param reportType report type
     * @return external SQL parser test parameters
     */
    public Collection<ExternalSQLParserTestParameter> createTestParameters(final String sqlCaseFileName,
                                                                           final List<String> sqlCaseFileContent,
                                                                           final List<String> resultFileContent, final String databaseType, final String reportType) {
        Collection<ExternalSQLParserTestParameter> result = new LinkedList<>();
        List<String> sqlLines = new ArrayList<>();
        int sqlCaseIndex = 1;
        String delimiter = ";";
        for (String line : sqlCaseFileContent) {
            String trimLine = line.trim();
            if (trimLine.isEmpty() || 0 == sqlLines.size() && isComment(trimLine)) {
                continue;
            }
            if (0 == sqlLines.size() && trimLine.toUpperCase().startsWith("DELIMITER")) {
                delimiter = getNewDelimiter(trimLine, delimiter);
                continue;
            }
            sqlLines.add(trimLine);
            if (trimLine.endsWith(delimiter)) {
                if (existInResultContent(resultFileContent, sqlLines)) {
                    String sqlCaseId = sqlCaseFileName + sqlCaseIndex++;
                    String sql = String.join("\n", sqlLines);
                    sql = sql.substring(0, sql.length() - delimiter.length());
                    result.add(new ExternalSQLParserTestParameter(sqlCaseId, databaseType, sql, reportType));
                }
                sqlLines.clear();
            }
        }
        return result;
    }
    
    private String getNewDelimiter(final String trimSql, final String delimiter) {
        String newDelimiter = trimSql
                .substring(DELIMITER_COMMAND_LENGTH, trimSql.endsWith(delimiter) ? trimSql.length() - delimiter.length() : trimSql.length())
                .trim();
        if (newDelimiter.startsWith("\"") && newDelimiter.endsWith("\"") || newDelimiter.startsWith("'") && newDelimiter.endsWith("'")) {
            newDelimiter = newDelimiter.substring(1, newDelimiter.length() - 1);
        }
        return newDelimiter.isEmpty() ? delimiter : newDelimiter;
    }
    
    private boolean isComment(final String statement) {
        return statement.startsWith("#") || statement.startsWith("/") || statement.startsWith("--") || statement.startsWith(":") || statement.startsWith("\\");
    }
    
    private boolean existInResultContent(final List<String> resultLines, final List<String> sqlLines) {
        int nextLineIndex = findSQLNextLineIndex(resultLines, sqlLines);
        return -1 != nextLineIndex && (nextLineIndex == resultLines.size() || !resultLines.get(nextLineIndex).contains("ERROR"));
    }
    
    private int findSQLNextLineIndex(final List<String> resultLines, final List<String> sqlLines) {
        int completedSQLIndex = 0;
        for (int resultIndex = 0; resultIndex < resultLines.size(); resultIndex++) {
            if (Objects.equals(sqlLines.get(completedSQLIndex), resultLines.get(resultIndex).trim())) {
                if (++completedSQLIndex == sqlLines.size()) {
                    return resultIndex + 1;
                }
            } else {
                completedSQLIndex = 0;
            }
        }
        return -1;
    }
}
