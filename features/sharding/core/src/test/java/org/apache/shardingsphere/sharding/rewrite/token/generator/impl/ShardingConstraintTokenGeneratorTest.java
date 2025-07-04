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
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ConstraintToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ConstraintSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingConstraintTokenGeneratorTest {
    
    private final ShardingConstraintTokenGenerator generator = new ShardingConstraintTokenGenerator(mock(ShardingRule.class));
    
    @Test
    void assertIsNotGenerateSQLTokenWithoutConstraintSQLStatementAttribute() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes());
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyConstraint() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        ConstraintSQLStatementAttribute constraintAttribute = mock(ConstraintSQLStatementAttribute.class);
        when(constraintAttribute.getConstraints()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes(constraintAttribute));
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        assertTrue(generator.isGenerateSQLToken(mockSQLStatementContext()));
    }
    
    @Test
    void assertGenerateSQLTokens() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mockSQLStatementContext());
        assertThat(actual.size(), is(1));
        assertConstraintToken((ConstraintToken) actual.iterator().next());
    }
    
    private SQLStatementContext mockSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        ConstraintSQLStatementAttribute constraintAttribute = mock(ConstraintSQLStatementAttribute.class);
        when(constraintAttribute.getConstraints()).thenReturn(Collections.singleton(new ConstraintSegment(1, 3, mock(IdentifierValue.class))));
        when(result.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes(constraintAttribute));
        return result;
    }
    
    private void assertConstraintToken(final ConstraintToken actual) {
        assertThat(actual.getStartIndex(), is(1));
        assertThat(actual.getStopIndex(), is(3));
    }
}
