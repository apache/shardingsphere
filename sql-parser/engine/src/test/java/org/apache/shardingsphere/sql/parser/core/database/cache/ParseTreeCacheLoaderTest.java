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

package org.apache.shardingsphere.sql.parser.core.database.cache;

import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.database.parser.SQLParserExecutor;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ParseTreeCacheLoaderTest {
    
    private static final String SQL = "select * from user where id=1";
    
    @Test
    public void assertParseTreeCacheLoader() throws NoSuchFieldException, IllegalAccessException {
        SQLParserExecutor sqlParserExecutor = mock(SQLParserExecutor.class, RETURNS_DEEP_STUBS);
        ParseTreeCacheLoader loader = new ParseTreeCacheLoader("MySQL");
        Field sqlParserExecutorField = loader.getClass().getDeclaredField("sqlParserExecutor");
        sqlParserExecutorField.setAccessible(true);
        sqlParserExecutorField.set(loader, sqlParserExecutor);
        assertThat(loader.load(SQL), isA(ParseASTNode.class));
    }
}
