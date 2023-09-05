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

import org.apache.shardingsphere.infra.binder.statement.dml.MergeStatementBinder;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleMergeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleUpdateStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MergeStatementBinderTest {
    
    @Test
    void assertBind() {
        MergeStatement mergeStatement = new OracleMergeStatement();
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        targetTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        mergeStatement.setTarget(targetTable);
        SimpleTableSegment sourceTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        sourceTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("b")));
        mergeStatement.setSource(sourceTable);
        mergeStatement.setExpr(new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("id")),
                new ColumnSegment(0, 0, new IdentifierValue("order_id")), "=", "id = order_id"));
        UpdateStatement updateStatement = new OracleUpdateStatement();
        updateStatement.setTable(targetTable);
        ColumnSegment targetTableColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        targetTableColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        ColumnSegment sourceTableColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        sourceTableColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("b")));
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0,
                Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(targetTableColumn), sourceTableColumn)));
        updateStatement.setSetAssignment(setAssignmentSegment);
        mergeStatement.setUpdate(updateStatement);
        MergeStatement actual = new MergeStatementBinder().bind(mergeStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(mergeStatement));
        assertThat(actual.getSource(), not(mergeStatement.getSource()));
        assertThat(actual.getSource(), instanceOf(SimpleTableSegment.class));
        assertThat(actual.getTarget(), not(mergeStatement.getTarget()));
        assertThat(actual.getTarget(), instanceOf(SimpleTableSegment.class));
        assertThat(actual.getUpdate(), not(mergeStatement.getUpdate()));
        assertThat(actual.getUpdate().getSetAssignment().getAssignments().iterator().next().getValue(), instanceOf(ColumnSegment.class));
        assertThat(((ColumnSegment) actual.getUpdate().getSetAssignment().getAssignments().iterator().next().getValue()).getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order_item"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable("t_order_item").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        when(result.containsDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).containsSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME).containsTable("t_order")).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME).containsTable("t_order_item")).thenReturn(true);
        return result;
    }
}
