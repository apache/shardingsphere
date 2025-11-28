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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBind() {
        UpdateStatement updateStatement = new UpdateStatement(databaseType);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        updateStatement.setTable(simpleTableSegment);
        updateStatement.setWhere(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("status")),
                new LiteralExpressionSegment(0, 0, 0), "=", "status = 1")));
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement, new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), updateStatement));
        assertThat(actual, not(updateStatement));
        assertThat(actual.getTable(), not(updateStatement.getTable()));
        assertThat(actual.getTable(), isA(SimpleTableSegment.class));
        assertTrue(actual.getWhere().isPresent());
        assertThat(actual.getWhere().get(), not(updateStatement.getWhere()));
        assertThat(actual.getWhere().get(), isA(WhereSegment.class));
        assertTrue(updateStatement.getWhere().isPresent());
        assertThat(actual.getWhere().get().getExpr(), not(updateStatement.getWhere().get().getExpr()));
        assertThat(actual.getWhere().get().getExpr(), isA(BinaryOperationExpression.class));
        assertThat(((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft(), isA(ColumnSegment.class));
        assertThat(((ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false,"int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"int", false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false,"int", false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        return result;
    }
}
