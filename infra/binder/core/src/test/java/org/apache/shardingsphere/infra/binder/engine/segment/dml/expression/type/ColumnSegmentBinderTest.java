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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.function.DialectFunctionOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.AmbiguousColumnException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class ColumnSegmentBinderTest {
    
    private SQLStatementBinderContext createBinderContext() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        return new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("orderByScopeCases")
    void assertBindWithOrderByScope(final String name, final SegmentType type, final String qualifier, final String projectionColumn, final String localColumn, final String localOwner,
                                    final boolean active, final String expectedTable) {
        SQLStatementBinderContext binderContext = createBinderContext();
        if (active) {
            binderContext.setOrderByFromTableBinderContexts(createOrderScope(localOwner, localColumn, "local_table"));
        }
        ColumnSegment column = new ColumnSegment(20, 26, new IdentifierValue("user_id"));
        if (null != qualifier) {
            column.setOwner(new OwnerSegment(10, 18, new IdentifierValue(qualifier)));
        }
        ColumnSegment actual = ColumnSegmentBinder.bind(column, type, binderContext, createOrderScope("", projectionColumn, "projected_table"), createOrderScope("scope", "user_id", "outer_table"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is(expectedTable));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actual.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.PHYSICAL_TABLE));
    }
    
    private static Stream<Arguments> orderByScopeCases() {
        return Stream.of(
                Arguments.of("local column before outer", SegmentType.ORDER_BY, null, "email", "user_id", "scope", true, "local_table"),
                Arguments.of("projection before local", SegmentType.ORDER_BY, null, "user_id", "user_id", "scope", true, "projected_table"),
                Arguments.of("real outer reference", SegmentType.ORDER_BY, null, "email", "email", "scope", true, "outer_table"),
                Arguments.of("no active local scope", SegmentType.ORDER_BY, null, "email", "user_id", "scope", false, "outer_table"),
                Arguments.of("nested projection ignores order scope", SegmentType.PROJECTION, null, "email", "user_id", "scope", true, "outer_table"),
                Arguments.of("qualified local alias shadows outer", SegmentType.ORDER_BY, "scope", "email", "user_id", "scope", true, "local_table"),
                Arguments.of("qualified outer alias", SegmentType.ORDER_BY, "scope", "email", "user_id", "local_alias", true, "outer_table"),
                Arguments.of("qualified inactive scope", SegmentType.ORDER_BY, "scope", "email", "user_id", "scope", false, "outer_table"),
                Arguments.of("qualified nested projection", SegmentType.PROJECTION, "scope", "email", "user_id", "scope", true, "outer_table"));
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> createOrderScope(final String owner, final String column, final String table) {
        ColumnSegment boundColumn = new ColumnSegment(0, 0, new IdentifierValue(column));
        boundColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue(table), new IdentifierValue(column), TableSourceType.PHYSICAL_TABLE));
        return ImmutableMultimap.of(CaseInsensitiveString.of(owner),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundColumn)), TableSourceType.PHYSICAL_TABLE));
    }
    
    @Test
    void assertBindAmbiguousLocalOrderByColumnBeforeOuter() {
        SQLStatementBinderContext binderContext = createBinderContext();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> localScopes = LinkedHashMultimap.create();
        localScopes.putAll(createOrderScope("left_table", "user_id", "left_table"));
        localScopes.putAll(createOrderScope("right_table", "user_id", "right_table"));
        binderContext.setOrderByFromTableBinderContexts(localScopes);
        assertThrows(AmbiguousColumnException.class, () -> ColumnSegmentBinder.bind(new ColumnSegment(20, 26, new IdentifierValue("user_id")), SegmentType.ORDER_BY,
                binderContext, createOrderScope("", "email", "projected_table"), createOrderScope("scope", "user_id", "outer_table")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("columnBoundInfoCases")
    void assertCreateColumnSegmentBoundInfo(final String name, final String originalTable, final ColumnSegment input, final String expectedTable, final String expectedColumn,
                                            final String expectedDatabase, final String expectedSchema, final OwnerSegment expectedOwner) {
        ColumnSegment column = new ColumnSegment(20, 26, new IdentifierValue("logical_column"));
        column.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("stale_db"), new IdentifierValue("stale_schema")),
                new IdentifierValue(originalTable), new IdentifierValue("logical_column"), TableSourceType.PHYSICAL_TABLE));
        ColumnSegmentBoundInfo actual = ColumnSegmentBinder.createColumnSegmentBoundInfo(column, input, TableSourceType.TEMPORARY_TABLE);
        assertThat(actual.getOriginalDatabase().getValue(), is(expectedDatabase));
        assertThat(actual.getOriginalSchema().getValue(), is(expectedSchema));
        assertThat(actual.getOriginalTable().getValue(), is(expectedTable));
        assertThat(actual.getOriginalColumn().getValue(), is(expectedColumn));
        assertThat(actual.getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        assertThat(actual.getOwner(), is(expectedOwner));
        if (null != input) {
            assertThat(actual, not(sameInstance(input.getColumnBoundInfo())));
        }
    }
    
    private static Stream<Arguments> columnBoundInfoCases() {
        ColumnSegment input = new ColumnSegment(0, 0, new IdentifierValue("source_column"));
        input.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("source_db"), new IdentifierValue("source_schema")),
                new IdentifierValue("source_table"), new IdentifierValue("source_column"), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment ownedInput = new ColumnSegment(0, 0, new IdentifierValue("source_column"));
        ownedInput.setColumnBoundInfo(input.getColumnBoundInfo());
        OwnerSegment owner = new OwnerSegment(0, 0, new IdentifierValue("source_alias"));
        ownedInput.setOwner(owner);
        return Stream.of(
                Arguments.of("no source clears database", "explicit_table", null, "explicit_table", "logical_column", "", "", null),
                Arguments.of("source restores table and column", "", input, "source_table", "source_column", "source_db", "source_schema", null),
                Arguments.of("explicit table preserved", "explicit_table", input, "explicit_table", "source_column", "source_db", "source_schema", null),
                Arguments.of("source owner forwarded", "", ownedInput, "source_table", "source_column", "source_db", "source_schema", owner));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("declaredVariableCases")
    void assertBindDeclaredVariable(final String name, final String columnName, final String owner, final Collection<String> variableNames) {
        SQLStatementBinderContext binderContext = createBinderContext();
        when(binderContext.getSqlStatement().getVariableNames()).thenReturn(variableNames);
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        if (null != owner) {
            column.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        }
        ColumnSegment actual = ColumnSegmentBinder.bind(column, SegmentType.PROJECTION, binderContext, ImmutableMultimap.of(), ImmutableMultimap.of());
        assertTrue(actual.isVariable());
    }
    
    private static Stream<Arguments> declaredVariableCases() {
        return Stream.of(
                Arguments.of("declared variable", "var_value", null, Collections.singleton("var_value")),
                Arguments.of("case insensitive second variable", "VAR_VALUE", null, Arrays.asList("unused", "var_value")),
                Arguments.of("record variable owner", "field_name", "row_value", Collections.singleton("row_value")));
    }
    
    @Test
    void assertBindOuterColumnFromEarlierMatchingScope() {
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.setOrderByFromTableBinderContexts(createOrderScope("local_alias", "email", "local_table"));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerScopes = LinkedHashMultimap.create();
        outerScopes.putAll(createOrderScope("first_scope", "user_id", "first_table"));
        outerScopes.putAll(createOrderScope("last_scope", "other_column", "last_table"));
        ColumnSegment actual = ColumnSegmentBinder.bind(new ColumnSegment(0, 0, new IdentifierValue("user_id")), SegmentType.ORDER_BY,
                binderContext, createOrderScope("", "email", "projected_table"), outerScopes);
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("first_table"));
    }
    
    @Test
    void assertBindUnknownQualifiedOrderByColumn() {
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.setOrderByFromTableBinderContexts(createOrderScope("local_alias", "user_id", "local_table"));
        ColumnSegment column = new ColumnSegment(20, 26, new IdentifierValue("user_id"));
        column.setOwner(new OwnerSegment(0, 18, new IdentifierValue("missing_scope")));
        assertThrows(ColumnNotFoundException.class, () -> ColumnSegmentBinder.bind(column, SegmentType.ORDER_BY,
                binderContext, createOrderScope("", "email", "projected_table"), createOrderScope("outer_alias", "user_id", "outer_table")));
    }
    
    @Test
    void assertBindQualifiedExternalOrderByColumn() {
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.setOrderByFromTableBinderContexts(createOrderScope("local_alias", "user_id", "local_table"));
        binderContext.getExternalTableBinderContexts().putAll(createOrderScope("external_alias", "user_id", "external_table"));
        ColumnSegment column = new ColumnSegment(20, 26, new IdentifierValue("user_id"));
        column.setOwner(new OwnerSegment(0, 18, new IdentifierValue("external_alias")));
        ColumnSegment actual = ColumnSegmentBinder.bind(column, SegmentType.ORDER_BY, binderContext,
                createOrderScope("", "email", "projected_table"), createOrderScope("outer_alias", "user_id", "outer_table"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("external_table"));
    }
    
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
        SQLStatementBinderContext binderContext = createBinderContext();
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
        SQLStatementBinderContext binderContext = createBinderContext();
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
    void assertBindParameterMarkerProjectionFromExternalTable() {
        IdentifierValue sourceTableName = new IdentifierValue("\"Source\"");
        IdentifierValue sourceColumnName = new IdentifierValue("\"NewEmail\"");
        ParameterMarkerExpressionSegment sourceProjection = new ParameterMarkerExpressionSegment(0, 11, 0);
        sourceProjection.setAlias(new AliasSegment(2, 11, sourceColumnName));
        sourceProjection.setBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue("")),
                sourceTableName, sourceColumnName, TableSourceType.TEMPORARY_TABLE));
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.getExternalTableBinderContexts().put(CaseInsensitiveString.of(sourceTableName.getValue()),
                new SimpleTableSegmentBinderContext(Collections.singleton(sourceProjection), TableSourceType.TEMPORARY_TABLE));
        ColumnSegment actual = ColumnSegmentBinder.bind(new ColumnSegment(20, 29, sourceColumnName), SegmentType.SET_ASSIGNMENT,
                binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(actual.getColumnBoundInfo().getOriginalTable(), is(sourceTableName));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn(), is(sourceColumnName));
        assertThat(actual.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindParameterMarkerProjectionWithoutBoundInfoFromExternalTable() {
        IdentifierValue sourceColumnName = new IdentifierValue("\"NewEmail\"");
        ParameterMarkerExpressionSegment sourceProjection = new ParameterMarkerExpressionSegment(0, 11, 0);
        sourceProjection.setAlias(new AliasSegment(2, 11, sourceColumnName));
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.getExternalTableBinderContexts().put(CaseInsensitiveString.of("foo_cte"),
                new SimpleTableSegmentBinderContext(Collections.singleton(sourceProjection), TableSourceType.TEMPORARY_TABLE));
        ColumnSegment actual = ColumnSegmentBinder.bind(new ColumnSegment(20, 29, sourceColumnName), SegmentType.SET_ASSIGNMENT,
                binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertNotNull(actual.getColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalColumn(), is(sourceColumnName));
    }
    
    @Test
    void assertBindModelColumn() {
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.getModelColumnNames().add("projected_price");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("projected_price"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("projected_price"));
        assertThat(actual.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindModelColumnWithSamePhysicalColumn() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundProjectedPriceColumn = new ColumnSegment(0, 0, new IdentifierValue("projected_price"));
        boundProjectedPriceColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_product"), new IdentifierValue("projected_price"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_product"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundProjectedPriceColumn)), TableSourceType.PHYSICAL_TABLE));
        SQLStatementBinderContext binderContext = createBinderContext();
        binderContext.getModelColumnNames().add("projected_price");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("projected_price"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, tableBinderContexts, LinkedHashMultimap.create());
        assertNotNull(actual.getColumnBoundInfo());
        assertNull(actual.getOtherUsingColumnBoundInfo());
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("projected_price"));
        assertThat(actual.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
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
        SQLStatementBinderContext binderContext = createBinderContext();
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
        SQLStatementBinderContext binderContext = createBinderContext();
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
        SQLStatementBinderContext binderContext = createBinderContext();
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
    
    @Test
    void assertBindNestedObjectColumnWhenOwnerIsNotTable() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundStructColumn = new ColumnSegment(0, 0, new IdentifierValue("type_struct"));
        boundStructColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_data_type_struct"), new IdentifierValue("type_struct"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_data_type_struct"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundStructColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment fieldSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        fieldSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("type_struct")));
        ColumnSegment actual = ColumnSegmentBinder.bind(fieldSegment, SegmentType.PROJECTION, createBinderContext(), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getExpression(), is("type_struct.name"));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_data_type_struct"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("type_struct"));
    }
    
    @Test
    void assertBindProcedureRecordFieldVariable() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "MySQL")).build();
        selectStatement.getVariableNames().add("v_record");
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
        ColumnSegment columnSegment = new ColumnSegment(0, 20, new IdentifierValue("sensitive_a"));
        columnSegment.setOwner(new OwnerSegment(0, 7, new IdentifierValue("v_record")));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertTrue(actual.isVariable());
        assertTrue(actual.getOwner().isPresent());
        assertThat(actual.getExpression(), is("v_record.sensitive_a"));
        assertThat(actual.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindValueVariableBeforeTargetTableColumn() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "MySQL")).build();
        selectStatement.getVariableNames().add("sensitive_a");
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment targetColumn = new ColumnSegment(0, 0, new IdentifierValue("sensitive_a"));
        targetColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_sensitive"), new IdentifierValue("sensitive_a"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_sensitive"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(targetColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment actual = ColumnSegmentBinder.bind(new ColumnSegment(0, 10, new IdentifierValue("sensitive_a")),
                SegmentType.VALUES, binderContext, tableBinderContexts, LinkedHashMultimap.create());
        assertTrue(actual.isVariable());
        assertThat(actual.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindUnparenthesizedFunction() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("CURRENT_DATE"));
        ColumnSegment actual = ColumnSegmentBinder.bind(
                columnSegment, SegmentType.PROJECTION, createBinderContext(), LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(actual, is(columnSegment));
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is(""));
    }
    
    @Test
    void assertBindUnparenthesizedQualifiedFunction() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).build();
        DialectFunctionOption functionOption = mock(DialectFunctionOption.class);
        when(functionOption.getUnparenthesizedFunctionNames()).thenReturn(Collections.emptyList());
        when(functionOption.getUnparenthesizedQualifiedFunctionNames()).thenReturn(Collections.singleton("DBMS_RANDOM.VALUE"));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getFunctionOption()).thenReturn(functionOption);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
        ColumnSegment columnSegment = new ColumnSegment(0, 16, new IdentifierValue("VALUE"));
        columnSegment.setOwner(new OwnerSegment(0, 10, new IdentifierValue("DBMS_RANDOM")));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(
                        DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData))) {
            ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
            assertThat(actual, is(columnSegment));
        }
    }
    
    @Test
    void assertBindUnparenthesizedFunctionAsColumn() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundColumn = new ColumnSegment(0, 0, new IdentifierValue("CURRENT_DATE"));
        boundColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("CURRENT_DATE"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundColumn)), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment actual = ColumnSegmentBinder.bind(
                new ColumnSegment(0, 0, new IdentifierValue("CURRENT_DATE")), SegmentType.PROJECTION, createBinderContext(), tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actual.getColumnBoundInfo().getOriginalColumn().getValue(), is("CURRENT_DATE"));
    }
    
    @Test
    void assertBindQuotedUnparenthesizedFunctionWithoutColumn() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("`CURRENT_DATE`"));
        SQLStatementBinderContext binderContext = createBinderContext();
        assertThrows(ColumnNotFoundException.class,
                () -> ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create()));
    }
    
    @Test
    void assertBindUnparenthesizedFunctionAsAssignmentColumnWithoutColumn() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("CURRENT_DATE"));
        SQLStatementBinderContext binderContext = createBinderContext();
        assertThrows(ColumnNotFoundException.class,
                () -> ColumnSegmentBinder.bind(columnSegment, SegmentType.SET_ASSIGNMENT_COLUMNS, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create()));
    }
    
    @Test
    void assertBindExcludedColumnInSetAssignment() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLStatementBinderContext bindContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("EXCLUDED")));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.SET_ASSIGNMENT, bindContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(columnSegment, is(actual));
        assertTrue(actual.getOwner().isPresent());
        assertThat(actual.getOwner().get().getIdentifier().getValue(), is("EXCLUDED"));
    }
    
    @Test
    void assertBindUnparenthesizedSystemUserForPostgreSQL() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLStatementBinderContext bindContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("SYSTEM_USER"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, bindContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(actual, is(columnSegment));
    }
    
    @Test
    void assertBindUnparenthesizedSystemUserWithTableContextForPostgreSQL() {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        ColumnSegment boundOrderIdColumn = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        boundOrderIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("t_order"), new IdentifierValue("order_id"), TableSourceType.PHYSICAL_TABLE));
        tableBinderContexts.put(CaseInsensitiveString.of("t_order"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundOrderIdColumn)), TableSourceType.PHYSICAL_TABLE));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLStatementBinderContext bindContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), selectStatement);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("SYSTEM_USER"));
        ColumnSegment actual = ColumnSegmentBinder.bind(columnSegment, SegmentType.PROJECTION, bindContext, tableBinderContexts, LinkedHashMultimap.create());
        assertThat(actual, is(columnSegment));
    }
}
