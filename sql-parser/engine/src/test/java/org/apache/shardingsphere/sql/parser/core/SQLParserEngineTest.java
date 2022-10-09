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

package org.apache.shardingsphere.sql.parser.core;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.shardingsphere.sql.parser.core.database.parser.SQLParserExecutor;
import org.junit.Test;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public final class SQLParserEngineTest {
    
    @Test
    public void assertParse() {
        SQLParserExecutor sqlParserExecutor = mock(SQLParserExecutor.class);
        when(sqlParserExecutor.parse("")).thenReturn(mock(ParseASTNode.class));
        LoadingCache<String, ParseASTNode> parseTreeCache = Caffeine.newBuilder().softValues()
                .initialCapacity(128).maximumSize(1024).build(new CacheLoader<String, ParseASTNode>() {
                    
                    @ParametersAreNonnullByDefault
                    @Override
                    public ParseASTNode load(final String sql) {
                        return sqlParserExecutor.parse(sql);
                    }
                });
        parseTreeCache.get("");
        verify(sqlParserExecutor, times(1)).parse("");
        parseTreeCache.get("");
        verify(sqlParserExecutor, times(1)).parse("");
        sqlParserExecutor.parse("");
        verify(sqlParserExecutor, times(2)).parse("");
    }
}
