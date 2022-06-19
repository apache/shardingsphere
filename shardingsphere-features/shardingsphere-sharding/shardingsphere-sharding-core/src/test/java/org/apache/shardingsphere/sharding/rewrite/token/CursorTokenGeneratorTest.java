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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.MoveStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.CursorTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.CursorToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CursorTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        CursorTokenGenerator generator = new CursorTokenGenerator();
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class)));
        assertTrue(generator.isGenerateSQLToken(mock(CursorStatementContext.class)));
        assertTrue(generator.isGenerateSQLToken(mock(CloseStatementContext.class)));
        assertTrue(generator.isGenerateSQLToken(mock(MoveStatementContext.class)));
        assertTrue(generator.isGenerateSQLToken(mock(FetchStatementContext.class)));
    }
    
    @Test
    public void assertGenerateSQLToken() {
        CursorTokenGenerator generator = new CursorTokenGenerator();
        CursorStatementContext statementContext = mock(CursorStatementContext.class);
        when(statementContext.getCursorName()).thenReturn(new CursorNameSegment(0, 0, new IdentifierValue("t_order_cursor")));
        SQLToken actual = generator.generateSQLToken(statementContext);
        assertTrue(actual instanceof CursorToken);
    }
}
