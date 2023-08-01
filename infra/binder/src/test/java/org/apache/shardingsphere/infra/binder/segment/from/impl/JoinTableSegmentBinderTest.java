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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.enums.JoinType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JoinTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindWithAlias() {
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        SimpleTableSegment leftTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        leftTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        rightTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("i")));
        when(joinTableSegment.getLeft()).thenReturn(leftTable);
        when(joinTableSegment.getRight()).thenReturn(rightTable);
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertTrue(actual.getLeft() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(actual.getRight() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getJoinTableProjectionSegments().size(), is(7));
        assertTrue(tableBinderContexts.containsKey("o"));
        assertTrue(tableBinderContexts.containsKey("i"));
    }
    
    @Test
    void assertBindWithoutAlias() {
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        SimpleTableSegment leftTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        when(joinTableSegment.getLeft()).thenReturn(leftTable);
        when(joinTableSegment.getRight()).thenReturn(rightTable);
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertTrue(actual.getLeft() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(actual.getRight() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getJoinTableProjectionSegments().size(), is(7));
        assertTrue(tableBinderContexts.containsKey("t_order"));
        assertTrue(tableBinderContexts.containsKey("t_order_item"));
    }
    
    @Test
    void assertBindWithNaturalJoin() {
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        SimpleTableSegment leftTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        leftTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        rightTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("i")));
        when(joinTableSegment.getLeft()).thenReturn(leftTable);
        when(joinTableSegment.getRight()).thenReturn(rightTable);
        when(joinTableSegment.isNatural()).thenReturn(true);
        when(joinTableSegment.getJoinType()).thenReturn(JoinType.RIGHT.name());
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertTrue(actual.getLeft() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(actual.getRight() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getJoinTableProjectionSegments().size(), is(4));
        assertTrue(tableBinderContexts.containsKey("o"));
        assertTrue(tableBinderContexts.containsKey("i"));
    }
    
    @Test
    void assertBindWithJoinUsing() {
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        SimpleTableSegment leftTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        leftTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        rightTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("i")));
        when(joinTableSegment.getLeft()).thenReturn(leftTable);
        when(joinTableSegment.getRight()).thenReturn(rightTable);
        when(joinTableSegment.getJoinType()).thenReturn(JoinType.RIGHT.name());
        when(joinTableSegment.getUsing()).thenReturn(Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("status")), new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertTrue(actual.getLeft() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getLeft()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(actual.getRight() instanceof SimpleTableSegment);
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getRight()).getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getJoinTableProjectionSegments().size(), is(5));
        assertTrue(tableBinderContexts.containsKey("o"));
        assertTrue(tableBinderContexts.containsKey("i"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)));
        when(schema.getTable("t_order_item").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        return result;
    }
}
