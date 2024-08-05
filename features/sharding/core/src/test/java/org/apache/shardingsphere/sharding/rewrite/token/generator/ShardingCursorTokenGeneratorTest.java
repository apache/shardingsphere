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

package org.apache.shardingsphere.sharding.rewrite.token.generator;

import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.MoveStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingCursorTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.CursorToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingCursorTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLToken() {
        ShardingCursorTokenGenerator generator = new ShardingCursorTokenGenerator(mock(ShardingRule.class));
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class)));
        Optional<CursorNameSegment> cursorName = Optional.of(new CursorNameSegment(0, 0, new IdentifierValue("t_order_cursor")));
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class);
        when(cursorStatementContext.getCursorName()).thenReturn(cursorName);
        assertTrue(generator.isGenerateSQLToken(cursorStatementContext));
        CloseStatementContext closeStatementContext = mock(CloseStatementContext.class);
        when(closeStatementContext.getCursorName()).thenReturn(cursorName);
        assertTrue(generator.isGenerateSQLToken(closeStatementContext));
        MoveStatementContext moveStatementContext = mock(MoveStatementContext.class);
        when(moveStatementContext.getCursorName()).thenReturn(cursorName);
        assertTrue(generator.isGenerateSQLToken(moveStatementContext));
        FetchStatementContext fetchStatementContext = mock(FetchStatementContext.class);
        when(fetchStatementContext.getCursorName()).thenReturn(cursorName);
        assertTrue(generator.isGenerateSQLToken(fetchStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        ShardingCursorTokenGenerator generator = new ShardingCursorTokenGenerator(mock(ShardingRule.class));
        CursorStatementContext statementContext = mock(CursorStatementContext.class);
        when(statementContext.getCursorName()).thenReturn(Optional.of(new CursorNameSegment(0, 0, new IdentifierValue("t_order_cursor"))));
        SQLToken actual = generator.generateSQLToken(statementContext);
        assertThat(actual, instanceOf(CursorToken.class));
    }
}
