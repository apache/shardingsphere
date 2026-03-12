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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    
    private WhereSegment createWhereSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "nvl", "nvl(status, 0)");
        functionSegment.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("status")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, 0));
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, functionSegment, new LiteralExpressionSegment(0, 0, 0), "=", "nvl(status, 0) = 0"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable("t_user").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_name", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_user")).thenReturn(true);
        return result;
    }
}
