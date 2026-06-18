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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.CursorToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingCursorTokenGeneratorTest {
    
    private final ShardingCursorTokenGenerator generator = new ShardingCursorTokenGenerator(mock(ShardingRule.class));
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotCursorContextAvailable() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutCursorName() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(mock(CursorSQLStatementAttribute.class)));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithCursorName() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        CursorSQLStatementAttribute cursorSQLStatementAttribute = mock(CursorSQLStatementAttribute.class);
        when(cursorSQLStatementAttribute.getCursorName()).thenReturn(Optional.of(mock(CursorNameSegment.class)));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(cursorSQLStatementAttribute));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        CursorSQLStatementAttribute cursorSQLStatementAttribute = mock(CursorSQLStatementAttribute.class);
        when(cursorSQLStatementAttribute.getCursorName()).thenReturn(Optional.of(new CursorNameSegment(0, 0, new IdentifierValue("foo_cursor"))));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(cursorSQLStatementAttribute));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        SQLToken actual = generator.generateSQLToken(sqlStatementContext);
        assertThat(actual, isA(CursorToken.class));
    }
}
