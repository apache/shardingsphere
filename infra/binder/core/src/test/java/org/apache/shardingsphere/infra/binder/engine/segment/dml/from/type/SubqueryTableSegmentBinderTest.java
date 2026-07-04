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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubqueryTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindWithSubqueryTableAlias() {
        SelectStatement selectStatement = createSelectStatement(new ShorthandProjectionSegment(0, 0));
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
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        columnProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("order_id_alias")));
        SelectStatement selectStatement = createSelectStatement(columnProjectionSegment);
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
        SelectStatement selectStatement = createSelectStatement(new ShorthandProjectionSegment(0, 0));
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
        SelectStatement selectStatement = createSelectStatement(
                new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))),
                new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
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
    
    @Test
    void assertBindWithSubqueryAliasColumns() {
        SelectStatement selectStatement = createSelectStatement(
                new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))),
                new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, ""));
        AliasSegment aliasSegment = new AliasSegment(0, 0, new IdentifierValue("combined"));
        aliasSegment.getColumnAliases().add(new IdentifierValue("id"));
        aliasSegment.getColumnAliases().add(new IdentifierValue("name"));
        subqueryTableSegment.setAlias(aliasSegment);
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), selectStatement),
                tableBinderContexts, LinkedHashMultimap.create(), false);
        assertThat(actual.getColumns().size(), is(2));
        List<ColumnSegment> actualColumns = new ArrayList<>(actual.getColumns());
        assertFalse(actualColumns.get(0).getOwner().isPresent());
        assertThat(actualColumns.get(0).getIdentifier().getValue(), is("id"));
        assertThat(actualColumns.get(0).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualColumns.get(0).getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertFalse(actualColumns.get(1).getOwner().isPresent());
        assertThat(actualColumns.get(1).getIdentifier().getValue(), is("name"));
        assertThat(actualColumns.get(1).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualColumns.get(1).getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertTrue(tableBinderContexts.containsKey(CaseInsensitiveString.of("combined")));
        TableSegmentBinderContext tableSegmentBinderContext = tableBinderContexts.get(CaseInsensitiveString.of("combined")).iterator().next();
        Optional<ProjectionSegment> idProjection = tableSegmentBinderContext.findProjectionSegmentByColumnLabel("id");
        assertTrue(idProjection.isPresent());
        assertThat(idProjection.get(), isA(ColumnProjectionSegment.class));
        Optional<ProjectionSegment> nameProjection = tableSegmentBinderContext.findProjectionSegmentByColumnLabel("name");
        assertTrue(nameProjection.isPresent());
        assertThat(nameProjection.get(), isA(ColumnProjectionSegment.class));
    }
    
    @Test
    void assertBindWithSubqueryAliasColumnsAndDifferentColumnCount() {
        SelectStatement selectStatement = createSelectStatement(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, ""));
        AliasSegment aliasSegment = new AliasSegment(0, 0, new IdentifierValue("combined"));
        aliasSegment.getColumnAliases().add(new IdentifierValue("id"));
        aliasSegment.getColumnAliases().add(new IdentifierValue("name"));
        subqueryTableSegment.setAlias(aliasSegment);
        ShardingSphereMetaData metaData = createMetaData();
        assertThrows(DifferenceInColumnCountOfSelectListAndColumnNameListException.class,
                () -> SubqueryTableSegmentBinder.bind(subqueryTableSegment,
                        new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), selectStatement),
                        LinkedHashMultimap.create(), LinkedHashMultimap.create(), false));
    }
    
    private SelectStatement createSelectStatement(final ProjectionSegment... projections) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().addAll(Arrays.asList(projections));
        return SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).build();
    }
    
    private ShardingSphereMetaData createMetaData() {
        Collection<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false));
        ShardingSphereTable table = new ShardingSphereTable("t_order", columns, Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singletonList(table), Collections.emptyList());
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ResourceMetaData globalResourceMetaData = new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType,
                new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singletonList(schema), props);
        return new ShardingSphereMetaData(Collections.singletonList(database), globalResourceMetaData, globalRuleMetaData, props);
    }
}
