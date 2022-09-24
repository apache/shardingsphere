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

import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.DistinctProjectionPrefixTokenGenerator;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DistinctProjectionPrefixTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        DistinctProjectionPrefixTokenGenerator generator = new DistinctProjectionPrefixTokenGenerator();
        assertFalse(generator.isGenerateSQLToken(mock(CreateDatabaseStatementContext.class)));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        List<AggregationDistinctProjection> aggregationDistinctProjections = new LinkedList<>();
        when(selectStatementContext.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(aggregationDistinctProjections);
        assertFalse(generator.isGenerateSQLToken(selectStatementContext));
        aggregationDistinctProjections.add(mock(AggregationDistinctProjection.class));
        assertTrue(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    public void assertGenerateSQLToken() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        final int testStartIndex = 1;
        when(selectStatementContext.getProjectionsContext().getStartIndex()).thenReturn(testStartIndex);
        DistinctProjectionPrefixTokenGenerator generator = new DistinctProjectionPrefixTokenGenerator();
        assertThat(generator.generateSQLToken(selectStatementContext).toString(), is("DISTINCT "));
    }
}
