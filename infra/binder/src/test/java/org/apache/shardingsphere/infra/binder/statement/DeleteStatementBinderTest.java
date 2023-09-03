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

package org.apache.shardingsphere.infra.binder.statement;

import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteStatementBinderTest {
    
    @Test
    void assertBind() {
        DeleteStatement deleteStatement = new MySQLDeleteStatement();
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        deleteStatement.setTable(simpleTableSegment);
        deleteStatement.setWhere(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("status")),
                new LiteralExpressionSegment(0, 0, 0), "=", "status = 1")));
        DeleteStatement actual = new DeleteStatementBinder().bind(deleteStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(deleteStatement));
        assertThat(actual.getTable(), not(deleteStatement.getTable()));
        assertThat(actual.getTable(), instanceOf(SimpleTableSegment.class));
        assertTrue(actual.getWhere().isPresent());
        assertThat(actual.getWhere().get(), not(deleteStatement.getWhere()));
        assertThat(actual.getWhere().get(), instanceOf(WhereSegment.class));
        assertTrue(deleteStatement.getWhere().isPresent());
        assertThat(actual.getWhere().get().getExpr(), not(deleteStatement.getWhere().get().getExpr()));
        assertThat(actual.getWhere().get().getExpr(), instanceOf(BinaryOperationExpression.class));
        assertThat(((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft(), instanceOf(ColumnSegment.class));
        assertThat(((ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        when(result.containsDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).containsSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME).containsTable("t_order")).thenReturn(true);
        return result;
    }
}
