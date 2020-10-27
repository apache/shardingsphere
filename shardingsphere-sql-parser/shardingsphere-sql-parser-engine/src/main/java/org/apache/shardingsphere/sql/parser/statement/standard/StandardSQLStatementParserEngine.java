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

package org.apache.shardingsphere.sql.parser.statement.standard;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorType;
import org.apache.shardingsphere.sql.parser.cache.SQLParsedResultCache;
import org.apache.shardingsphere.sql.parser.cache.SQLParsedResultCaches;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLStatementVisitorFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorRule;
import org.apache.shardingsphere.sql.parser.statement.SQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.engine.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.hook.ParsingHookRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * Standard SQL statement parser engine.
 */
@RequiredArgsConstructor
public final class StandardSQLStatementParserEngine implements SQLStatementParserEngine {
    
    private final String databaseTypeName;
    
    private final SQLParsedResultCaches caches = new SQLParsedResultCaches();
    
    private final ParsingHookRegistry parsingHookRegistry = ParsingHookRegistry.getInstance();
    
    /*
     * To make sure SkyWalking will be available at the next release of ShardingSphere, a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    @SuppressWarnings("OverlyBroadCatchBlock")
    @Override
    public SQLStatement parseToSQLStatement(final String sql, final boolean useCache) {
        parsingHookRegistry.start(sql);
        try {
            SQLStatement result = parseToSQLStatement0(sql, useCache);
            parsingHookRegistry.finishSuccess(result);
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            parsingHookRegistry.finishFailure(ex);
            throw ex;
        }
    }
    
    private SQLStatement parseToSQLStatement0(final String sql, final boolean useCache) {
        Optional<SQLStatement> statement = getCache(sql, useCache);
        if (statement.isPresent()) {
            return statement.get();
        }
        ParseTree parseTree = SQLParserEngineFactory.getSQLParserEngine(databaseTypeName).parse(sql, useCache);
        ParseTreeVisitor visitor = SQLStatementVisitorFactory.newInstance(databaseTypeName, SQLVisitorRule.valueOf(parseTree.getClass()));
        SQLStatement result = (SQLStatement) parseTree.accept(visitor);
        putCache(sql, result, useCache);
        return result;
    }
    
    private Optional getCache(final String sql, final boolean useCache) {
        if (useCache) {
            SQLParsedResultCache cache = caches.getCache(SQLVisitorType.STATEMENT);
            return cache.get(sql);
        }
        return Optional.empty();
    }
    
    private void putCache(final String sql, final Object parsedResult, final boolean useCache) {
        if (useCache) {
            SQLParsedResultCache cache = caches.getCache(SQLVisitorType.STATEMENT);
            cache.put(sql, parsedResult);
        }
    }
}
