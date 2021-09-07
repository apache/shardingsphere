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

package org.apache.shardingsphere.infra.parser.sql;

import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParserContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement parser executor.
 */
public final class SQLStatementParserExecutor {
    
    private final SQLParserEngine parserEngine;
    
    private final SQLVisitorEngine visitorEngine;
    
    public SQLStatementParserExecutor(final String databaseType) {
        parserEngine = new SQLParserEngine(databaseType);
        visitorEngine = new SQLVisitorEngine(databaseType, "STATEMENT", new Properties());
    }
    
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @return SQL statement
     */
    public SQLStatement parse(final String sql) {
        ParserContext parserContext = parserEngine.parse(sql, false);
        SQLStatement result = visitorEngine.visit(parserContext.getParseTree());
        handleComments(result, parserContext);
        return result;
    }
    
    private void handleComments(final SQLStatement sqlStatement, final ParserContext parserContext) {
        if (sqlStatement instanceof AbstractSQLStatement) {
            ((AbstractSQLStatement) sqlStatement).setCommentsSegments(parserContext.getHiddenTokens().stream()
                    .map(each -> new CommentsSegment(each.getText(), each.getStartIndex(), each.getStopIndex())).collect(Collectors.toList()));
        }
    }
}
