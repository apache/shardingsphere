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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.from.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.bounded.ColumnSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
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
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        ColumnSegment boundedOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundedOrderIdColumn.setColumnBoundedInfo(new ColumnSegmentBoundedInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order"), new IdentifierValue("order_id")));
        tableBinderContexts.put("t_order", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundedOrderIdColumn))));
        ColumnSegment boundedItemIdColumn = new ColumnSegment(0, 0, new IdentifierValue("item_id"));
        boundedItemIdColumn.setColumnBoundedInfo(new ColumnSegmentBoundedInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order_item"), new IdentifierValue("item_id")));
        tableBinderContexts.put("t_order_item", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundedItemIdColumn))));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        SQLStatementBinderContext statementBinderContext =
                new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.emptySet());
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.JOIN_ON, statementBinderContext, tableBinderContexts, Collections.emptyMap());
        assertNotNull(actual.getColumnBoundedInfo());
        assertNull(actual.getOtherUsingColumnBoundedInfo());
        assertThat(actual.getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actual.getColumnBoundedInfo().getOriginalColumn().getValue(), is("order_id"));
    }
    
    @Test
    void assertBindFromOuterTable() {
        Map<String, TableSegmentBinderContext> outerTableBinderContexts = new LinkedHashMap<>();
        ColumnSegment boundedOrderStatusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundedOrderStatusColumn.setColumnBoundedInfo(new ColumnSegmentBoundedInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order"), new IdentifierValue("status")));
        outerTableBinderContexts.put("t_order", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundedOrderStatusColumn))));
        ColumnSegment boundedOrderItemStatusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        boundedOrderItemStatusColumn.setColumnBoundedInfo(new ColumnSegmentBoundedInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order_item"), new IdentifierValue("status")));
        outerTableBinderContexts.put("t_order_item", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundedOrderItemStatusColumn))));
        SQLStatementBinderContext statementBinderContext =
                new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.emptySet());
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, statementBinderContext, Collections.emptyMap(), outerTableBinderContexts);
        assertNotNull(actual.getColumnBoundedInfo());
        assertNull(actual.getOtherUsingColumnBoundedInfo());
        assertThat(actual.getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(actual.getColumnBoundedInfo().getOriginalColumn().getValue(), is("status"));
    }
}
