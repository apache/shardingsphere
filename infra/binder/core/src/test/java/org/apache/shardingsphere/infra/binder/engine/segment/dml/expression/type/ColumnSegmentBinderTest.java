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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.syntax.AmbiguousColumnException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ColumnSegmentBinderTest {
    
    @Test
    void assertBindWithMultiTablesJoinAndNoOwner() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundOrderIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("order_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderIdColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment boundItemIdColumn = new ColumnSegment(0, 0, new IdentifierValue("item_id"));
        boundItemIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order_item"), new IdentifierValue("item_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order_item"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundItemIdColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), mock(SelectStatement.class));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.JOIN_ON, binderContext, tableBinderContexts, LinkedHashMultimap.create());
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actual.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_schema"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
    }
    
    @Test
    void assertBindFromOuterTable() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderStatusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderStatusColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        outerTableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderStatusColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment boundOrderItemStatusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderItemStatusColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order_item"), new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        outerTableBinderContexts.put(CaseInsensitiveString.of("t_order_item"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderItemStatusColumn)), TableSourceType.PHYSICAL_TABLE));
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), mock(SelectStatement.class));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, LinkedHashMultimap.create(), outerTableBinderContexts);
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actual.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_schema"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
    }
    
    @Test
    void assertBindWithSameTableAliasAndSameProjection() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("temp"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment boundOrderItemColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderItemColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order_item"), new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("temp"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderItemColumn)), TableSourceType.PHYSICAL_TABLE));
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), mock(SelectStatement.class));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("temp")));
        assertThrows(AmbiguousColumnException.class, () -> ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, LinkedHashMultimap.create()));
    }
    
    @Test
    void assertBindWithSameTableAliasAndDifferentProjection() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundOrderColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("order_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("temp"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment boundOrderItemColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderItemColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order_item"), new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("temp"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderItemColumn)), TableSourceType.PHYSICAL_TABLE));
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), mock(SelectStatement.class));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("temp")));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, LinkedHashMultimap.create());
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actual.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_schema"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
    }
    
    @Test
    void assertBindOwner() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundOrderIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("order_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderIdColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment boundItemIdColumn = new ColumnSegment(0, 0, new IdentifierValue("item_id"));
        boundItemIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order_item"), new IdentifierValue("item_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order_item"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundItemIdColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("t_order")));
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), mock(SelectStatement.class));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.JOIN_ON, binderContext, tableBinderContexts, LinkedHashMultimap.create());
        assertTrue(actual.getOwner().isPresent());
        assertTrue(actual.getOwner().get().getTableBoundInfo().isPresent());
        TableSegmentBoundInfo actualTableBoundInfo = actual.getOwner().get().getTableBoundInfo().get();
        assertThat(actualTableBoundInfo.getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actualTableBoundInfo.getOriginalSchema().getValue(), is("foo_schema"));
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actual.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_schema"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
    }
}
