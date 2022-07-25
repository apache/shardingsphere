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

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingRemoveTokenGenerator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRemoveTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLTokenWithNonSelectStatement() {
        ShardingRemoveTokenGenerator shardingRemoveTokenGenerator = new ShardingRemoveTokenGenerator();
        assertFalse(shardingRemoveTokenGenerator.isGenerateSQLToken(mock(InsertStatementContext.class)));
    }
    
    @Test
    public void assertIsGenerateSQLTokenWithEmptyAggregationDistinctProjections() {
        ShardingRemoveTokenGenerator shardingRemoveTokenGenerator = new ShardingRemoveTokenGenerator();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections().isEmpty()).thenReturn(Boolean.TRUE);
        assertFalse(shardingRemoveTokenGenerator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    public void assertIsGenerateSQLTokenWithNonEmptyAggregationDistinctProjections() {
        ShardingRemoveTokenGenerator shardingRemoveTokenGenerator = new ShardingRemoveTokenGenerator();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections().isEmpty()).thenReturn(Boolean.FALSE);
        assertTrue(shardingRemoveTokenGenerator.isGenerateSQLToken(selectStatementContext));
    }
}
