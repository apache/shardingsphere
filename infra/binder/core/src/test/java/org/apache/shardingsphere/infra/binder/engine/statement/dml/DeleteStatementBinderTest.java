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
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBind() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        DeleteStatement deleteStatement = DeleteStatement.builder()
                .databaseType(databaseType)
                .table(simpleTableSegment)
                .where(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("status")),
                        new LiteralExpressionSegment(0, 0, 0), "=", "status = 1")))
                .build();
        DeleteStatement actual = new DeleteStatementBinder().bind(deleteStatement, new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), deleteStatement));
        assertThat(actual, not(deleteStatement));
        assertThat(actual.getTable(), not(deleteStatement.getTable()));
        assertThat(actual.getTable(), isA(SimpleTableSegment.class));
        assertTrue(actual.getWhere().isPresent());
        assertThat(actual.getWhere().get(), not(deleteStatement.getWhere()));
        assertThat(actual.getWhere().get(), isA(WhereSegment.class));
        assertTrue(deleteStatement.getWhere().isPresent());
        assertThat(actual.getWhere().get().getExpr(), not(deleteStatement.getWhere().get().getExpr()));
        assertThat(actual.getWhere().get().getExpr(), isA(BinaryOperationExpression.class));
        assertThat(((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft(), isA(ColumnSegment.class));
        assertThat(((ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    @Test
    void assertBindOrderByColumnWithCte() {
        DeleteStatement deleteStatement = DeleteStatement.builder()
                .databaseType(databaseType)
                .with(createWithSegment())
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new ColumnOrderByItemSegment(
                        new ColumnSegment(0, 0, new IdentifierValue("user_id")), OrderDirection.ASC, null))))
                .build();
        DeleteStatement actual = new DeleteStatementBinder().bind(deleteStatement, new SQLStatementBinderContext(createMetaData(),
                "foo_db", new HintValueContext(), deleteStatement));
        ColumnOrderByItemSegment actualOrderByItem = (ColumnOrderByItemSegment) actual.getOrderBy().get().getOrderByItems().iterator().next();
        assertThat(actualOrderByItem.getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    private WithSegment createWithSegment() {
        return new WithSegment(0, 0, new LinkedList<>(Collections.singletonList(
                new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("combined_users")),
                        new SubquerySegment(0, 0, createWithSelectStatement(), "")))),
                false);
    }
    
    private SelectStatement createWithSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        return SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        IdentifierValue tUser = new IdentifierValue("t_user");
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable("t_user").getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(tUser).getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
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
