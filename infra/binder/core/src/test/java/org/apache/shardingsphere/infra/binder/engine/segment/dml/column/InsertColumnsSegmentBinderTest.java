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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.column;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ColumnSegmentBinder.class)
class InsertColumnsSegmentBinderTest {
    
    @SuppressWarnings("unchecked")
    @Test
    void assertBind() {
        ColumnSegment firstColumn = new ColumnSegment(0, 1, new IdentifierValue("order_id"));
        ColumnSegment secondColumn = new ColumnSegment(2, 3, new IdentifierValue("user_id"));
        InsertColumnsSegment segment = new InsertColumnsSegment(5, 8, Arrays.asList(firstColumn, secondColumn));
        ColumnSegment boundFirstColumn = new ColumnSegment(0, 1, new IdentifierValue("order_id"));
        ColumnSegment boundSecondColumn = new ColumnSegment(2, 3, new IdentifierValue("user_id"));
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        when(ColumnSegmentBinder.bind(eq(firstColumn), eq(SegmentType.INSERT_COLUMNS), eq(binderContext), eq(tableBinderContexts), any(Multimap.class))).thenReturn(boundFirstColumn);
        when(ColumnSegmentBinder.bind(eq(secondColumn), eq(SegmentType.INSERT_COLUMNS), eq(binderContext), eq(tableBinderContexts), any(Multimap.class))).thenReturn(boundSecondColumn);
        InsertColumnsSegment actual = InsertColumnsSegmentBinder.bind(segment, binderContext, tableBinderContexts);
        assertThat(actual.getStartIndex(), is(5));
        assertThat(actual.getStopIndex(), is(8));
        Collection<ColumnSegment> actualColumns = actual.getColumns();
        assertThat(actualColumns.size(), is(2));
        assertThat(actualColumns.iterator().next(), is(boundFirstColumn));
        assertThat(actualColumns.stream().skip(1).findFirst().orElse(null), is(boundSecondColumn));
    }
}
