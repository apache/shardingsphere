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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
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
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), new SelectStatement(databaseType)), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getLeft(), isA(SimpleTableSegment.class));
        assertThat(actual.getRight(), isA(SimpleTableSegment.class));
        assertJoinTableProjectionSegments(actual.getDerivedJoinTableProjectionSegments());
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("o")));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("i")));
    }
    
    private void assertJoinTableProjectionSegments(final Collection<ProjectionSegment> joinTableProjectionSegments) {
        List<ProjectionSegment> actual = new ArrayList<>(joinTableProjectionSegments);
        assertThat(actual.size(), is(7));
        assertThat(actual.get(0), isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actual.get(0)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) actual.get(0)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(1)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(((ColumnProjectionSegment) actual.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(2)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnProjectionSegment) actual.get(2)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(3)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("item_id"));
        assertThat(((ColumnProjectionSegment) actual.get(3)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(((ColumnProjectionSegment) actual.get(4)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) actual.get(4)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(((ColumnProjectionSegment) actual.get(5)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(((ColumnProjectionSegment) actual.get(5)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(((ColumnProjectionSegment) actual.get(6)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnProjectionSegment) actual.get(6)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
    }
    
    @Test
    void assertBindWithoutAlias() {
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        SimpleTableSegment leftTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        when(joinTableSegment.getLeft()).thenReturn(leftTable);
        when(joinTableSegment.getRight()).thenReturn(rightTable);
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), new SelectStatement(databaseType)), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getLeft(), isA(SimpleTableSegment.class));
        assertThat(actual.getRight(), isA(SimpleTableSegment.class));
        assertJoinTableProjectionSegments(actual.getDerivedJoinTableProjectionSegments());
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("t_order")));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("t_order_item")));
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
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), new SelectStatement(databaseType)), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getLeft(), isA(SimpleTableSegment.class));
        assertThat(actual.getRight(), isA(SimpleTableSegment.class));
        assertJoinTableProjectionSegmentsWithNaturalJoin(actual.getDerivedJoinTableProjectionSegments());
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("o")));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("i")));
    }
    
    private void assertJoinTableProjectionSegmentsWithNaturalJoin(final Collection<ProjectionSegment> joinTableProjectionSegments) {
        List<ProjectionSegment> actual = new ArrayList<>(joinTableProjectionSegments);
        assertThat(actual.size(), is(4));
        assertThat(actual.get(0), isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actual.get(0)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) actual.get(0)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(1)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(((ColumnProjectionSegment) actual.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(2)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnProjectionSegment) actual.get(2)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(3)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("item_id"));
        assertThat(((ColumnProjectionSegment) actual.get(3)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
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
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), new SelectStatement(databaseType)), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getLeft(), isA(SimpleTableSegment.class));
        assertThat(actual.getRight(), isA(SimpleTableSegment.class));
        assertJoinTableProjectionSegmentsWithUsing(actual.getDerivedJoinTableProjectionSegments());
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("o")));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("i")));
    }
    
    private void assertJoinTableProjectionSegmentsWithUsing(final Collection<ProjectionSegment> joinTableProjectionSegments) {
        List<ProjectionSegment> actual = new ArrayList<>(joinTableProjectionSegments);
        assertThat(actual.size(), is(5));
        assertThat(actual.get(0), isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actual.get(0)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnProjectionSegment) actual.get(0)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(((ColumnProjectionSegment) actual.get(1)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) actual.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(((ColumnProjectionSegment) actual.get(2)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(((ColumnProjectionSegment) actual.get(2)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actual.get(3)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("item_id"));
        assertThat(((ColumnProjectionSegment) actual.get(3)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
        assertThat(((ColumnProjectionSegment) actual.get(4)).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(((ColumnProjectionSegment) actual.get(4)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order_item"));
    }
    
    @Test
    void assertBindWithMultiTableJoin() {
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        JoinTableSegment leftTable = mockLeftJoinSegment();
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        rightTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("i")));
        when(joinTableSegment.getLeft()).thenReturn(leftTable);
        when(joinTableSegment.getRight()).thenReturn(rightTable);
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        JoinTableSegment actual = JoinTableSegmentBinder.bind(joinTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), new SelectStatement(databaseType)), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getLeft(), isA(JoinTableSegment.class));
        assertThat(((JoinTableSegment) actual.getLeft()).getLeft(), isA(SimpleTableSegment.class));
        assertThat(((JoinTableSegment) actual.getLeft()).getRight(), isA(SimpleTableSegment.class));
        assertThat(actual.getRight(), isA(SimpleTableSegment.class));
        assertThat(actual.getDerivedJoinTableProjectionSegments().size(), is(10));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("o")));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("o2")));
        assertTrue(tableBinderContexts.containsKey(new CaseInsensitiveString("i")));
    }
    
    private JoinTableSegment mockLeftJoinSegment() {
        JoinTableSegment result = mock(JoinTableSegment.class);
        SimpleTableSegment leftTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        leftTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment rightTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        rightTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o2")));
        when(result.getLeft()).thenReturn(leftTable);
        when(result.getRight()).thenReturn(rightTable);
        return result;
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false,"int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"int", false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false,"int", false, true, false, false)));
        when(schema.getTable("t_order_item").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("item_id", Types.INTEGER, true, false,"int", false, true, false, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false,"int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"int", false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false,"int", false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order_item")).thenReturn(true);
        return result;
    }
}
