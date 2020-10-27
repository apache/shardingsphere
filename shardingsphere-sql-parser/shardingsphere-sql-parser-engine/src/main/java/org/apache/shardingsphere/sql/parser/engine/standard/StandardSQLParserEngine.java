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

import java.util.Optional;

/**
 * Standard SQL parser engine.
 */
@RequiredArgsConstructor
public final class StandardSQLParserEngine {
    
    private final String databaseTypeName;
    
    private final SQLParsedResultCaches caches = new SQLParsedResultCaches();
    
    /**
     * Parse SQL.
     * 
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @return parse tree
     */
    public ParseTree parse(final String sql, final boolean useCache) {
        Optional<ParseTree> parseTree = getCache(sql, useCache, SQLVisitorType.FORMAT);
        if (parseTree.isPresent()) {
            return parseTree.get();
        }
        ParseTree result = parse0(sql);
        putCache(sql, result, useCache, SQLVisitorType.FORMAT);
        return result;
    }
    
    /**
     * Parse.
     *
     * @param sql SQL to be parsed
     * @param visitor visitor
     * @return object
     */
    public Object parse(final String sql, final ParseTreeVisitor visitor) {
        return parse0(sql).accept(visitor);
    }

    private ParseTree parse0(final String sql) {
        return new SQLParserExecutor(databaseTypeName, sql).execute().getRootNode();
    }
    
    private Optional getCache(final String sql, final boolean useCache, final SQLVisitorType type) {
        if (useCache) {
            return caches.getCache(type).get(sql);
        }
        return Optional.empty();
    }
    
    private void putCache(final String sql, final Object parsedResult, final boolean useCache, final SQLVisitorType type) {
        if (useCache) {
            caches.getCache(type).put(sql, parsedResult);
        }
    }
}
