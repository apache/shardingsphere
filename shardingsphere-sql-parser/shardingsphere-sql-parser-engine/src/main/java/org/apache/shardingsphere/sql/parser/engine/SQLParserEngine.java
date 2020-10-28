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

package org.apache.shardingsphere.sql.parser.engine;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.cache.SQLParsedResultCache;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserExecutor;

import java.util.Optional;

/**
 * SQL parser engine.
 */
@RequiredArgsConstructor
public final class SQLParserEngine {
    
    private final String databaseType;
    
    private final SQLParsedResultCache<ParseTree> cache = new SQLParsedResultCache<>();
    
    /**
     * Parse SQL.
     * 
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @return parse tree
     */
    public ParseTree parse(final String sql, final boolean useCache) {
        if (!useCache) {
            return parse(sql);
        }
        Optional<ParseTree> parseTree = cache.get(sql);
        if (parseTree.isPresent()) {
            return parseTree.get();
        }
        ParseTree result = new SQLParserExecutor(databaseType, sql).execute().getRootNode();
        cache.put(sql, result);
        return result;
    }
    
    private ParseTree parse(final String sql) {
        return new SQLParserExecutor(databaseType, sql).execute().getRootNode();
    }
}
