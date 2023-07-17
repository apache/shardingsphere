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

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.RowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingDQLResultMergerTest {
    
    @Test
    void assertBuildIteratorStreamMergedResult() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        SelectStatement selectStatement = buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(Collections.singletonList(createQueryResult()), selectStatementContext, createDatabase(), mock(ConnectionContext.class)),
                instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "Oracle"));
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        OracleSelectStatement selectStatement = (OracleSelectStatement) buildSelectStatement(new OracleSelectStatement());
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(MySQLSelectStatement.class);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        TopProjectionSegment topProjectionSegment = mock(TopProjectionSegment.class);
        when(topProjectionSegment.getAlias()).thenReturn("row_id");
        when(subProjectionsSegment.getProjections()).thenReturn(Collections.singletonList(topProjectionSegment));
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), null, selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildIteratorStreamMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        SQLServerSelectStatement selectStatement = (SQLServerSelectStatement) buildSelectStatement(new SQLServerSelectStatement());
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResult() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "Oracle"));
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(MySQLSelectStatement.class);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        TopProjectionSegment topProjectionSegment = mock(TopProjectionSegment.class);
        when(topProjectionSegment.getAlias()).thenReturn("row_id");
        when(subProjectionsSegment.getProjections()).thenReturn(Collections.singletonList(topProjectionSegment));
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        OracleSelectStatement selectStatement = (OracleSelectStatement) buildSelectStatement(new OracleSelectStatement());
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), null,
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildOrderByStreamMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        SQLServerSelectStatement selectStatement = (SQLServerSelectStatement) buildSelectStatement(new SQLServerSelectStatement());
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResult() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "Oracle"));
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(MySQLSelectStatement.class);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        TopProjectionSegment topProjectionSegment = mock(TopProjectionSegment.class);
        when(topProjectionSegment.getAlias()).thenReturn("row_id");
        when(subProjectionsSegment.getProjections()).thenReturn(Collections.singletonList(topProjectionSegment));
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        OracleSelectStatement selectStatement = (OracleSelectStatement) buildSelectStatement(new OracleSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), null,
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByStreamMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        SQLServerSelectStatement selectStatement = (SQLServerSelectStatement) buildSelectStatement(new SQLServerSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResult() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithMySQLLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "Oracle"));
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(MySQLSelectStatement.class);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        TopProjectionSegment topProjectionSegment = mock(TopProjectionSegment.class);
        when(topProjectionSegment.getAlias()).thenReturn("row_id");
        when(subProjectionsSegment.getProjections()).thenReturn(Collections.singletonList(topProjectionSegment));
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        OracleSelectStatement selectStatement = (OracleSelectStatement) buildSelectStatement(new OracleSelectStatement());
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), null, selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        SQLServerSelectStatement selectStatement = (SQLServerSelectStatement) buildSelectStatement(new SQLServerSelectStatement());
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnly() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class)), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithMySQLLimit() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        MySQLSelectStatement selectStatement = (MySQLSelectStatement) buildSelectStatement(new MySQLSelectStatement());
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithOracleLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "Oracle"));
        final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        when(binaryOperationExpression.getLeft()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("row_id")));
        when(binaryOperationExpression.getRight()).thenReturn(new LiteralExpressionSegment(0, 0, 1));
        when(binaryOperationExpression.getOperator()).thenReturn(">=");
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        SubqueryTableSegment subqueryTableSegment = mock(SubqueryTableSegment.class);
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        SelectStatement subSelectStatement = mock(MySQLSelectStatement.class);
        ProjectionsSegment subProjectionsSegment = mock(ProjectionsSegment.class);
        TopProjectionSegment topProjectionSegment = mock(TopProjectionSegment.class);
        when(topProjectionSegment.getAlias()).thenReturn("row_id");
        when(subProjectionsSegment.getProjections()).thenReturn(Collections.singletonList(topProjectionSegment));
        when(subSelectStatement.getProjections()).thenReturn(subProjectionsSegment);
        when(subquerySegment.getSelect()).thenReturn(subSelectStatement);
        when(subqueryTableSegment.getSubquery()).thenReturn(subquerySegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        OracleSelectStatement selectStatement = (OracleSelectStatement) buildSelectStatement(new OracleSelectStatement());
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(subqueryTableSegment);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), null,
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithSQLServerLimit() throws SQLException {
        final ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        SQLServerSelectStatement selectStatement = (SQLServerSelectStatement) buildSelectStatement(new SQLServerSelectStatement());
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setLimit(new LimitSegment(0, 0, new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        MergedResult actual = resultMerger.merge(createQueryResults(), selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
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
        ShardingSphereColumn column1 = new ShardingSphereColumn("col1", 0, false, false, false, true, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("col2", 0, false, false, false, true, false);
        ShardingSphereColumn column3 = new ShardingSphereColumn("col3", 0, false, false, false, true, false);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Arrays.asList(column1, column2, column3), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap());
        return new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "MySQL"), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema));
    }
    
    private SelectStatement buildSelectStatement(final SelectStatement result) {
        result.setFrom(new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl"))));
        result.setProjections(new ProjectionsSegment(0, 0));
        return result;
    }
}
