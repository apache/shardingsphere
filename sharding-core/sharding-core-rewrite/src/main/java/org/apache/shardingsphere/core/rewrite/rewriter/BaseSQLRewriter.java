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

package org.apache.shardingsphere.core.rewrite.rewriter;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.Substitutable;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertColumnsToken;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertColumnsPlaceholder;

import java.util.List;

/**
 * Base SQL rewriter.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class BaseSQLRewriter implements SQLRewriter {
    
    private final SQLStatement sqlStatement;
    
    private final List<SQLToken> sqlTokens;
    
    private int currentSQLTokenIndex;
    
    /**
     * Append whole SQL.
     *
     * @param sqlBuilder SQL builder
     */
    public void appendWholeSQL(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(sqlStatement.getLogicSQL());
    }
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final ParameterBuilder parameterBuilder, final SQLToken sqlToken) {
        if (sqlToken instanceof InsertColumnsToken) {
            appendInsertColumnsPlaceholder(sqlBuilder, (InsertColumnsToken) sqlToken);
        }
        appendLiteral(sqlBuilder, sqlToken);
    }
    
    private void appendInsertColumnsPlaceholder(final SQLBuilder sqlBuilder, final InsertColumnsToken insertColumnsToken) {
        InsertColumnsPlaceholder columnsPlaceholder = new InsertColumnsPlaceholder(insertColumnsToken.getColumns(), insertColumnsToken.isToAppendParenthesis());
        sqlBuilder.appendPlaceholder(columnsPlaceholder);
    }
    
    private void appendLiteral(final SQLBuilder sqlBuilder, final SQLToken sqlToken) {
        String logicSQL = sqlStatement.getLogicSQL();
        int stopIndex = sqlTokens.size() - 1 == currentSQLTokenIndex ? logicSQL.length() : sqlTokens.get(currentSQLTokenIndex + 1).getStartIndex();
        sqlBuilder.appendLiterals(logicSQL.substring(getStartIndex(sqlToken) > logicSQL.length() ? logicSQL.length() : getStartIndex(sqlToken), stopIndex));
        currentSQLTokenIndex++;
    }
    
    private int getStartIndex(final SQLToken sqlToken) {
        return sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
    }
    
    /**
     * Append initial literal.
     *
     * @param sqlBuilder SQL builder
     */
    public void appendInitialLiteral(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(sqlStatement.getLogicSQL().substring(0, sqlTokens.get(0).getStartIndex()));
    }
}
