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

import org.apache.shardingsphere.test.loader.ExternalSQLParserTestParameter;
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
    
    @Override
    public Collection<ExternalSQLParserTestParameter> createTestParameters(final String sqlCaseFileName, final List<String> sqlCaseFileContent,
                                                                           final List<String> resultFileContent, final String databaseType, final String reportType) {
        Collection<ExternalSQLParserTestParameter> result = new LinkedList<>();
        List<String> lines = new ArrayList<>();
        int sqlCaseIndex = 1;
        String delimiter = ";";
        for (String each : sqlCaseFileContent) {
            String line = each.trim();
            if (line.isEmpty() || lines.isEmpty() && SQLLineComment.isComment(line)) {
                continue;
            }
            if (lines.isEmpty() && line.toUpperCase().startsWith("DELIMITER")) {
                delimiter = getNewDelimiter(line, delimiter);
                continue;
            }
            lines.add(line);
            if (line.endsWith(delimiter)) {
                if (existInResultContent(resultFileContent, lines)) {
                    String sqlCaseId = sqlCaseFileName + sqlCaseIndex++;
                    String sql = String.join("\n", lines);
                    sql = sql.substring(0, sql.length() - delimiter.length());
                    result.add(new ExternalSQLParserTestParameter(sqlCaseId, databaseType, sql, reportType));
                }
                lines.clear();
            }
        }
        return result;
    }
    
    private String getNewDelimiter(final String sql, final String delimiter) {
        String newDelimiter = sql.substring(DELIMITER_COMMAND_LENGTH, sql.endsWith(delimiter) ? sql.length() - delimiter.length() : sql.length()).trim();
        if (newDelimiter.startsWith("\"") && newDelimiter.endsWith("\"") || newDelimiter.startsWith("'") && newDelimiter.endsWith("'")) {
            newDelimiter = newDelimiter.substring(1, newDelimiter.length() - 1);
        }
        return newDelimiter.isEmpty() ? delimiter : newDelimiter;
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
