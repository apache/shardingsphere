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

package org.apache.shardingsphere.infra.binder.engine.statement.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HierarchicalQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBind() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        ColumnProjectionSegment orderIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        ColumnProjectionSegment userIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        ColumnProjectionSegment statusProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        projections.getProjections().add(orderIdProjection);
        projections.getProjections().add(userIdProjection);
        projections.getProjections().add(statusProjection);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projections).from(simpleTableSegment).where(createWhereSegment()).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        assertThat(actual, not(selectStatement));
        assertTrue(actual.getFrom().isPresent());
        assertThat(actual.getFrom().get(), not(simpleTableSegment));
        assertThat(actual.getFrom().get(), isA(SimpleTableSegment.class));
        assertThat(((SimpleTableSegment) actual.getFrom().get()).getTableName(), not(simpleTableSegment.getTableName()));
        assertThat(actual.getProjections(), not(selectStatement.getProjections()));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual.getProjections().getProjections());
        assertThat(actualProjections, not(selectStatement.getProjections()));
        assertThat(actualProjections.get(0), not(orderIdProjection));
        assertThat(actualProjections.get(0), isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(0)).getColumn(), not(orderIdProjection.getColumn()));
        assertThat(actualProjections.get(1), not(userIdProjection));
        assertThat(actualProjections.get(1), isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(1)).getColumn(), not(userIdProjection.getColumn()));
        assertThat(actualProjections.get(2), not(statusProjection));
        assertThat(actualProjections.get(2), isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(2)).getColumn(), not(statusProjection.getColumn()));
        assertTrue(actual.getWhere().isPresent());
        assertThat(actual.getWhere().get(), not(selectStatement.getWhere()));
        assertThat(actual.getWhere().get(), isA(WhereSegment.class));
        assertTrue(selectStatement.getWhere().isPresent());
        assertThat(actual.getWhere().get().getExpr(), not(selectStatement.getWhere().get().getExpr()));
        assertThat(actual.getWhere().get().getExpr(), isA(BinaryOperationExpression.class));
        assertThat(((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft(), isA(FunctionSegment.class));
        assertThat(((FunctionSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getParameters().iterator().next(), isA(ColumnSegment.class));
        assertThat(((ColumnSegment) ((FunctionSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getParameters().iterator().next())
                .getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    @Test
    void assertBindHierarchicalQuery() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        HierarchicalQuerySegment hierarchicalQuerySegment = new HierarchicalQuerySegment(0, 0);
        hierarchicalQuerySegment.setNoCycle(true);
        hierarchicalQuerySegment.setStartWith(new BinaryOperationExpression(
                0, 0, new ColumnSegment(0, 0, new IdentifierValue("user_id")), new LiteralExpressionSegment(0, 0, 1), "=", "user_id = 1"));
        hierarchicalQuerySegment.setConnectBy(new BinaryOperationExpression(
                0, 0, new ColumnSegment(0, 0, new IdentifierValue("order_id")), new ColumnSegment(0, 0, new IdentifierValue("user_id")), "=", "order_id = user_id"));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).hierarchicalQuery(hierarchicalQuerySegment).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        assertTrue(actual.getHierarchicalQuery().isPresent());
        assertThat(actual.getHierarchicalQuery().get(), not(hierarchicalQuerySegment));
        assertTrue(actual.getHierarchicalQuery().get().isNoCycle());
        BinaryOperationExpression actualStartWith = (BinaryOperationExpression) actual.getHierarchicalQuery().get().getStartWith();
        assertThat(actualStartWith, not(hierarchicalQuerySegment.getStartWith()));
        ColumnSegment actualStartWithColumn = (ColumnSegment) actualStartWith.getLeft();
        assertThat(actualStartWithColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actualStartWithColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        BinaryOperationExpression actualConnectBy = (BinaryOperationExpression) actual.getHierarchicalQuery().get().getConnectBy();
        assertThat(actualConnectBy, not(hierarchicalQuerySegment.getConnectBy()));
        ColumnSegment actualConnectByLeftColumn = (ColumnSegment) actualConnectBy.getLeft();
        assertThat(actualConnectByLeftColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(actualConnectByLeftColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        ColumnSegment actualConnectByRightColumn = (ColumnSegment) actualConnectBy.getRight();
        assertThat(actualConnectByRightColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actualConnectByRightColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    @Test
    void assertBindWindow() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_name"))));
        ColumnSegment partitionColumnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        FunctionSegment lengthFunction = new FunctionSegment(0, 0, "LENGTH", "LENGTH(user_name)");
        lengthFunction.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("user_name")));
        ExpressionOrderByItemSegment expressionOrderByItemSegment =
                new ExpressionOrderByItemSegment(0, 0, "LENGTH(user_name)", OrderDirection.ASC, null, lengthFunction);
        ColumnOrderByItemSegment columnOrderByItemSegment =
                new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")), OrderDirection.ASC, null);
        WindowItemSegment windowItemSegment = new WindowItemSegment(0, 0);
        windowItemSegment.setWindowName(new IdentifierValue("w"));
        windowItemSegment.setPartitionListSegments(Collections.singleton(partitionColumnSegment));
        windowItemSegment.setOrderBySegment(new OrderBySegment(0, 0, Arrays.asList(expressionOrderByItemSegment, columnOrderByItemSegment)));
        WindowSegment windowSegment = new WindowSegment(0, 0);
        windowSegment.getItemSegments().add(windowItemSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).window(windowSegment).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, new SQLStatementBinderContext(mockMetaData(),
                "foo_db", new HintValueContext(), selectStatement));
        assertTrue(actual.getWindow().isPresent());
        assertThat(actual.getWindow().get(), not(windowSegment));
        WindowItemSegment actualWindowItem = actual.getWindow().get().getItemSegments().iterator().next();
        assertThat(actualWindowItem, not(windowItemSegment));
        ColumnSegment actualPartitionColumn = (ColumnSegment) actualWindowItem.getPartitionListSegments().iterator().next();
        assertThat(actualPartitionColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actualPartitionColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        ExpressionOrderByItemSegment actualExpressionOrderByItem = (ExpressionOrderByItemSegment) actualWindowItem.getOrderBySegment().getOrderByItems().iterator().next();
        FunctionSegment actualLengthFunction = (FunctionSegment) actualExpressionOrderByItem.getExpr();
        ColumnSegment actualLengthParameter = (ColumnSegment) actualLengthFunction.getParameters().iterator().next();
        assertThat(actualLengthParameter.getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        ColumnOrderByItemSegment actualColumnOrderByItem = (ColumnOrderByItemSegment) new ArrayList<>(actualWindowItem.getOrderBySegment().getOrderByItems()).get(1);
        assertThat(actualColumnOrderByItem.getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actualColumnOrderByItem.getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
    }
    
    @Test
    void assertBindWithLikeSubquery() {
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("email"))));
        BinaryOperationExpression subqueryWhereExpression =
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("user_id")), new LiteralExpressionSegment(0, 0, 1), "=", "user_id = 1");
        SelectStatement subquerySelect = SelectStatement.builder().databaseType(databaseType).projections(subqueryProjections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).where(new WhereSegment(0, 0, subqueryWhereExpression)).build();
        ListExpression likeExpression = new ListExpression(0, 0);
        likeExpression.getItems().add(new SubqueryExpressionSegment(new SubquerySegment(0, 0, subquerySelect, "")));
        BinaryOperationExpression whereExpression =
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("email")), likeExpression, "LIKE", "email LIKE (SELECT email FROM t_user WHERE user_id = 1)");
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).where(new WhereSegment(0, 0, whereExpression)).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        BinaryOperationExpression actualWhereExpression = (BinaryOperationExpression) actual.getWhere().get().getExpr();
        ListExpression actualLikeExpression = (ListExpression) actualWhereExpression.getRight();
        SubqueryExpressionSegment actualSubqueryExpression = (SubqueryExpressionSegment) actualLikeExpression.getItems().iterator().next();
        SelectStatement actualSubquerySelect = actualSubqueryExpression.getSubquery().getSelect();
        ColumnSegment actualSubqueryProjection = ((ColumnProjectionSegment) actualSubquerySelect.getProjections().getProjections().iterator().next()).getColumn();
        BinaryOperationExpression actualSubqueryWhere = (BinaryOperationExpression) actualSubquerySelect.getWhere().get().getExpr();
        assertThat(actualSubqueryProjection.getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        assertThat(((ColumnSegment) actualSubqueryWhere.getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
    }
    
    @Test
    void assertBindWithSameNameAsPhysicalTable() {
        ProjectionsSegment withProjections = new ProjectionsSegment(0, 0);
        withProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        ExpressionProjectionSegment userNameProjection = new ExpressionProjectionSegment(0, 0, "UPPER(user_name)",
                new FunctionSegment(0, 0, "UPPER", "UPPER(user_name)"));
        ((FunctionSegment) userNameProjection.getExpr()).getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("user_name")));
        userNameProjection.setAlias(new AliasSegment(0, 0, new IdentifierValue("user_name")));
        withProjections.getProjections().add(userNameProjection);
        withProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("email"))));
        SelectStatement withSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(withProjections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
        CommonTableExpressionSegment commonTableExpressionSegment = new CommonTableExpressionSegment(
                0, 0, new AliasSegment(0, 0, new IdentifierValue("t_user")), new SubquerySegment(0, 0, withSelectStatement, ""));
        WithSegment withSegment = new WithSegment(0, 0, new LinkedList<>(Collections.singleton(commonTableExpressionSegment)), false);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ShorthandProjectionSegment(0, 0));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).with(withSegment).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        ProjectionSegment actualProjection = actual.getProjections().getProjections().iterator().next();
        assertThat(actualProjection, isA(ShorthandProjectionSegment.class));
        assertThat(((ShorthandProjectionSegment) actualProjection).getActualProjectionSegments().size(), is(3));
        List<ProjectionSegment> actualProjectionSegments = new ArrayList<>(((ShorthandProjectionSegment) actualProjection).getActualProjectionSegments());
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(0)).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(1)).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(2)).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is(""));
    }
    
    @Test
    void assertBindWithCipherDerivedExpression() {
        ProjectionsSegment withProjections = new ProjectionsSegment(0, 0);
        withProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        FunctionSegment ifNullFunction = new FunctionSegment(0, 0, "IFNULL", "IFNULL(user_name, 'N/A')");
        ifNullFunction.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("user_name")));
        ifNullFunction.getParameters().add(new LiteralExpressionSegment(0, 0, "N/A"));
        ExpressionProjectionSegment userNameProjection = new ExpressionProjectionSegment(0, 0, "IFNULL(user_name, 'N/A')", ifNullFunction);
        userNameProjection.setAlias(new AliasSegment(0, 0, new IdentifierValue("user_name")));
        withProjections.getProjections().add(userNameProjection);
        SelectStatement withSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(withProjections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
        WithSegment withSegment = new WithSegment(0, 0,
                new LinkedList<>(Collections.singleton(createCommonTableExpression("ifnulled", withSelectStatement))), false);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ShorthandProjectionSegment(0, 0));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).with(withSegment).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("ifnulled")))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        ProjectionSegment actualProjection = actual.getProjections().getProjections().iterator().next();
        List<ProjectionSegment> actualProjectionSegments = new ArrayList<>(((ShorthandProjectionSegment) actualProjection).getActualProjectionSegments());
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(1)).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindWithAggregationDerivedExpression() {
        ProjectionsSegment withProjections = new ProjectionsSegment(0, 0);
        withProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        AggregationProjectionSegment maxOrderIdProjection = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "MAX(order_id)");
        maxOrderIdProjection.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        maxOrderIdProjection.setAlias(new AliasSegment(0, 0, new IdentifierValue("max_id")));
        withProjections.getProjections().add(maxOrderIdProjection);
        SelectStatement withSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(withProjections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).build();
        WithSegment withSegment = new WithSegment(0, 0,
                new LinkedList<>(Collections.singleton(createCommonTableExpression("max_order", withSelectStatement))), false);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ShorthandProjectionSegment(0, 0));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).with(withSegment).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("max_order")))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        ProjectionSegment actualProjection = actual.getProjections().getProjections().iterator().next();
        List<ProjectionSegment> actualProjectionSegments = new ArrayList<>(((ShorthandProjectionSegment) actualProjection).getActualProjectionSegments());
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is(""));
        assertThat(((ColumnProjectionSegment) actualProjectionSegments.get(1)).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindWithPhysicalTableColumnAndCteColumn() {
        ProjectionsSegment withProjections = new ProjectionsSegment(0, 0);
        withProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        SelectStatement withSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(withProjections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).build();
        WithSegment withSegment = new WithSegment(0, 0,
                new LinkedList<>(Collections.singleton(createCommonTableExpression("order_users", withSelectStatement))), false);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_name"))));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).with(withSegment).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual.getProjections().getProjections());
        assertThat(((ColumnProjectionSegment) actualProjections.get(0)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        assertThat(((ColumnProjectionSegment) actualProjections.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
    }
    
    @Test
    void assertBindWithMultipleCtesAndJoinCondition() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(createOwnerColumnSegment("ui", "user_name")));
        projections.getProjections().add(new ColumnProjectionSegment(createOwnerColumnSegment("uo", "order_cnt")));
        ColumnSegment leftOnColumn = createOwnerColumnSegment("ui", "user_id");
        ColumnSegment rightOnColumn = createOwnerColumnSegment("uo", "user_id");
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(createAliasedSimpleTableSegment("user_info", "ui"));
        joinTableSegment.setRight(createAliasedSimpleTableSegment("user_orders", "uo"));
        joinTableSegment.setCondition(new BinaryOperationExpression(0, 0, leftOnColumn, rightOnColumn, "=", "ui.user_id = uo.user_id"));
        ColumnSegment orderByColumn = createOwnerColumnSegment("ui", "user_id");
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType)
                .with(new WithSegment(0, 0, new LinkedList<>(Arrays.asList(
                        createCommonTableExpression("user_orders", createUserOrdersSelectStatement()),
                        createCommonTableExpression("user_info", createUserInfoSelectStatement()))), false))
                .projections(projections).from(joinTableSegment)
                .orderBy(new OrderBySegment(0, 0, Collections.singleton(new ColumnOrderByItemSegment(orderByColumn, OrderDirection.ASC, null)))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual.getProjections().getProjections());
        ColumnSegment actualUserNameColumn = ((ColumnProjectionSegment) actualProjections.get(0)).getColumn();
        assertThat(actualUserNameColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        assertThat(actualUserNameColumn.getColumnBoundInfo().getTableSourceType().name(), is("TEMPORARY_TABLE"));
        ColumnSegment actualOrderCountColumn = ((ColumnProjectionSegment) actualProjections.get(1)).getColumn();
        assertThat(actualOrderCountColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualOrderCountColumn.getColumnBoundInfo().getTableSourceType().name(), is("TEMPORARY_TABLE"));
        BinaryOperationExpression actualJoinCondition = (BinaryOperationExpression) ((JoinTableSegment) actual.getFrom().get()).getCondition();
        assertThat(((ColumnSegment) actualJoinCondition.getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
        assertThat(((ColumnSegment) actualJoinCondition.getRight()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        ColumnOrderByItemSegment actualOrderByItem = (ColumnOrderByItemSegment) actual.getOrderBy().get().getOrderByItems().iterator().next();
        assertThat(actualOrderByItem.getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_user"));
    }
    
    @Test
    void assertBindRecursiveCteJoinCondition() {
        SelectStatement anchorSelect = SelectStatement.builder().databaseType(databaseType).projections(createOrderProjections())
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).build();
        ColumnSegment leftOnColumn = createOwnerColumnSegment("child", "user_id");
        ColumnSegment rightOnColumn = createOwnerColumnSegment("product_tree", "user_id");
        JoinTableSegment recursiveJoinTableSegment = new JoinTableSegment();
        recursiveJoinTableSegment.setLeft(createAliasedSimpleTableSegment("t_order", "child"));
        recursiveJoinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("product_tree"))));
        recursiveJoinTableSegment.setCondition(new BinaryOperationExpression(0, 0, leftOnColumn, rightOnColumn, "=", "child.user_id = product_tree.user_id"));
        SelectStatement recursiveSelect = SelectStatement.builder().databaseType(databaseType).projections(createChildOrderProjections()).from(recursiveJoinTableSegment).build();
        SelectStatement withSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(createOrderProjections())
                .combine(new CombineSegment(0, 0, new SubquerySegment(0, 0, anchorSelect, ""), CombineType.UNION_ALL, new SubquerySegment(0, 0, recursiveSelect, ""))).build();
        CommonTableExpressionSegment commonTableExpressionSegment = createCommonTableExpression("product_tree", withSelectStatement);
        commonTableExpressionSegment.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        commonTableExpressionSegment.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        WithSegment withSegment = new WithSegment(0, 0, new LinkedList<>(Collections.singleton(commonTableExpressionSegment)), true);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).with(withSegment).projections(createOrderProjections())
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("product_tree")))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual.getProjections().getProjections());
        assertThat(((ColumnProjectionSegment) actualProjections.get(0)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) actualProjections.get(1)).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        CommonTableExpressionSegment actualCommonTableExpression = actual.getWith().get().getCommonTableExpressions().iterator().next();
        SelectStatement actualRecursiveSelect = actualCommonTableExpression.getSubquery().getSelect().getCombine().get().getRight().getSelect();
        BinaryOperationExpression actualJoinCondition = (BinaryOperationExpression) ((JoinTableSegment) actualRecursiveSelect.getFrom().get()).getCondition();
        assertThat(((ColumnSegment) actualJoinCondition.getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnSegment) actualJoinCondition.getRight()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnSegment) actualJoinCondition.getRight()).getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(((ColumnSegment) actualJoinCondition.getRight()).getColumnBoundInfo().getTableSourceType().name(), is("TEMPORARY_TABLE"));
    }
    
    @Test
    void assertBindRecursiveCteSearchAndCycleColumns() {
        SelectStatement anchorSelect = SelectStatement.builder().databaseType(databaseType).projections(createOrderProjections())
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).build();
        SelectStatement withSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(createOrderProjections())
                .combine(new CombineSegment(0, 0, new SubquerySegment(0, 0, anchorSelect, ""), CombineType.UNION_ALL, new SubquerySegment(0, 0, anchorSelect, ""))).build();
        CommonTableExpressionSegment commonTableExpressionSegment = createCommonTableExpression("product_tree", withSelectStatement);
        commonTableExpressionSegment.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        commonTableExpressionSegment.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        commonTableExpressionSegment.getSearchColumns().add(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        commonTableExpressionSegment.getCycleColumns().add(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        WithSegment withSegment = new WithSegment(0, 0, new LinkedList<>(Collections.singleton(commonTableExpressionSegment)), true);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).with(withSegment).projections(createOrderProjections())
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("product_tree")))).build();
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement,
                new SQLStatementBinderContext(mockMetaData(), "foo_db", new HintValueContext(), selectStatement));
        CommonTableExpressionSegment actualCommonTableExpression = actual.getWith().get().getCommonTableExpressions().iterator().next();
        ColumnSegment actualSearchColumn = actualCommonTableExpression.getSearchColumns().iterator().next();
        assertThat(actualSearchColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualSearchColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        assertThat(actualSearchColumn.getColumnBoundInfo().getTableSourceType().name(), is("TEMPORARY_TABLE"));
        ColumnSegment actualCycleColumn = actualCommonTableExpression.getCycleColumns().iterator().next();
        assertThat(actualCycleColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actualCycleColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(actualCycleColumn.getColumnBoundInfo().getTableSourceType().name(), is("TEMPORARY_TABLE"));
    }
    
    private WhereSegment createWhereSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "nvl", "nvl(status, 0)");
        functionSegment.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("status")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, 0));
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, functionSegment, new LiteralExpressionSegment(0, 0, 0), "=", "nvl(status, 0) = 0"));
    }
    
    private CommonTableExpressionSegment createCommonTableExpression(final String alias, final SelectStatement selectStatement) {
        return new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue(alias)), new SubquerySegment(0, 0, selectStatement, ""));
    }
    
    private ProjectionsSegment createOrderProjections() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        result.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        return result;
    }
    
    private ProjectionsSegment createChildOrderProjections() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ColumnProjectionSegment(createOwnerColumnSegment("child", "user_id")));
        result.getProjections().add(new ColumnProjectionSegment(createOwnerColumnSegment("child", "order_id")));
        return result;
    }
    
    private SelectStatement createUserOrdersSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        ColumnProjectionSegment orderCountProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        orderCountProjection.setAlias(new AliasSegment(0, 0, new IdentifierValue("order_cnt")));
        projections.getProjections().add(orderCountProjection);
        return SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))).build();
    }
    
    private SelectStatement createUserInfoSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_name"))));
        return SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
    }
    
    private SimpleTableSegment createAliasedSimpleTableSegment(final String tableName, final String alias) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private ColumnSegment createOwnerColumnSegment(final String owner, final String columnName) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        return result;
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        IdentifierValue tUser = new IdentifierValue("t_user");
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable("t_user").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("email", Types.VARCHAR, false, false, false, true, false, false)));
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(tUser).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("email", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase)).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase(fooDatabase).getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_user")).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tOrder)).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tUser)).thenReturn(true);
        return result;
    }
}
