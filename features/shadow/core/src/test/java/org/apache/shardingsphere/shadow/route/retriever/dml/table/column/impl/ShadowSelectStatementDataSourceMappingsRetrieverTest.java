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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ColumnExtractor.class, ShadowExtractor.class})
class ShadowSelectStatementDataSourceMappingsRetrieverTest {
    
    @Test
    void assertRetrieveWithColumnOwner() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        when(whereSegment.getExpr()).thenReturn(expressionSegment);
        when(sqlStatementContext.getWhereSegments()).thenReturn(Arrays.asList(whereSegment, mock(WhereSegment.class, RETURNS_DEEP_STUBS)));
        ColumnSegment columnSegment = mock(ColumnSegment.class, RETURNS_DEEP_STUBS);
        when(columnSegment.getColumnBoundInfo().getOriginalTable().getValue()).thenReturn("foo_tbl");
        OwnerSegment ownerSegment = new OwnerSegment(0, 0, new IdentifierValue("foo"));
        ownerSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(columnSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        when(ColumnExtractor.extract(expressionSegment)).thenReturn(Collections.singleton(columnSegment));
        when(ShadowExtractor.extractValues(expressionSegment, Collections.singletonList("foo"))).thenReturn(Optional.of(Collections.singleton("foo")));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        ShadowSelectStatementDataSourceMappingsRetriever retriever = new ShadowSelectStatementDataSourceMappingsRetriever(sqlStatementContext, Collections.singletonList("foo"));
        Collection<ShadowColumnCondition> actual = retriever.getShadowColumnConditions("foo_col");
        assertThat(actual.size(), is(1));
        ShadowColumnCondition actualCondition = actual.iterator().next();
        assertThat(actualCondition.getTable(), is("foo_tbl"));
        assertThat(actualCondition.getColumn(), is("foo_col"));
        assertThat(actualCondition.getValues(), is(Collections.singleton("foo")));
        
    }
    
    @Test
    void assertRetrieveWithoutColumnOwner() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        when(whereSegment.getExpr()).thenReturn(expressionSegment);
        when(sqlStatementContext.getWhereSegments()).thenReturn(Arrays.asList(whereSegment, mock(WhereSegment.class, RETURNS_DEEP_STUBS)));
        ColumnSegment columnSegment = mock(ColumnSegment.class, RETURNS_DEEP_STUBS);
        when(columnSegment.getColumnBoundInfo().getOriginalTable().getValue()).thenReturn("foo_tbl");
        when(columnSegment.getOwner()).thenReturn(Optional.empty());
        when(ColumnExtractor.extract(expressionSegment)).thenReturn(Collections.singleton(columnSegment));
        when(ShadowExtractor.extractValues(expressionSegment, Collections.singletonList("foo"))).thenReturn(Optional.of(Collections.singleton("foo")));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        ShadowSelectStatementDataSourceMappingsRetriever retriever = new ShadowSelectStatementDataSourceMappingsRetriever(sqlStatementContext, Collections.singletonList("foo"));
        Collection<ShadowColumnCondition> actual = retriever.getShadowColumnConditions("foo_col");
        assertThat(actual.size(), is(1));
        ShadowColumnCondition actualCondition = actual.iterator().next();
        assertThat(actualCondition.getTable(), is("foo_tbl"));
        assertThat(actualCondition.getColumn(), is("foo_col"));
        assertThat(actualCondition.getValues(), is(Collections.singleton("foo")));
    }
}
