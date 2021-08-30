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

import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ConstraintTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ConstraintToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ConstraintTokenGeneratorTest {

    private static final int TEST_START_INDEX = 1;

    private static final int TEST_STOP_INDEX = 3;

    @Test
    public void assertIsGenerateSQLToken() {
        CreateDatabaseStatementContext createDatabaseStatementContext = mock(CreateDatabaseStatementContext.class);
        ConstraintTokenGenerator constraintTokenGenerator = new ConstraintTokenGenerator();
        assertFalse(constraintTokenGenerator.isGenerateSQLToken(createDatabaseStatementContext));
        AlterTableStatementContext alterTableStatementContext = mock(AlterTableStatementContext.class);
        Collection<ConstraintSegment> constraintSegmentCollection = new LinkedList<>();
        when(alterTableStatementContext.getConstraints()).thenReturn(constraintSegmentCollection);
        assertFalse(constraintTokenGenerator.isGenerateSQLToken(alterTableStatementContext));
        constraintSegmentCollection.add(mock(ConstraintSegment.class));
        assertTrue(constraintTokenGenerator.isGenerateSQLToken(alterTableStatementContext));
    }

    @Test
    public void assertGenerateSQLTokens() {
        ConstraintSegment constraintSegment = mock(ConstraintSegment.class);
        when(constraintSegment.getStartIndex()).thenReturn(TEST_START_INDEX);
        when(constraintSegment.getStopIndex()).thenReturn(TEST_STOP_INDEX);
        IdentifierValue constraintIdentifier = mock(IdentifierValue.class);
        when(constraintSegment.getIdentifier()).thenReturn(constraintIdentifier);
        Collection<ConstraintSegment> constraintSegmentCollection = new LinkedList<>();
        constraintSegmentCollection.add(constraintSegment);
        AlterTableStatementContext alterTableStatementContext = mock(AlterTableStatementContext.class);
        when(alterTableStatementContext.getConstraints()).thenReturn(constraintSegmentCollection);
        ShardingRule shardingRule = mock(ShardingRule.class);
        ConstraintTokenGenerator constraintTokenGenerator = new ConstraintTokenGenerator();
        constraintTokenGenerator.setShardingRule(shardingRule);
        Collection<ConstraintToken> result = constraintTokenGenerator.generateSQLTokens(alterTableStatementContext);
        assertThat(result.size(), is(1));
        assertThat(result.stream().collect(Collectors.toList()).get(0).getStartIndex(), is(TEST_START_INDEX));
    }
}
