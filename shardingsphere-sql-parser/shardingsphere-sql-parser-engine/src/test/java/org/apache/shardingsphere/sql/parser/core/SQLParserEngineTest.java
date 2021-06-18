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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.core.database.parser.SQLParserExecutor;
import org.junit.Test;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class SQLParserEngineTest {

    @Test
    public void assertParse() {
        ParseTree parseTree = mock(ParseTree.class);

        SQLParserExecutor sqlParserExecutor = mock(SQLParserExecutor.class);
        when(sqlParserExecutor.parse("")).thenReturn(parseTree);

        LoadingCache<String, ParseTree> parseTreeCache = CacheBuilder.newBuilder().softValues()
                .initialCapacity(128).maximumSize(1024).concurrencyLevel(4).build(new CacheLoader<String, ParseTree>() {
                        @ParametersAreNonnullByDefault
                        @Override
                        public ParseTree load(final String sql) {
                            return sqlParserExecutor.parse(sql);
                        }
                    }
                );

        parseTreeCache.getUnchecked("");
        verify(sqlParserExecutor, times(1)).parse("");
        parseTreeCache.getUnchecked("");
        verify(sqlParserExecutor, times(1)).parse("");
        sqlParserExecutor.parse("");
        verify(sqlParserExecutor, times(2)).parse("");
    }
}
