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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.combine;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.invocation.InvocationOnMock;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class CombineSegmentBinderTest {
    
    @Test
    void assertBind() {
        SelectStatement leftSelect = new SelectStatement(mock(DatabaseType.class));
        SelectStatement rightSelect = new SelectStatement(mock(DatabaseType.class));
        SubquerySegment leftSegment = new SubquerySegment(1, 5, leftSelect, "LEFT");
        SubquerySegment rightSegment = new SubquerySegment(6, 10, rightSelect, "RIGHT");
        CombineSegment segment = new CombineSegment(0, 20, leftSegment, CombineType.UNION, rightSegment);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(
                mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS), "foo_db", new HintValueContext(), mock(SQLStatement.class));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> externalContexts = binderContext.getExternalTableBinderContexts();
        CaseInsensitiveString tableKey = new CaseInsensitiveString("t_order");
        externalContexts.put(tableKey, mock(TableSegmentBinderContext.class));
        Collection<String> cteAliases = binderContext.getCommonTableExpressionsSegmentsUniqueAliases();
        cteAliases.add("existing_cte");
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        SelectStatement boundLeftSelect = new SelectStatement(mock(DatabaseType.class));
        SelectStatement boundRightSelect = new SelectStatement(mock(DatabaseType.class));
        AtomicReference<SQLStatementBinderContext> capturedLeftContext = new AtomicReference<>();
        AtomicReference<SQLStatementBinderContext> capturedRightContext = new AtomicReference<>();
        try (MockedConstruction<SelectStatementBinder> ignore = mockConstruction(SelectStatementBinder.class, (mock, context) -> {
            when(mock.bind(eq(leftSelect), any(SQLStatementBinderContext.class))).thenAnswer(invocation -> {
                capturedLeftContext.set(getSqlStatementBinderContext(invocation, "left_cte"));
                return boundLeftSelect;
            });
            when(mock.bind(eq(rightSelect), any(SQLStatementBinderContext.class))).thenAnswer(invocation -> {
                capturedRightContext.set(getSqlStatementBinderContext(invocation, "right_cte"));
                return boundRightSelect;
            });
        })) {
            CombineSegment actual = CombineSegmentBinder.bind(segment, binderContext, outerTableBinderContexts);
            assertThat(actual.getStartIndex(), is(0));
            assertThat(actual.getStopIndex(), is(20));
            assertThat(actual.getLeft().getSelect(), is(boundLeftSelect));
            assertThat(actual.getRight().getSelect(), is(boundRightSelect));
        }
        assertThat(capturedLeftContext.get().getExternalTableBinderContexts().get(tableKey).iterator().next(), is(externalContexts.get(tableKey).iterator().next()));
        assertThat(capturedRightContext.get().getExternalTableBinderContexts().get(tableKey).iterator().next(), is(externalContexts.get(tableKey).iterator().next()));
        assertThat(binderContext.getCommonTableExpressionsSegmentsUniqueAliases(), hasItems("existing_cte", "left_cte", "right_cte"));
    }
    
    private static SQLStatementBinderContext getSqlStatementBinderContext(final InvocationOnMock invocation, final String commonTableExpressionsSegmentsUniqueAlias) {
        SQLStatementBinderContext subqueryContext = invocation.getArgument(1);
        subqueryContext.getCommonTableExpressionsSegmentsUniqueAliases().add(commonTableExpressionsSegmentsUniqueAlias);
        return subqueryContext;
    }
}
