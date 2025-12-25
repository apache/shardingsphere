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
package org.apache.shardingsphere.sql.parser.engine.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.database.parser.SQLParserExecutor;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLParserEngineTest {
    
    @Test
    void assertParse() {
        SQLParserExecutor sqlParserExecutor = mock(SQLParserExecutor.class);
        when(sqlParserExecutor.parse("")).thenReturn(mock(ParseASTNode.class));
        LoadingCache<String, ParseASTNode> parseTreeCache = Caffeine.newBuilder().softValues().initialCapacity(128).maximumSize(1024L).build(sqlParserExecutor::parse);
        parseTreeCache.get("");
        verify(sqlParserExecutor).parse("");
        parseTreeCache.get("");
        verify(sqlParserExecutor).parse("");
        sqlParserExecutor.parse("");
        verify(sqlParserExecutor, times(2)).parse("");
    }
}
