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

import java.util.Collection;
import java.util.LinkedList;

/**
 * External SQL parser test parameter loader.
 */
public final class ExternalSQLParserTestParameterLoader extends AbstractTestParameterLoader<ExternalSQLParserTestParameter> {
    
    public ExternalSQLParserTestParameterLoader(final TestParameterLoadStrategy loadStrategy) {
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
    
    private boolean isInProcedure(final boolean inProcedure, final String statementLines) {
        if (statementLines.contains("{") && statementLines.contains("}")) {
            return inProcedure;
        }
        return (statementLines.contains("{") || statementLines.contains("}") || statementLines.contains("$$")) != inProcedure;
    }
    
    private String getStatement(final String completedSQL, final String rawSQLLine, final boolean inProcedure) {
        return (rawSQLLine.isEmpty() || isComment(rawSQLLine)) && !inProcedure ? "" : completedSQL + rawSQLLine + " ";
    }
    
    private boolean isComment(final String statement) {
        return statement.startsWith("#") || statement.startsWith("/") || statement.startsWith("--") || statement.startsWith(":") || statement.startsWith("\\");
    }
    
    private int searchInResultContent(final int resultIndex, final String[] resultLines, final String completedSQL, final int statementLines) {
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
