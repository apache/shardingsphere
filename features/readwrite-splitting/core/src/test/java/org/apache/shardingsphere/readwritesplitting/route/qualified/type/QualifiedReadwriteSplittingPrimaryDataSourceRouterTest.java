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

package org.apache.shardingsphere.readwritesplitting.route.qualified.type;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualifiedReadwriteSplittingPrimaryDataSourceRouterTest {
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext = new HintValueContext();
    
    @Test
    void assertIsQualifiedWithSelect() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getLock()).thenReturn(Optional.of(new LockSegment(0, 1)));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
    
    @Test
    void assertIsQualifiedWithSelectAndContainsLastInsertIdProjection() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().isContainsLastInsertIdProjection()).thenReturn(true);
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
    
    @Test
    void assertIsQualifiedWithUpdate() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
    
    @Test
    void assertIsQualifiedWithHintManager() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setWriteRouteOnly();
            assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
        }
    }
    
    @Test
    void assertIsQualifiedWithHintValue() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        hintValueContext.setWriteRouteOnly(true);
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
    
    @Test
    void assertIsNotQualifiedWithHint() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        assertFalse(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
    
    @Test
    void assertRoute() {
        ReadwriteSplittingDataSourceGroupRule rule = mock(ReadwriteSplittingDataSourceGroupRule.class);
        when(rule.getWriteDataSource()).thenReturn("write_ds");
        assertThat(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().route(rule), is("write_ds"));
    }
}
