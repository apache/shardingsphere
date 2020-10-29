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

package org.apache.shardingsphere.sql.parser.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserExecutor;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL parser engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserEngine {
    
    private static final Map<String, SQLParserExecutor> ENGINES = new ConcurrentHashMap<>();
    
    /**
     * Parse SQL.
     *
     * @param databaseType database type
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @param visitorType SQL visitor type
     * @param <T> type of SQL visitor result
     * @return SQL visitor result
     */
    public static <T> T parse(final String databaseType, final String sql, final boolean useCache, final String visitorType) {
        ParseTree parseTree = parse(databaseType, sql, useCache);
        ParseTreeVisitor<T> visitor = SQLVisitorFactory.newInstance(databaseType, visitorType, SQLVisitorRule.valueOf(parseTree.getClass()));
        return parseTree.accept(visitor);
    }
    
    /**
     * Parse SQL.
     *
     * @param databaseType database type
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @return parse tree
     */
    public static ParseTree parse(final String databaseType, final String sql, final boolean useCache) {
        return getSQLParserExecutor(databaseType).parse(sql, useCache);
    }
    
    private static SQLParserExecutor getSQLParserExecutor(final String databaseType) {
        return ENGINES.containsKey(databaseType) ? ENGINES.get(databaseType) : ENGINES.computeIfAbsent(databaseType, SQLParserExecutor::new);
    }
}
