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

package org.apache.shardingsphere.sharding.merge.dql;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.IteratorStreamMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;

class ShardingDQLResultMergerTest {
    
    private final DatabaseType mysqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DatabaseType oracleDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    private final DatabaseType sqlserverDatabaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    @Test
    void assertBuildIteratorStreamMergedResult() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), isA(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(resultMerger.merge(Collections.singletonList(createQueryResult()), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), isA(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), isA(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(oracleDatabaseType);
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(oracleDatabaseType));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1L));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(binaryOperationExpression.getText()).thenReturn("");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(SelectStatement.class);
        when(subSelectStatement.getDatabaseType()).thenReturn(oracleDatabaseType);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(sqlserverDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(sqlserverDatabaseType));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createSQLServerDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), isA(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResult() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), isA(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), isA(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(oracleDatabaseType);
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1L));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(binaryOperationExpression.getText()).thenReturn("");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(SelectStatement.class);
        when(subSelectStatement.getDatabaseType()).thenReturn(oracleDatabaseType);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(oracleDatabaseType));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(sqlserverDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(sqlserverDatabaseType));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1L, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createSQLServerDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), isA(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResult() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(oracleDatabaseType);
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1L));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(binaryOperationExpression.getText()).thenReturn("");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(SelectStatement.class);
        when(subSelectStatement.getDatabaseType()).thenReturn(oracleDatabaseType);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(oracleDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(sqlserverDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(sqlserverDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1L, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createSQLServerDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResult() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(oracleDatabaseType);
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1L));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(binaryOperationExpression.getText()).thenReturn("");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(SelectStatement.class);
        when(subSelectStatement.getDatabaseType()).thenReturn(oracleDatabaseType);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(oracleDatabaseType));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(sqlserverDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(sqlserverDatabaseType));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1L, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createSQLServerDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), isA(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnly() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), isA(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithMySQLLimit() throws SQLException {
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(mysqlDatabaseType));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS)), "foo_db", Collections.emptyList());
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), isA(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(oracleDatabaseType);
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1L));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(SelectStatement.class);
        when(subSelectStatement.getDatabaseType()).thenReturn(oracleDatabaseType);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(oracleDatabaseType));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(sqlserverDatabaseType);
        SelectStatement selectStatement = buildSelectStatement(new SelectStatement(sqlserverDatabaseType));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1L, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, createShardingSphereMetaData(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS)), "foo_db", Collections.emptyList());
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createSQLServerDatabase(), mock(ConnectionContext.class));
        assertThat(actual, isA(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), isA(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertMergeWithNestedAggregationInExpressionWithoutGroupBy() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(mysqlDatabaseType);
        when(selectStatementContext.getGroupByContext().getItems()).thenReturn(Collections.emptyList());
        doNothing().when(selectStatementContext).setIndexes(anyMap());
        ShardingSphereDatabase database = createDatabase();
        AggregationProjectionSegment sumSegment = new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(amount)");
        AggregationProjection nestedSum = new AggregationProjection(AggregationType.SUM, sumSegment, new IdentifierValue("__sharding_expr_agg_1"), mysqlDatabaseType);
        nestedSum.setIndex(1);
        when(selectStatementContext.getProjectionsContext().getExpandAggregationProjections()).thenReturn(Collections.singletonList(nestedSum));
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.emptyList());
        QueryResult queryResult1 = mockQueryResultSingleColumn(new BigDecimal("0"));
        QueryResult queryResult2 = mockQueryResultSingleColumn(new BigDecimal("100"));
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(mysqlDatabaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2), selectStatementContext, database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal("100")));
    }
    
    private QueryResult mockQueryResultSingleColumn(final Object value) throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("__sharding_expr_agg_1");
        when(result.getValue(1, Object.class)).thenReturn(value);
        return result;
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        when(database.getName()).thenReturn("foo_db");
        return new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
    }
    
    private List<QueryResult> createQueryResults() throws SQLException {
        List<QueryResult> result = new LinkedList<>();
        QueryResult queryResult = createQueryResult();
        result.add(queryResult);
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        result.add(mock(QueryResult.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("count(*)");
        when(result.getValue(1, Object.class)).thenReturn(0);
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", Collections.singleton(createTable()), Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", mysqlDatabaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), Collections.singleton(schema));
    }
    
    private ShardingSphereDatabase createSQLServerDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("dbo", Collections.singleton(createTable()), Collections.emptyList());
        return new ShardingSphereDatabase(
                "foo_db", TypedSPILoader.getService(DatabaseType.class, "SQLServer"), mock(ResourceMetaData.class), mock(RuleMetaData.class), Collections.singleton(schema));
    }
    
    private ShardingSphereTable createTable() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("col1", 0, false, false, false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("col2", 0, false, false, false, true, false, false);
        ShardingSphereColumn column3 = new ShardingSphereColumn("col3", 0, false, false, false, true, false, false);
        return new ShardingSphereTable("tbl", Arrays.asList(column1, column2, column3), Collections.emptyList(), Collections.emptyList());
    }
    
    private SelectStatement buildSelectStatement(final SelectStatement result) {
        result.setFrom(new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl"))));
        result.setProjections(new ProjectionsSegment(0, 0));
        return result;
    }
}
