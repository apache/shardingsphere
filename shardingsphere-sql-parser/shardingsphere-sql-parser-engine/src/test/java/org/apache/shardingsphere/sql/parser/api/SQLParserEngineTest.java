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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.database.parser.SQLParserExecutor;
import org.junit.Test;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SQLParserEngineTest {
    
    private static final String SQL = "SELECT COUNT(*) FROM user";
    
    @Test
    public void assertParse() throws NoSuchFieldException, IllegalAccessException {
        SQLParserExecutor sqlParserExecutor = mock(SQLParserExecutor.class);
        when(sqlParserExecutor.parse(SQL)).thenReturn(mock(ParseASTNode.class));
        CacheOption cacheOption = new CacheOption(128, 1024L);
        SQLParserEngine sqlParserEngine = new SQLParserEngine("H2", cacheOption);
        Field sqlParserExecutorFiled = sqlParserEngine.getClass().getDeclaredField("sqlParserExecutor");
        Field parseTreeCacheField = sqlParserEngine.getClass().getDeclaredField("parseTreeCache");
        sqlParserExecutorFiled.setAccessible(true);
        parseTreeCacheField.setAccessible(true);
        sqlParserExecutorFiled.set(sqlParserEngine, sqlParserExecutor);
        LoadingCache<String, ParseASTNode> parseTreeCache = Caffeine.newBuilder().softValues().initialCapacity(128)
                .maximumSize(1024).build(new CacheLoader<String, ParseASTNode>() {
                    
                    @ParametersAreNonnullByDefault
                    @Override
                    public ParseASTNode load(final String sql) {
                        return sqlParserExecutor.parse(sql);
                    }
                });
        parseTreeCacheField.set(sqlParserEngine, parseTreeCache);
        sqlParserEngine.parse(SQL, true);
        verify(sqlParserExecutor, times(1)).parse(SQL);
        sqlParserEngine.parse(SQL, true);
        verify(sqlParserExecutor, times(1)).parse(SQL);
        sqlParserEngine.parse(SQL, false);
        verify(sqlParserExecutor, times(2)).parse(SQL);
    }
}
