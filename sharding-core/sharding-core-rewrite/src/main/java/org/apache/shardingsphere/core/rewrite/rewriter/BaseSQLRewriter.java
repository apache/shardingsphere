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
import org.apache.shardingsphere.core.parse.sql.token.Substitutable;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;

/**
 * Base SQL rewriter.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class BaseSQLRewriter implements SQLRewriter {
    
    private final SQLStatement sqlStatement;
    
    private int tokenCount;
    
    /**
     * Is to rewrite or not.
     * 
     * @return rewrite or not
     */
    public boolean isToRewriteSQLTokens() {
        return !sqlStatement.getSQLTokens().isEmpty();
    }
    
    /**
     * rewrite original literal.
     *
     * @param sqlBuilder sql builder
     */
    public void rewrite(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(sqlStatement.getLogicSQL());
    }
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final SQLToken sqlToken) {
        String originalSQL = sqlStatement.getLogicSQL();
        int stopPosition = sqlStatement.getSQLTokens().size() - 1 == tokenCount ? originalSQL.length() : sqlStatement.getSQLTokens().get(tokenCount + 1).getStartIndex();
        sqlBuilder.appendLiterals(originalSQL.substring(getStartIndex(sqlToken) > originalSQL.length() ? originalSQL.length() : getStartIndex(sqlToken), stopPosition));
        tokenCount++;
    }
    
    private int getStartIndex(final SQLToken sqlToken) {
        return sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
    }
    
    /**
     * rewrite initial literal.
     *
     * @param sqlBuilder sql builder
     */
    public void rewriteInitialLiteral(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(sqlStatement.getLogicSQL().substring(0, sqlStatement.getSQLTokens().get(0).getStartIndex()));
    }
}
