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

import org.apache.shardingsphere.infra.binder.context.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingConstraintTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ConstraintToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingConstraintTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLToken() {
        ShardingConstraintTokenGenerator generator = new ShardingConstraintTokenGenerator(mock(ShardingRule.class));
        assertFalse(generator.isGenerateSQLToken(mock(UnknownSQLStatementContext.class)));
        AlterTableStatementContext alterTableStatementContext = mock(AlterTableStatementContext.class);
        Collection<ConstraintSegment> constraintSegments = new LinkedList<>();
        when(alterTableStatementContext.getConstraints()).thenReturn(constraintSegments);
        assertFalse(generator.isGenerateSQLToken(alterTableStatementContext));
        constraintSegments.add(mock(ConstraintSegment.class));
        assertTrue(generator.isGenerateSQLToken(alterTableStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokens() {
        ConstraintSegment constraintSegment = mock(ConstraintSegment.class);
        when(constraintSegment.getStartIndex()).thenReturn(1);
        when(constraintSegment.getStopIndex()).thenReturn(3);
        IdentifierValue constraintIdentifier = mock(IdentifierValue.class);
        when(constraintSegment.getIdentifier()).thenReturn(constraintIdentifier);
        AlterTableStatementContext alterTableStatementContext = mock(AlterTableStatementContext.class);
        when(alterTableStatementContext.getConstraints()).thenReturn(Collections.singleton(constraintSegment));
        ShardingConstraintTokenGenerator generator = new ShardingConstraintTokenGenerator(mock(ShardingRule.class));
        Collection<SQLToken> actual = generator.generateSQLTokens(alterTableStatementContext);
        assertThat(actual.size(), is(1));
        assertConstraintToken((ConstraintToken) actual.iterator().next());
    }
    
    private static void assertConstraintToken(final ConstraintToken actual) {
        assertThat(actual.getStartIndex(), is(1));
        assertThat(actual.getStopIndex(), is(3));
    }
}
