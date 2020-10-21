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

package org.apache.shardingsphere.sql.parser.engine.ast;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.engine.SQLParsedResultCache;

import java.util.Optional;

/**
 * AST SQL parsed result cache.
 */
public final class ASTSQLParseResultCache implements SQLParsedResultCache<ParseTree> {
    
    private final Cache<String, ParseTree> cache = CacheBuilder.newBuilder().softValues().initialCapacity(2000).maximumSize(65535).build();
    
    @Override
    public void put(final String sql, final ParseTree parseTree) {
        cache.put(sql, parseTree);
    }
    
    @Override
    public Optional<ParseTree> get(final String sql) {
        return Optional.ofNullable(cache.getIfPresent(sql));
    }
    
    @Override
    public synchronized void clear() {
        cache.invalidateAll();
    }
}
