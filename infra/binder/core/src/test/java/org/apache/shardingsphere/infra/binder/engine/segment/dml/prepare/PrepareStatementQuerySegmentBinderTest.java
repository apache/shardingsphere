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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.prepare;

import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class PrepareStatementQuerySegmentBinderTest {
    
    @Test
    void assertBindWithMerge() {
        MergeStatement mergeStatement = mock(MergeStatement.class);
        PrepareStatementQuerySegment segment = new PrepareStatementQuerySegment(6, 118);
        segment.setMerge(mergeStatement);
        PrepareStatementQuerySegment actual = PrepareStatementQuerySegmentBinder.bind(segment, mock(SQLStatementBinderContext.class));
        assertThat(actual.getStartIndex(), is(6));
        assertThat(actual.getStopIndex(), is(118));
        assertThat(actual.getMerge().orElse(null), sameInstance(mergeStatement));
        assertFalse(actual.getSelect().isPresent());
        assertFalse(actual.getInsert().isPresent());
        assertFalse(actual.getUpdate().isPresent());
        assertFalse(actual.getDelete().isPresent());
    }
    
    @Test
    void assertBindWithSelect() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        PrepareStatementQuerySegment segment = new PrepareStatementQuerySegment(6, 13);
        segment.setSelect(selectStatement);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        SelectStatement boundSelect = mock(SelectStatement.class);
        try (
                MockedConstruction<SelectStatementBinder> ignored = mockConstruction(SelectStatementBinder.class,
                        (mock, context) -> when(mock.bind(selectStatement, binderContext)).thenReturn(boundSelect))) {
            PrepareStatementQuerySegment actual = PrepareStatementQuerySegmentBinder.bind(segment, binderContext);
            assertThat(actual.getStartIndex(), is(6));
            assertThat(actual.getStopIndex(), is(13));
            assertThat(actual.getSelect().orElse(null), sameInstance(boundSelect));
            assertFalse(actual.getMerge().isPresent());
        }
    }
}
