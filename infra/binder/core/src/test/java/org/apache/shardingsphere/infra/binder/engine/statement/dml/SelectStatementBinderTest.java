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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
        SelectStatement selectStatement = new SelectStatement(databaseType);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projections);
        ColumnProjectionSegment orderIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        ColumnProjectionSegment userIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        ColumnProjectionSegment statusProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        projections.getProjections().add(orderIdProjection);
        projections.getProjections().add(userIdProjection);
        projections.getProjections().add(statusProjection);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        selectStatement.setFrom(simpleTableSegment);
        selectStatement.setWhere(createWhereSegment());
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
    
    private WhereSegment createWhereSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "nvl", "nvl(status, 0)");
        functionSegment.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("status")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, 0));
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, functionSegment, new LiteralExpressionSegment(0, 0, 0), "=", "nvl(status, 0) = 0"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, "int", false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, "int", false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        return result;
    }
}
