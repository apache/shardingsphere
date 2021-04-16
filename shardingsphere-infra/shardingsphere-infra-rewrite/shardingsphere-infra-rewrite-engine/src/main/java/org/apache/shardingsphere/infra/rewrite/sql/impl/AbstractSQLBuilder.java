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

package org.apache.shardingsphere.infra.rewrite.sql.impl;

import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.SQLBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.ComposableSQLToken;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract SQL builder.
 */
public abstract class AbstractSQLBuilder implements SQLBuilder {
    
    private final SQLRewriteContext context;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    public AbstractSQLBuilder(final SQLRewriteContext context) {
        this.context = context;
        context.getSqlTokens().forEach(each -> {
            if (each instanceof ComposableSQLToken) {
                sqlTokens.addAll(((ComposableSQLToken) each).getSqlTokens());
            } else {
                sqlTokens.add(each);
            }
        });
    }
    
    @Override
    public final String toSQL() {
        if (sqlTokens.isEmpty()) {
            return context.getSql();
        }
        Collections.sort(sqlTokens);
        StringBuilder result = new StringBuilder();
        result.append(context.getSql(), 0, sqlTokens.get(0).getStartIndex());
        for (SQLToken each : sqlTokens) {
            result.append(getSQLTokenText(each));
            result.append(getConjunctionText(each));
        }
        return result.toString();
    }
    
    protected abstract String getSQLTokenText(SQLToken sqlToken);

    private String getConjunctionText(final SQLToken sqlToken) {
        return context.getSql().substring(getStartIndex(sqlToken), getStopIndex(sqlToken));
    }
    
    private int getStartIndex(final SQLToken sqlToken) {
        int startIndex = sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
        return Math.min(startIndex, context.getSql().length());
    }
    
    private int getStopIndex(final SQLToken sqlToken) {
        int currentSQLTokenIndex = sqlTokens.indexOf(sqlToken);
        return sqlTokens.size() - 1 == currentSQLTokenIndex ? context.getSql().length() : sqlTokens.get(currentSQLTokenIndex + 1).getStartIndex();
    }
}
