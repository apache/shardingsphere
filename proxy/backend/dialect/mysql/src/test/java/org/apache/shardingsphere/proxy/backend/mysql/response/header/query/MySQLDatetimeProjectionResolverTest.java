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

package org.apache.shardingsphere.proxy.backend.mysql.response.header.query;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine.ProjectionEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.SubqueryExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLDatetimeProjectionResolverTest {
    
    private static final DatabaseType MYSQL = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource({"datetimeProjectionArguments", "datetimeFunctionArguments", "datetimeResultExpressionArguments"})
    void assertIsDatetimeWithDatetime(final String name, final SelectStatementContext context, final ShardingSphereDatabase database, final int columnIndex) {
        assertTrue(MySQLDatetimeProjectionResolver.isDatetime(context, database, columnIndex));
    }
    
    private static Stream<Arguments> datetimeProjectionArguments() {
        ShardingSphereDatabase mysqlDatabase = createColumnsDatabase();
        setStorageTypes(mysqlDatabase, "MySQL");
        ShardingSphereDatabase mixedDatabase = createColumnsDatabase();
        setStorageTypes(mixedDatabase, "MySQL", "PostgreSQL");
        ColumnProjectionSegment temporary = column("foo_alias");
        temporary.getColumn().setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(null, null), new IdentifierValue("foo_table"),
                new IdentifierValue("foo_datetime"), TableSourceType.TEMPORARY_TABLE));
        ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(0, 0);
        shorthand.getActualProjectionSegments().add(column("foo_integer"));
        shorthand.getActualProjectionSegments().add(column("foo_datetime"));
        AggregationDistinctProjectionSegment distinctMaximum = new AggregationDistinctProjectionSegment(0, 0, AggregationType.MAX, "MAX(DISTINCT foo_datetime)", "foo_datetime");
        distinctMaximum.getParameters().add(column("foo_datetime").getColumn());
        ColumnProjectionSegment aliasedOrigin = boundColumn("foo_other_alias", "foo_datetime");
        SelectStatement aliasedSimple = select(aliasedTable("foo_table", "foo_alias"), aliasedOrigin);
        SelectStatementContext legacyProjection = mock(SelectStatementContext.class);
        when(legacyProjection.getSqlStatement()).thenReturn(select(table("foo_table"), column("foo_datetime")));
        when(legacyProjection.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, Collections.singleton(new ColumnProjection("foo_table", "foo_datetime", null, MYSQL))));
        ColumnProjectionSegment originalSchemaColumn = column("foo_alias");
        originalSchemaColumn.getColumn().setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")),
                new IdentifierValue("foo_table"), new IdentifierValue("foo_datetime"), TableSourceType.PHYSICAL_TABLE));
        ShardingSphereDatabase originalSchemaDatabase = mock(ShardingSphereDatabase.class);
        when(originalSchemaDatabase.getName()).thenReturn("foo_db");
        when(originalSchemaDatabase.getSchema("foo_db")).thenReturn(new ShardingSphereSchema("foo_db", MYSQL, Collections.singleton(createTable("foo_table")), Collections.emptyList()));
        SelectStatement aliasedUnion = combine(select(table("foo_table"), aliasedColumn("foo_timestamp", "foo_x")), select(table("foo_table"), column("foo_datetime")));
        ColumnProjectionSegment boundDerived = boundColumn("foo_x", "foo_timestamp");
        boundDerived.getColumn().getColumnBoundInfo().setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_union")));
        ShardingSphereDatabase columnsDatabase = createColumnsDatabase();
        SelectStatement union = combine(select(table("foo_table"), column("foo_timestamp")), select(table("foo_table"), column("foo_datetime")));
        return Stream.of(
                Arguments.of("legacy-null-bound-projection", legacyProjection, columnsDatabase, 1),
                Arguments.of("second-result-column", context(select(table("foo_table"), column("foo_integer"), column("foo_datetime"))), columnsDatabase, 2),
                args("original-schema-and-database", select(table("foo_table"), originalSchemaColumn), originalSchemaDatabase),
                args("correlated-bound-column", select(table("foo_table"), new SubqueryProjectionSegment(subquery(select(null, boundColumn("foo_alias", "foo_datetime")), 0), "foo_scalar")),
                        columnsDatabase),
                args("bound-datetime", select(table("foo_table"), boundColumn("foo_alias", "foo_datetime")), columnsDatabase),
                args("hint-simple-datetime", select(table("foo_table"), column("foo_datetime")), columnsDatabase),
                args("hint-qualified-simple-datetime", select(table("foo_table"), qualifiedColumn("foo_table", "foo_datetime")), columnsDatabase),
                args("hint-derived-renamed-datetime", select(derived(select(table("foo_table"), aliasedColumn("foo_datetime", "foo_x")), "foo_alias"), qualifiedColumn("foo_alias", "foo_x")),
                        columnsDatabase),
                args("temporary-bound-source", select(table("foo_cte"), temporary), columnsDatabase),
                args("bound-derived-unknown-retains-datetime", select(derived(select(table("foo_table"), aliasedExpression(function("IFNULL", column("foo_datetime").getColumn(),
                        column("foo_timestamp").getColumn()), "foo_alias")), "foo_derived"), temporary), columnsDatabase),
                args("bound-alias-does-not-override-origin", aliasedSimple, columnsDatabase),
                args("datetime-timestamp-union", combine(select(table("foo_table"), column("foo_datetime")), select(table("foo_table"), column("foo_timestamp"))), mysqlDatabase),
                args("timestamp-datetime-union", union, mysqlDatabase),
                args("nested-union", combine(select(table("foo_table"), column("foo_timestamp")), union), mysqlDatabase),
                args("datetime-null-union", combine(select(table("foo_table"), column("foo_datetime")), select(null, expression(new LiteralExpressionSegment(0, 0, null)))), mysqlDatabase),
                args("null-datetime-union", combine(select(null, expression(new LiteralExpressionSegment(0, 0, null))), select(table("foo_table"), column("foo_datetime"))), mysqlDatabase),
                args("date-timestamp-union", combine(select(table("foo_table"), column("foo_date")), select(table("foo_table"), column("foo_timestamp"))), mysqlDatabase),
                args("datetime-date-union", combine(select(table("foo_table"), column("foo_datetime")), select(table("foo_table"), column("foo_date"))), mysqlDatabase),
                args("mixed-storage-datetime-first", combine(select(table("foo_table"), column("foo_datetime")), select(table("foo_table"), column("foo_timestamp"))), mixedDatabase),
                args("unknown-second-union", combine(select(table("foo_table"), column("foo_datetime")), select(table("foo_table"), column("foo_unknown"))), columnsDatabase),
                args("unexpanded-second-union", combine(select(table("foo_table"), column("foo_datetime")), select(table("foo_table"), new ShorthandProjectionSegment(0, 0))), columnsDatabase),
                args("derived-union", select(derived(union, "foo_union"), qualifiedColumn("foo_union", "foo_timestamp")), mysqlDatabase),
                args("nested-derived-union", select(derived(select(derived(union, "foo_inner"), aliasedQualifiedColumn("foo_inner", "foo_timestamp", "foo_x")), "foo_outer"),
                        qualifiedColumn("foo_outer", "foo_x")), mysqlDatabase),
                Arguments.of("derived-second-column", context(select(derived(select(table("foo_table"), column("foo_integer"), column("foo_datetime")), "foo_alias"),
                        qualifiedColumn("foo_alias", "foo_datetime"))), columnsDatabase, 1),
                args("derived-expanded-shorthand", select(derived(select(table("foo_table"), shorthand), "foo_alias"), qualifiedColumn("foo_alias", "foo_datetime")), columnsDatabase),
                args("minimum-datetime", select(table("foo_table"), aggregation(AggregationType.MIN, column("foo_datetime").getColumn())), columnsDatabase),
                args("maximum-datetime", select(table("foo_table"), aggregation(AggregationType.MAX, column("foo_datetime").getColumn())), columnsDatabase),
                args("distinct-maximum", select(table("foo_table"), distinctMaximum), columnsDatabase),
                args("wrapped-minimum", select(table("foo_table"), expression(aggregation(AggregationType.MIN, column("foo_datetime").getColumn()))), columnsDatabase),
                args("maximum-derived-union", select(derived(union, "foo_union"), aggregation(AggregationType.MAX, qualifiedColumn("foo_union", "foo_timestamp").getColumn())), mysqlDatabase),
                args("scalar-subquery", select(null, new SubqueryProjectionSegment(subquery(select(table("foo_table"), column("foo_datetime")), 0), "foo_scalar")), columnsDatabase),
                args("unqualified-bound-join", select(join(table("foo_table"), table("bar_table")), boundColumn("foo_alias", "foo_datetime")), columnsDatabase),
                args("bound-derived-join-owner", select(join(derived(aliasedUnion, "foo_union"), table("bar_table")), boundDerived), mysqlDatabase),
                args("bound-entire-derived-union", select(derived(aliasedUnion, "foo_union"), boundColumn("foo_x", "foo_timestamp")), mysqlDatabase),
                args("bound-join-with-unrelated-subquery", select(join(table("foo_table"), table("bar_table")), boundColumn("foo_alias", "foo_datetime"),
                        new SubqueryProjectionSegment(subquery(select(table("foo_table"), column("foo_timestamp")), 0), "foo_scalar")), columnsDatabase),
                args("qualified-join-source", select(join(aliasedTable("foo_table", "foo_left"), aliasedTable("bar_table", "foo_right")), qualifiedColumn("foo_left", "foo_datetime")),
                        columnsDatabase),
                args("qualified-right-join-source", select(join(aliasedTable("foo_table", "foo_left"), aliasedTable("bar_table", "foo_right")), qualifiedColumn("foo_right", "foo_datetime")),
                        columnsDatabase));
    }
    
    private static Stream<Arguments> datetimeFunctionArguments() {
        return Stream.of(
                args("current-timestamp", select(null, expression(function("CURRENT_TIMESTAMP"))), createFunctionDatabase("MySQL")),
                args("now", select(null, expression(function("NOW"))), createFunctionDatabase("MySQL")),
                args("localtime", select(null, expression(function("LOCALTIME"))), createFunctionDatabase("MySQL")),
                args("localtimestamp", select(null, expression(function("LOCALTIMESTAMP"))), createFunctionDatabase("MySQL", "MySQL")));
    }
    
    private static Stream<Arguments> datetimeResultExpressionArguments() {
        ShardingSphereDatabase database = createColumnsDatabase();
        setStorageTypes(database, "MySQL");
        ExpressionSegment datetime = column("foo_datetime").getColumn();
        ExpressionSegment timestamp = column("foo_timestamp").getColumn();
        ExpressionSegment nullLiteral = new LiteralExpressionSegment(0, 0, null);
        return Stream.of(
                args("case-datetime", select(table("foo_table"), expression(caseWhen(Collections.singleton(datetime), datetime))), database),
                args("case-timestamp-datetime", select(table("foo_table"), expression(caseWhen(Collections.singleton(timestamp), datetime))), database),
                args("case-datetime-null", select(table("foo_table"), expression(caseWhen(Collections.singleton(datetime), nullLiteral))), database),
                args("case-absent-else", select(table("foo_table"), expression(caseWhen(Collections.singleton(datetime), null))), database),
                args("coalesce-datetime-timestamp", select(table("foo_table"), expression(coalesce(datetime, timestamp))), database),
                args("coalesce-timestamp-datetime", select(table("foo_table"), expression(coalesce(timestamp, datetime))), database),
                args("coalesce-null-datetime", select(table("foo_table"), expression(coalesce(nullLiteral, datetime))), database));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource({"nonDatetimeProjectionArguments", "nonMySQLFunctionArguments", "nonDatetimeResultExpressionArguments"})
    void assertIsDatetimeWithoutDatetime(final String name, final SelectStatementContext context, final ShardingSphereDatabase database, final int columnIndex) {
        assertFalse(MySQLDatetimeProjectionResolver.isDatetime(context, database, columnIndex));
    }
    
    private static Stream<Arguments> nonDatetimeProjectionArguments() {
        ShardingSphereDatabase columnsDatabase = createColumnsDatabase();
        ShardingSphereDatabase mysqlDatabase = createColumnsDatabase();
        setStorageTypes(mysqlDatabase, "MySQL");
        ShardingSphereDatabase mixedDatabase = createColumnsDatabase();
        setStorageTypes(mixedDatabase, "MySQL", "PostgreSQL");
        ShardingSphereDatabase foreignDatabase = mock(ShardingSphereDatabase.class);
        when(foreignDatabase.getName()).thenReturn("foo_db");
        ColumnProjectionSegment foreignColumn = column("foo_datetime");
        foreignColumn.getColumn().setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("bar_db"), null),
                new IdentifierValue("foo_table"), new IdentifierValue("foo_datetime"), TableSourceType.PHYSICAL_TABLE));
        FunctionSegment qualifiedFunction = function("CURRENT_TIMESTAMP");
        qualifiedFunction.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_owner")));
        ShardingSphereDatabase missingSchemaDatabase = mock(ShardingSphereDatabase.class);
        when(missingSchemaDatabase.getDefaultSchemaName()).thenReturn("foo_unknown");
        return Stream.of(
                args("timestamp", select(table("foo_table"), column("foo_timestamp")), columnsDatabase),
                args("integer", select(table("foo_table"), column("foo_integer")), columnsDatabase),
                args("date", select(table("foo_table"), column("foo_date")), columnsDatabase),
                args("time", select(table("foo_table"), column("foo_time")), columnsDatabase),
                args("unknown-column", select(table("foo_table"), column("foo_unknown")), columnsDatabase),
                args("unknown-table", select(table("foo_unknown"), column("foo_datetime")), columnsDatabase),
                args("unknown-schema", select(table("foo_table"), column("foo_datetime")), missingSchemaDatabase),
                args("unbound-no-from", select(null, column("foo_datetime")), mock(ShardingSphereDatabase.class)),
                args("wrong-owner", select(table("foo_table"), qualifiedColumn("foo_unknown", "foo_datetime")), mock(ShardingSphereDatabase.class)),
                args("hint-derived-timestamp-alias-collision", select(derived(select(table("foo_table"), aliasedColumn("foo_timestamp", "foo_datetime")), "foo_table"),
                        qualifiedColumn("foo_table", "foo_datetime")), columnsDatabase),
                args("bound-derived-unknown-retains-timestamp", select(derived(select(table("foo_table"), aliasedExpression(function("IFNULL", column("foo_timestamp").getColumn(),
                        column("foo_datetime").getColumn()), "foo_x")), "foo_derived"), boundColumn("foo_x", "foo_timestamp")), columnsDatabase),
                args("known-derived-timestamp-overrides-bound-datetime", select(derived(select(table("foo_table"), aliasedColumn("foo_timestamp", "foo_x")), "foo_derived"),
                        boundColumn("foo_x", "foo_datetime")), columnsDatabase),
                args("unbound-derived-unknown-does-not-guess", select(derived(select(table("foo_table"), aliasedExpression(function("IFNULL", column("foo_datetime").getColumn(),
                        column("foo_timestamp").getColumn()), "foo_datetime")), "foo_table"), qualifiedColumn("foo_table", "foo_datetime")), columnsDatabase),
                args("maximum-derived-unknown-does-not-retain", select(derived(select(table("foo_table"), aliasedExpression(function("IFNULL", column("foo_datetime").getColumn(),
                        column("foo_timestamp").getColumn()), "foo_x")), "foo_derived"), aggregation(AggregationType.MAX, boundColumn("foo_x", "foo_datetime").getColumn())), columnsDatabase),
                args("foreign-bound-database", select(table("foo_table"), foreignColumn), foreignDatabase),
                args("unknown-derived-output", select(derived(select(table("foo_table"), column("foo_datetime")), "foo_alias"), qualifiedColumn("foo_alias", "foo_unknown")),
                        mock(ShardingSphereDatabase.class)),
                args("unexpanded-derived-shorthand", select(derived(select(table("foo_table"), new ShorthandProjectionSegment(0, 0)), "foo_alias"), qualifiedColumn("foo_alias", "foo_datetime")),
                        mock(ShardingSphereDatabase.class)),
                args("wrong-derived-owner", select(derived(select(table("foo_table"), column("foo_datetime")), "foo_alias"), qualifiedColumn("foo_unknown", "foo_datetime")),
                        mock(ShardingSphereDatabase.class)),
                args("scalar-unexpanded-output", select(null, new SubqueryProjectionSegment(subquery(select(table("foo_table"), new ShorthandProjectionSegment(0, 0)), 0), "foo_scalar")),
                        mock(ShardingSphereDatabase.class)),
                args("scalar-partially-expanded-output", select(null, new SubqueryProjectionSegment(subquery(select(table("foo_table"), new ShorthandProjectionSegment(0, 0),
                        column("foo_datetime")), 0), "foo_scalar")), columnsDatabase),
                args("timestamp-union", combine(select(table("foo_table"), column("foo_timestamp")), select(table("foo_table"), column("foo_timestamp"))), mysqlDatabase),
                args("date-union", combine(select(table("foo_table"), column("foo_date")), select(table("foo_table"), column("foo_date"))), mysqlDatabase),
                args("null-union", combine(select(null, expression(new LiteralExpressionSegment(0, 0, null))), select(null, expression(new LiteralExpressionSegment(0, 0, null)))),
                        createFunctionDatabase("MySQL")),
                args("unknown-first-union", combine(select(table("foo_table"), column("foo_unknown")), select(table("foo_table"), column("foo_datetime"))), mysqlDatabase),
                args("timestamp-unknown-second-union", combine(select(table("foo_table"), column("foo_timestamp")), select(table("foo_table"), column("foo_unknown"))), columnsDatabase),
                args("timestamp-unexpanded-second-union", combine(select(table("foo_table"), column("foo_timestamp")), select(table("foo_table"), new ShorthandProjectionSegment(0, 0))),
                        columnsDatabase),
                args("mixed-storage-timestamp-first", combine(select(table("foo_table"), column("foo_timestamp")), select(table("foo_table"), column("foo_datetime"))), mixedDatabase),
                args("mixed-storage-null-first", combine(select(null, expression(new LiteralExpressionSegment(0, 0, null))), select(table("foo_table"), column("foo_datetime"))), mixedDatabase),
                args("minimum-timestamp", select(table("foo_table"), aggregation(AggregationType.MIN, column("foo_timestamp").getColumn())), columnsDatabase),
                args("maximum-timestamp", select(table("foo_table"), aggregation(AggregationType.MAX, column("foo_timestamp").getColumn())), columnsDatabase),
                args("maximum-union-unknown-is-not-confirmed", select(derived(combine(select(table("foo_table"), aliasedColumn("foo_datetime", "foo_x")),
                        select(table("foo_table"), column("foo_unknown"))), "foo_union"), aggregation(AggregationType.MAX, qualifiedColumn("foo_union", "foo_x").getColumn())), columnsDatabase),
                args("maximum-union-mixed-storage-is-not-confirmed", select(derived(combine(select(table("foo_table"), aliasedColumn("foo_datetime", "foo_x")),
                        select(table("foo_table"), column("foo_timestamp"))), "foo_union"), aggregation(AggregationType.MAX, qualifiedColumn("foo_union", "foo_x").getColumn())), mixedDatabase),
                args("non-preserving-aggregation", select(table("foo_table"), aggregation(AggregationType.SUM, column("foo_datetime").getColumn())), mock(ShardingSphereDatabase.class)),
                args("incomplete-aggregation", select(table("foo_table"), new AggregationProjectionSegment(0, 0, AggregationType.MAX, "MAX(*)")), mock(ShardingSphereDatabase.class)),
                args("unknown-function", select(null, expression(function("foo_function"))), mock(ShardingSphereDatabase.class)),
                args("qualified-function", select(null, expression(qualifiedFunction)), mock(ShardingSphereDatabase.class)),
                args("literal-number", select(null, expression(new LiteralExpressionSegment(0, 0, 1))), mock(ShardingSphereDatabase.class)),
                args("text-only-expression", select(null, new ExpressionProjectionSegment(0, 0, "foo_expression")), mock(ShardingSphereDatabase.class)),
                args("parameter-projection", select(null, new ParameterMarkerExpressionSegment(0, 0, 0)), mock(ShardingSphereDatabase.class)),
                args("function-table-source", select(new FunctionTableSegment(0, 0, function("foo_function")), column("foo_datetime")), mock(ShardingSphereDatabase.class)),
                args("ambiguous-derived-join-left", select(join(derived(select(table("foo_table"), column("foo_datetime")), "foo_alias"), table("bar_table")),
                        boundColumn("foo_alias", "foo_datetime")), mock(ShardingSphereDatabase.class)),
                args("ambiguous-derived-join-right", select(join(table("bar_table"), derived(select(table("foo_table"), column("foo_datetime")), "foo_alias")),
                        boundColumn("foo_alias", "foo_datetime")), mock(ShardingSphereDatabase.class)),
                args("unbound-join-source", select(join(table("foo_table"), table("bar_table")), column("foo_datetime")), mock(ShardingSphereDatabase.class)));
    }
    
    private static Stream<Arguments> nonMySQLFunctionArguments() {
        return Stream.of(
                args("sqlserver-current-timestamp", select(null, expression(function("CURRENT_TIMESTAMP"))), createFunctionDatabase("SQLServer")),
                args("h2-current-timestamp", select(null, expression(function("CURRENT_TIMESTAMP"))), createFunctionDatabase("H2")),
                args("mariadb-current-timestamp", select(null, expression(function("CURRENT_TIMESTAMP"))), createFunctionDatabase("MariaDB")),
                args("mixed-storage-current-timestamp", select(null, expression(function("CURRENT_TIMESTAMP"))), createFunctionDatabase("MySQL", "PostgreSQL")),
                args("no-storage-current-timestamp", select(null, expression(function("CURRENT_TIMESTAMP"))), createFunctionDatabase()));
    }
    
    private static Stream<Arguments> nonDatetimeResultExpressionArguments() {
        ShardingSphereDatabase database = createColumnsDatabase();
        ShardingSphereDatabase mixedDatabase = createColumnsDatabase();
        setStorageTypes(mixedDatabase, "MySQL", "PostgreSQL");
        ExpressionSegment datetime = column("foo_datetime").getColumn();
        ExpressionSegment timestamp = column("foo_timestamp").getColumn();
        ExpressionSegment unknown = column("foo_unknown").getColumn();
        ExpressionSegment integer = column("foo_integer").getColumn();
        CaseWhenExpression datetimeCondition = new CaseWhenExpression(0, 0, datetime, Collections.singleton(datetime), Collections.singleton(timestamp), timestamp);
        CaseWhenExpression unknownThen = caseWhen(Arrays.asList(datetime, unknown), datetime);
        FunctionSegment qualifiedCoalesce = coalesce(datetime, timestamp);
        qualifiedCoalesce.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_owner")));
        ShorthandProjectionSegment unknownShorthand = new ShorthandProjectionSegment(0, 0);
        unknownShorthand.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_q")));
        SelectStatement unknownRight = select(derived(select(table("foo_table"), aliasedColumn("foo_datetime", "foo_x")), "foo_q"), unknownShorthand);
        SelectStatement unknownUnion = combine(select(table("foo_table"), aliasedColumn("foo_datetime", "foo_x")), unknownRight);
        ExpressionSegment derived = qualifiedColumn("foo_union", "foo_x").getColumn();
        return Stream.of(
                args("case-timestamp", select(table("foo_table"), expression(caseWhen(Collections.singleton(timestamp), timestamp))), database),
                args("case-ignores-conditions", select(table("foo_table"), expression(datetimeCondition)), database),
                args("case-unknown-later-then", select(table("foo_table"), expression(unknownThen)), database),
                args("case-unknown-else", select(table("foo_table"), expression(caseWhen(Collections.singleton(datetime), unknown))), database),
                args("case-non-temporal-result", select(table("foo_table"), expression(caseWhen(Collections.singleton(datetime), integer))), database),
                args("case-mixed-storage", select(table("foo_table"), expression(caseWhen(Collections.singleton(datetime), datetime))), mixedDatabase),
                args("case-derived-unknown-is-not-confirmed", select(derived(unknownUnion, "foo_union"), expression(caseWhen(Collections.singleton(derived), derived))), database),
                args("coalesce-timestamp", select(table("foo_table"), expression(coalesce(timestamp, timestamp))), database),
                args("coalesce-unknown-result", select(table("foo_table"), expression(coalesce(datetime, unknown))), database),
                args("coalesce-non-temporal-result", select(table("foo_table"), expression(coalesce(datetime, integer))), database),
                args("coalesce-mixed-storage", select(table("foo_table"), expression(coalesce(datetime, timestamp))), mixedDatabase),
                args("coalesce-qualified-function", select(table("foo_table"), expression(qualifiedCoalesce)), mock(ShardingSphereDatabase.class)));
    }
    
    private static Arguments args(final String name, final SelectStatement selectStatement, final ShardingSphereDatabase database) {
        return Arguments.of(name, context(selectStatement), database, 1);
    }
    
    private static SelectStatementContext context(final SelectStatement selectStatement) {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(selectStatement);
        Collection<Projection> projections = new ArrayList<>(selectStatement.getProjections().getProjections().size());
        ProjectionEngine engine = new ProjectionEngine(MYSQL);
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            engine.createProjection(each).ifPresent(projections::add);
        }
        when(result.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, projections));
        Collection<SubquerySegment> subqueries = SubqueryExtractor.extractSubquerySegments(selectStatement, false);
        if (!subqueries.isEmpty()) {
            Map<Integer, SelectStatementContext> subqueryContexts = new HashMap<>(subqueries.size(), 1F);
            for (SubquerySegment each : subqueries) {
                subqueryContexts.put(each.getStartIndex(), context(each.getSelect()));
            }
            when(result.getSubqueryContexts()).thenReturn(subqueryContexts);
        }
        return result;
    }
    
    /**
     * Create SELECT input.
     *
     * @param from source, or {@code null} without FROM
     * @param projections projections
     * @return SELECT input
     */
    private static SelectStatement select(final TableSegment from, final ProjectionSegment... projections) {
        ProjectionsSegment projectionSegments = new ProjectionsSegment(0, 0);
        projectionSegments.getProjections().addAll(Arrays.asList(projections));
        return SelectStatement.builder().databaseType(MYSQL).from(from).projections(projectionSegments).build();
    }
    
    private static SelectStatement combine(final SelectStatement left, final SelectStatement right) {
        return SelectStatement.builder().databaseType(MYSQL).projections(left.getProjections())
                .combine(new CombineSegment(0, 0, subquery(left, 0), CombineType.UNION_ALL, subquery(right, 1))).build();
    }
    
    private static SubquerySegment subquery(final SelectStatement selectStatement, final int startIndex) {
        SubquerySegment result = new SubquerySegment(startIndex, startIndex, "foo_subquery");
        result.setSelect(selectStatement);
        return result;
    }
    
    private static SimpleTableSegment table(final String name) {
        return new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(name)));
    }
    
    private static SimpleTableSegment aliasedTable(final String name, final String alias) {
        SimpleTableSegment result = table(name);
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private static SubqueryTableSegment derived(final SelectStatement selectStatement, final String alias) {
        SubqueryTableSegment result = new SubqueryTableSegment(0, 0, subquery(selectStatement, 0));
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private static JoinTableSegment join(final TableSegment left, final TableSegment right) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(left);
        result.setRight(right);
        return result;
    }
    
    private static ColumnProjectionSegment column(final String name) {
        return new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue(name)));
    }
    
    private static ColumnProjectionSegment boundColumn(final String name, final String originalColumn) {
        ColumnProjectionSegment result = column(name);
        result.getColumn().setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(null, null), new IdentifierValue("foo_table"),
                new IdentifierValue(originalColumn), TableSourceType.PHYSICAL_TABLE));
        return result;
    }
    
    private static ColumnProjectionSegment qualifiedColumn(final String owner, final String name) {
        ColumnProjectionSegment result = column(name);
        result.getColumn().setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        return result;
    }
    
    private static ColumnProjectionSegment aliasedColumn(final String name, final String alias) {
        ColumnProjectionSegment result = column(name);
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private static ColumnProjectionSegment aliasedQualifiedColumn(final String owner, final String name, final String alias) {
        ColumnProjectionSegment result = qualifiedColumn(owner, name);
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private static ExpressionProjectionSegment expression(final ExpressionSegment expr) {
        return new ExpressionProjectionSegment(0, 0, "foo_expression", expr);
    }
    
    private static ExpressionProjectionSegment aliasedExpression(final ExpressionSegment expr, final String alias) {
        ExpressionProjectionSegment result = expression(expr);
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private static AggregationProjectionSegment aggregation(final AggregationType type, final ExpressionSegment operand) {
        AggregationProjectionSegment result = new AggregationProjectionSegment(0, 0, type, "foo_aggregation");
        result.getParameters().add(operand);
        return result;
    }
    
    private static FunctionSegment function(final String name, final ExpressionSegment... params) {
        FunctionSegment result = new FunctionSegment(0, 0, name, "foo_function");
        result.getParameters().addAll(Arrays.asList(params));
        return result;
    }
    
    private static CaseWhenExpression caseWhen(final Collection<ExpressionSegment> thenExprs, @Nullable final ExpressionSegment elseExpr) {
        Collection<ExpressionSegment> conditions = new ArrayList<>(thenExprs.size());
        for (int index = 0; index < thenExprs.size(); index++) {
            conditions.add(new LiteralExpressionSegment(0, 0, 1));
        }
        return new CaseWhenExpression(0, 0, null, conditions, thenExprs, elseExpr);
    }
    
    private static FunctionSegment coalesce(final ExpressionSegment... params) {
        FunctionSegment result = function("COALESCE");
        result.getParameters().addAll(Arrays.asList(params));
        return result;
    }
    
    private static ShardingSphereDatabase createColumnsDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", MYSQL, Arrays.asList(createTable("foo_table"), createTable("bar_table")), Collections.emptyList());
        when(result.getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getSchema("foo_db")).thenReturn(schema);
        return result;
    }
    
    private static ShardingSphereTable createTable(final String name) {
        Collection<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("foo_datetime", Types.TIMESTAMP, false, false, false, true, false, true, true),
                new ShardingSphereColumn("foo_timestamp", Types.TIMESTAMP, false, false, false, true, false, true),
                new ShardingSphereColumn("foo_date", Types.DATE, false, false, false, true, false, true),
                new ShardingSphereColumn("foo_time", Types.TIME, false, false, false, true, false, true),
                new ShardingSphereColumn("foo_integer", Types.INTEGER, false, false, false, true, false, true));
        return new ShardingSphereTable(name, columns, Collections.emptyList(), Collections.emptyList());
    }
    
    private static ShardingSphereDatabase createFunctionDatabase(final String... storageTypes) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        setStorageTypes(result, storageTypes);
        return result;
    }
    
    private static void setStorageTypes(final ShardingSphereDatabase database, final String... storageTypes) {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(storageTypes.length, 1F);
        for (int index = 0; index < storageTypes.length; index++) {
            StorageUnit storageUnit = mock(StorageUnit.class);
            DatabaseType databaseType = mock(DatabaseType.class);
            when(databaseType.getType()).thenReturn(storageTypes[index]);
            when(storageUnit.getStorageType()).thenReturn(databaseType);
            storageUnits.put(Integer.toString(index), storageUnit);
        }
        when(database.getResourceMetaData()).thenReturn(new ResourceMetaData(Collections.emptyMap(), storageUnits));
    }
}
