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

package org.apache.shardingsphere.sql.parser.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

/**
 * Parse tree cache builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParseTreeCacheBuilder {
    
    /**
     * Build parse tree cache.
     * 
     * @param option cache option
     * @param databaseType database type
     * @return built parse tree cache
     */
    public static LoadingCache<String, ParseTree> build(final CacheOption option, final String databaseType) {
        return CacheBuilder.newBuilder().softValues()
                .initialCapacity(option.getInitialCapacity()).maximumSize(option.getMaximumSize()).concurrencyLevel(option.getConcurrencyLevel()).build(new ParseTreeCacheLoader(databaseType));
    }
}
