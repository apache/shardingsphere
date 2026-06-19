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
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubqueryTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindWithSubqueryTableAlias() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("temp")));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), selectStatement), tableBinderContexts, LinkedHashMultimap.create(), false);
        assertTrue(actual.getAlias().isPresent());
        assertTrue(tableBinderContexts.containsKey(CaseInsensitiveString.of("temp")));
        List<ProjectionSegment> projectionSegments = new ArrayList<>(tableBinderContexts.get(CaseInsensitiveString.of("temp")).iterator().next().getProjectionSegments());
        assertThat(projectionSegments.size(), is(3));
        assertThat(projectionSegments.get(0), isA(ColumnProjectionSegment.class));
        assertTrue(((ColumnProjectionSegment) projectionSegments.get(0)).getColumn().getOwner().isPresent());
        assertThat(((ColumnProjectionSegment) projectionSegments.get(0)).getColumn().getOwner().get().getIdentifier().getValue(), is("temp"));
        assertThat(((ColumnProjectionSegment) projectionSegments.get(0)).getColumn().getIdentifier().getValue(), is("order_id"));
        assertThat(projectionSegments.get(1), isA(ColumnProjectionSegment.class));
        assertTrue(((ColumnProjectionSegment) projectionSegments.get(1)).getColumn().getOwner().isPresent());
        assertThat(((ColumnProjectionSegment) projectionSegments.get(1)).getColumn().getOwner().get().getIdentifier().getValue(), is("temp"));
        assertThat(((ColumnProjectionSegment) projectionSegments.get(1)).getColumn().getIdentifier().getValue(), is("user_id"));
        assertThat(projectionSegments.get(2), isA(ColumnProjectionSegment.class));
        assertTrue(((ColumnProjectionSegment) projectionSegments.get(2)).getColumn().getOwner().isPresent());
        assertThat(((ColumnProjectionSegment) projectionSegments.get(2)).getColumn().getOwner().get().getIdentifier().getValue(), is("temp"));
        assertThat(((ColumnProjectionSegment) projectionSegments.get(2)).getColumn().getIdentifier().getValue(), is("status"));
    }
    
    @Test
    void assertBindWithSubqueryProjectionAlias() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        columnProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("order_id_alias")));
        projectionsSegment.getProjections().add(columnProjectionSegment);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("temp")));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), selectStatement), tableBinderContexts, LinkedHashMultimap.create(), false);
        assertTrue(actual.getAlias().isPresent());
        assertTrue(tableBinderContexts.containsKey(CaseInsensitiveString.of("temp")));
        List<ProjectionSegment> projectionSegments = new ArrayList<>(tableBinderContexts.get(CaseInsensitiveString.of("temp")).iterator().next().getProjectionSegments());
        assertThat(projectionSegments.size(), is(1));
        assertThat(projectionSegments.get(0), isA(ColumnProjectionSegment.class));
        assertTrue(((ColumnProjectionSegment) projectionSegments.get(0)).getColumn().getOwner().isPresent());
        assertThat(((ColumnProjectionSegment) projectionSegments.get(0)).getColumn().getOwner().get().getIdentifier().getValue(), is("temp"));
        assertThat(((ColumnProjectionSegment) projectionSegments.get(0)).getColumn().getIdentifier().getValue(), is("order_id_alias"));
    }
    
    @Test
    void assertBindWithoutSubqueryTableAlias() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, ""));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), selectStatement), tableBinderContexts, LinkedHashMultimap.create(), false);
        assertFalse(actual.getAlias().isPresent());
        assertTrue(tableBinderContexts.containsKey(CaseInsensitiveString.of("")));
    }
    
    @Test
    void assertBindWithSubqueryTableColumns() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("combined")));
        subqueryTableSegment.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("id")));
        subqueryTableSegment.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("name")));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), selectStatement),
                tableBinderContexts, LinkedHashMultimap.create(), false);
        assertThat(actual.getColumns().size(), is(2));
        List<ColumnSegment> actualColumns = new ArrayList<>(actual.getColumns());
        assertFalse(actualColumns.get(0).getOwner().isPresent());
        assertThat(actualColumns.get(0).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualColumns.get(0).getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertFalse(actualColumns.get(1).getOwner().isPresent());
        assertThat(actualColumns.get(1).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualColumns.get(1).getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertTrue(tableBinderContexts.containsKey(CaseInsensitiveString.of("combined")));
        TableSegmentBinderContext tableSegmentBinderContext = tableBinderContexts.get(CaseInsensitiveString.of("combined")).iterator().next();
        Optional<ProjectionSegment> idProjection = tableSegmentBinderContext.findProjectionSegmentByColumnLabel("id");
        assertTrue(idProjection.isPresent());
        assertThat(idProjection.get(), isA(ColumnProjectionSegment.class));
        ColumnProjectionSegment actualIdProjection = (ColumnProjectionSegment) idProjection.get();
        assertThat(actualIdProjection.getColumn().getIdentifier().getValue(), is("id"));
        assertThat(actualIdProjection.getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actualIdProjection.getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(actualIdProjection.getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualIdProjection.getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actualIdProjection.getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        Optional<ProjectionSegment> nameProjection = tableSegmentBinderContext.findProjectionSegmentByColumnLabel("name");
        assertTrue(nameProjection.isPresent());
        assertThat(nameProjection.get(), isA(ColumnProjectionSegment.class));
        ColumnProjectionSegment actualNameProjection = (ColumnProjectionSegment) nameProjection.get();
        assertThat(actualNameProjection.getColumn().getIdentifier().getValue(), is("name"));
        assertThat(actualNameProjection.getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actualNameProjection.getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(actualNameProjection.getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualNameProjection.getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(actualNameProjection.getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase)).thenReturn(schema);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase(fooDatabase).getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(fooDatabase)).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tOrder)).thenReturn(true);
        return result;
    }
}
