/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.integrate.asserts;

import io.shardingjdbc.test.sql.SQLCaseType;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;

/**
 * SQL statement assert message.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLStatementAssertMessage {
    
    private final SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private final ParserResultSetLoader parserResultSetLoader = ParserResultSetLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final SQLCaseType sqlCaseType;
    
    /**
     * Get full assert message.
     * 
     * @param assertMessage assert message
     * @return full assert message
     */
    public String getFullAssertMessage(final String assertMessage) {
        StringBuilder result = new StringBuilder(System.getProperty("line.separator"));
        result.append("SQL Case ID : ");
        result.append(sqlCaseId);
        result.append(System.getProperty("line.separator"));
        result.append("SQL         : ");
        if (SQLCaseType.Placeholder == sqlCaseType) {
            result.append(sqlCasesLoader.getSupportedPlaceholderSQL(sqlCaseId));
            result.append(System.getProperty("line.separator"));
            result.append("SQL Params  : ");
            result.append(parserResultSetLoader.getParserResult(sqlCaseId).getParameters());
            result.append(System.getProperty("line.separator"));
        } else {
            result.append(sqlCasesLoader.getSupportedLiteralSQL(sqlCaseId, parserResultSetLoader.getParserResult(sqlCaseId).getParameters()));
        }
        result.append(System.getProperty("line.separator"));
        result.append(assertMessage);
        result.append(System.getProperty("line.separator"));
        return result.toString();
    }
}
