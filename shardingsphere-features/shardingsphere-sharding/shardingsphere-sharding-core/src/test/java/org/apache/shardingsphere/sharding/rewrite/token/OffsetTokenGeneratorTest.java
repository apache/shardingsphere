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

import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.OffsetTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OffsetTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        OffsetTokenGenerator offsetTokenGenerator = new OffsetTokenGenerator();
        assertFalse(offsetTokenGenerator.isGenerateSQLToken(insertStatementContext));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getPaginationContext().getOffsetSegment().isPresent()).thenReturn(Boolean.FALSE);
        assertFalse(offsetTokenGenerator.isGenerateSQLToken(selectStatementContext));
        when(selectStatementContext.getPaginationContext().getOffsetSegment().isPresent()).thenReturn(Boolean.TRUE);
        ParameterMarkerPaginationValueSegment parameterMarkerPaginationValueSegment = mock(ParameterMarkerPaginationValueSegment.class);
        when(selectStatementContext.getPaginationContext().getOffsetSegment().get()).thenReturn(parameterMarkerPaginationValueSegment);
        assertFalse(offsetTokenGenerator.isGenerateSQLToken(selectStatementContext));
        NumberLiteralPaginationValueSegment numberLiteralPaginationValueSegment = mock(NumberLiteralPaginationValueSegment.class);
        when(selectStatementContext.getPaginationContext().getOffsetSegment().get()).thenReturn(numberLiteralPaginationValueSegment);
        assertTrue(offsetTokenGenerator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    public void assertGenerateSQLToken() {
        PaginationValueSegment paginationValueSegment = mock(PaginationValueSegment.class);
        final int testStartIndex = 1;
        when(paginationValueSegment.getStartIndex()).thenReturn(testStartIndex);
        final int testStopIndex = 3;
        when(paginationValueSegment.getStopIndex()).thenReturn(testStopIndex);
        PaginationContext paginationContext = mock(PaginationContext.class);
        when(paginationContext.getOffsetSegment()).thenReturn(Optional.of(paginationValueSegment));
        final long testRevisedOffset = 2;
        when(paginationContext.getRevisedOffset()).thenReturn(testRevisedOffset);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getPaginationContext()).thenReturn(paginationContext);
        OffsetTokenGenerator offsetTokenGenerator = new OffsetTokenGenerator();
        assertThat(offsetTokenGenerator.generateSQLToken(selectStatementContext).toString(), is(String.valueOf(testRevisedOffset)));
    }
}
