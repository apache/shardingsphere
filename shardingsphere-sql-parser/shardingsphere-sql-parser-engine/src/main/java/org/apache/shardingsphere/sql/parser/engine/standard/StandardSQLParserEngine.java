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

package org.apache.shardingsphere.sql.parser.engine.standard;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorType;
import org.apache.shardingsphere.sql.parser.cache.SQLParsedResultCaches;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserExecutor;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLStatementVisitorFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorRule;
import org.apache.shardingsphere.sql.parser.cache.SQLParsedResultCache;
import org.apache.shardingsphere.sql.parser.engine.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.hook.ParsingHookRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * Standard SQL parser engine.
 */
@RequiredArgsConstructor
public final class StandardSQLParserEngine implements SQLParserEngine {
    
    private final String databaseTypeName;
    
    private final SQLParsedResultCaches caches = new SQLParsedResultCaches();
    
    private final ParsingHookRegistry parsingHookRegistry = ParsingHookRegistry.getInstance();
    
    /**
     * Parse to AST.
     * @param sql SQL
     * @param useCache use cache or not
     * @return parse tree
     */
    public ParseTree parseToAST(final String sql, final boolean useCache) {
        Optional<ParseTree> parseTree = getCache(sql, useCache, SQLVisitorType.FORMAT);
        if (parseTree.isPresent()) {
            return parseTree.get();
        }
        ParseTree result = parse0(sql);
        putCache(sql, result, useCache, SQLVisitorType.FORMAT);
        return result;
    }
    
    // TODO check skywalking plugin
    /**
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
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
    
    /**
     * Parse.
     *
     * @param sql SQL
     * @param visitor visitor
     * @return object
     */
    public Object parse(final String sql, final ParseTreeVisitor visitor) {
        return parse0(sql).accept(visitor);
    }
    
    private SQLStatement parseToSQLStatement0(final String sql, final boolean useCache) {
        Optional<SQLStatement> statement = getCache(sql, useCache, SQLVisitorType.STATEMENT);
        if (statement.isPresent()) {
            return statement.get();
        }
        ParseTree parseTree = parse0(sql);
        ParseTreeVisitor visitor = SQLStatementVisitorFactory.newInstance(databaseTypeName, SQLVisitorRule.valueOf(parseTree.getClass()));
        SQLStatement result = (SQLStatement) parseTree.accept(visitor);
        putCache(sql, result, useCache, SQLVisitorType.STATEMENT);
        return result;
    }
    
    private Optional getCache(final String sql, final boolean useCache, final SQLVisitorType type) {
        if (useCache) {
            SQLParsedResultCache cache = caches.getCache(type);
            return cache.get(sql);
        }
        return Optional.empty();
    }
    
    private void putCache(final String sql, final Object parsedResult, final boolean useCache, final SQLVisitorType type) {
        if (useCache) {
            SQLParsedResultCache cache = caches.getCache(type);
            cache.put(sql, parsedResult);
        }
    }
    
    private ParseTree parse0(final String sql) {
        return new SQLParserExecutor(databaseTypeName, sql).execute().getRootNode();
    }
}
