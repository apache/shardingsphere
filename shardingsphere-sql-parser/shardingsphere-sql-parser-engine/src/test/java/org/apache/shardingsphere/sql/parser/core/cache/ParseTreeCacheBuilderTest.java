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

package org.apache.shardingsphere.sql.parser.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ParseTreeCacheBuilder Testcase.
 */
public class ParseTreeCacheBuilderTest {

    private CacheOption option;

    private String databaseType;

    /**
     * define cacheOption & databaseType.
     */
    @Before
    public void setUp() {
        option = new CacheOption(128, 1024L, 4);
        databaseType = "MySQL";
    }

    @Test
    public void build() {
        LoadingCache<String, ParseTree> loadingCache = CacheBuilder.newBuilder().softValues()
                .initialCapacity(option.getInitialCapacity()).maximumSize(option.getMaximumSize()).concurrencyLevel(option.getConcurrencyLevel()).build(new ParseTreeCacheLoader(databaseType));

        Assert.assertEquals(loadingCache.asMap(), ParseTreeCacheBuilder.build(option, databaseType).asMap());
    }
}
