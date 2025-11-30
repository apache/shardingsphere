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

package org.apache.shardingsphere.test.it.sql.parser.external.loader.template.type;

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.SQLLineComment;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Standard external test parameter load template.
 */
public final class StandardExternalTestParameterLoadTemplate implements ExternalTestParameterLoadTemplate {
    
    @Override
    public Collection<ExternalSQLTestParameter> load(final String sqlCaseFileName, final List<String> sqlCaseFileContent,
                                                     final List<String> resultFileContent, final String databaseType, final String reportType) {
        Collection<ExternalSQLTestParameter> result = new LinkedList<>();
        String completedSQL = "";
        int sqlCaseEnum = 1;
        int statementLines = 0;
        int resultIndex = 0;
        boolean inProcedure = false;
        for (String each : sqlCaseFileContent) {
            inProcedure = isInProcedure(inProcedure, each.trim());
            completedSQL = getStatement(completedSQL, each.trim(), inProcedure);
            statementLines = completedSQL.isEmpty() ? 0 : statementLines + 1;
            if (completedSQL.contains(";") && !inProcedure) {
                resultIndex = searchInResultContent(resultIndex, resultFileContent, completedSQL, statementLines);
                if (resultIndex >= resultFileContent.size() || !resultFileContent.get(resultIndex).contains("ERROR")) {
                    String sqlCaseId = sqlCaseFileName + sqlCaseEnum;
                    result.add(new ExternalSQLTestParameter(sqlCaseId, databaseType, completedSQL, reportType));
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
        return (rawSQLLine.isEmpty() || SQLLineComment.isComment(rawSQLLine)) && !inProcedure ? "" : completedSQL + rawSQLLine + " ";
    }
    
    private int searchInResultContent(final int resultIndex, final List<String> resultLines, final String completedSQL, final int statementLines) {
        int index = resultIndex;
        while (index < resultLines.size() && !completedSQL.startsWith(resultLines.get(index).trim())) {
            index++;
        }
        if (index != resultLines.size()) {
            return index + statementLines;
        }
        return resultIndex;
    }
}
