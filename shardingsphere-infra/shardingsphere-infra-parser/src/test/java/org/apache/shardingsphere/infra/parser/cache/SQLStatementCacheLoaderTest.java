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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLStatementCacheLoaderTest {
    private static final String SQL = "select * from user where id=1";

    @SneakyThrows
    @Test
    public void assertSQLStatementCacheLoad() {
        SQLStatementCacheLoader sqlStatementCacheLoader = new SQLStatementCacheLoader("MySQL");
        Field sqlStatementParserExecutorField = sqlStatementCacheLoader.getClass().getDeclaredField("sqlStatementParserExecutor");
        SQLStatementParserExecutor executor = mock(SQLStatementParserExecutor.class, Mockito.RETURNS_DEEP_STUBS);
        sqlStatementParserExecutorField.setAccessible(true);
        sqlStatementParserExecutorField.set(sqlStatementCacheLoader, executor);
        assertThat(sqlStatementCacheLoader.load(SQL), isA(SQLStatement.class));
        sqlStatementParserExecutorField.setAccessible(false);
    }
}
