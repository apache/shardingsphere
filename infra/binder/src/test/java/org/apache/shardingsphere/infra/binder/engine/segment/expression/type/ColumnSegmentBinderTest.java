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

package org.apache.shardingsphere.infra.binder.engine.segment.expression.type;

import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ColumnSegmentBinderTest {
    
    @Test
    void assertBindWithMultiTablesJoinAndNoOwner() {
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>(2, 1F);
        ColumnSegment boundOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundOrderIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order"), new IdentifierValue("order_id")));
        tableBinderContexts.put("t_order", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderIdColumn))));
        ColumnSegment boundItemIdColumn = new ColumnSegment(0, 0, new IdentifierValue("item_id"));
        boundItemIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order_item"), new IdentifierValue("item_id")));
        tableBinderContexts.put("t_order_item", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundItemIdColumn))));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        SQLStatementBinderContext binderContext =
                new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.emptySet());
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.JOIN_ON, binderContext, tableBinderContexts, Collections.emptyMap());
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
    }
    
    @Test
    void assertBindFromOuterTable() {
        Map<String, TableSegmentBinderContext> outerTableBinderContexts = new LinkedHashMap<>(2, 1F);
        ColumnSegment boundOrderStatusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderStatusColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order"), new IdentifierValue("status")));
        outerTableBinderContexts.put("t_order", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderStatusColumn))));
        ColumnSegment boundOrderItemStatusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundOrderItemStatusColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order_item"), new IdentifierValue("status")));
        outerTableBinderContexts.put("t_order_item", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderItemStatusColumn))));
        SQLStatementBinderContext binderContext =
                new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.emptySet());
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, Collections.emptyMap(), outerTableBinderContexts);
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
    }
}
