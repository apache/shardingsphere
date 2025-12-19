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

package org.apache.shardingsphere.sql.parser.engine.core.database.cache;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.database.parser.SQLParserExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class ParseTreeCacheLoaderTest {
    
    private static final String SQL = "SELECT * FROM user WHERE id=1";
    
    @Test
    void assertParseTreeCacheLoader() throws ReflectiveOperationException {
        SQLParserExecutor sqlParserExecutor = mock(SQLParserExecutor.class, RETURNS_DEEP_STUBS);
        ParseTreeCacheLoader loader = new ParseTreeCacheLoader(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        Plugins.getMemberAccessor().set(ParseTreeCacheLoader.class.getDeclaredField("sqlParserExecutor"), loader, sqlParserExecutor);
        assertThat(loader.load(SQL), isA(ParseASTNode.class));
    }
}
