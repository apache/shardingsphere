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

package org.apache.shardingsphere.infra.parser.cache;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserExecutor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class SQLStatementCacheLoaderTest {
    
    private static final String SQL = "SELECT * FROM user WHERE id=1";
    
    @Test
    void assertSQLStatementCacheLoad() throws ReflectiveOperationException {
        SQLStatementCacheLoader sqlStatementCacheLoader = new SQLStatementCacheLoader(TypedSPILoader.getService(DatabaseType.class, "SQL92"), new CacheOption(128, 1024L));
        SQLStatementParserExecutor executor = mock(SQLStatementParserExecutor.class, RETURNS_DEEP_STUBS);
        Plugins.getMemberAccessor().set(SQLStatementCacheLoader.class.getDeclaredField("sqlStatementParserExecutor"), sqlStatementCacheLoader, executor);
        assertThat(sqlStatementCacheLoader.load(SQL), isA(SQLStatement.class));
    }
}
