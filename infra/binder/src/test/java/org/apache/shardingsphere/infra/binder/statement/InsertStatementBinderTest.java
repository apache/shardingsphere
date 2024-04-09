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

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertStatementBinderTest {
    
    @Test
    void assertBindInsertValues() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("order_id")),
                new ColumnSegment(0, 0, new IdentifierValue("user_id")), new ColumnSegment(0, 0, new IdentifierValue("status")))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "OK"))));
        InsertStatement actual = new InsertStatementBinder().bind(insertStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(insertStatement));
        assertThat(actual.getTable().getTableName(), not(insertStatement.getTable().getTableName()));
        assertTrue(actual.getInsertColumns().isPresent());
        assertInsertColumns(actual.getInsertColumns().get().getColumns());
    }
    
    private static void assertInsertColumns(final Collection<ColumnSegment> insertColumns) {
        assertThat(insertColumns.size(), is(3));
        Iterator<ColumnSegment> iterator = insertColumns.iterator();
        ColumnSegment orderIdColumnSegment = iterator.next();
        assertThat(orderIdColumnSegment.getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(orderIdColumnSegment.getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(orderIdColumnSegment.getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(orderIdColumnSegment.getColumnBoundedInfo().getOriginalColumn().getValue(), is("order_id"));
        ColumnSegment userIdColumnSegment = iterator.next();
        assertThat(userIdColumnSegment.getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(userIdColumnSegment.getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(userIdColumnSegment.getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(userIdColumnSegment.getColumnBoundedInfo().getOriginalColumn().getValue(), is("user_id"));
        ColumnSegment statusColumnSegment = iterator.next();
        assertThat(statusColumnSegment.getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(statusColumnSegment.getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(statusColumnSegment.getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(statusColumnSegment.getColumnBoundedInfo().getOriginalColumn().getValue(), is("status"));
    }
    
    @Test
    void assertBindInsertSelectWithColumns() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("order_id")),
                new ColumnSegment(0, 0, new IdentifierValue("user_id")), new ColumnSegment(0, 0, new IdentifierValue("status")))));
        MySQLSelectStatement subSelectStatement = new MySQLSelectStatement();
        subSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
        subSelectStatement.setProjections(projections);
        insertStatement.setInsertSelect(new SubquerySegment(0, 0, subSelectStatement, ""));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "OK"))));
        InsertStatement actual = new InsertStatementBinder().bind(insertStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(insertStatement));
        assertThat(actual.getTable().getTableName(), not(insertStatement.getTable().getTableName()));
        assertTrue(actual.getInsertColumns().isPresent());
        assertInsertColumns(actual.getInsertColumns().get().getColumns());
        assertInsertSelect(actual);
    }
    
    @Test
    void assertBindInsertSelectWithoutColumns() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        MySQLSelectStatement subSelectStatement = new MySQLSelectStatement();
        subSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
        subSelectStatement.setProjections(projections);
        insertStatement.setInsertSelect(new SubquerySegment(0, 0, subSelectStatement, ""));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "OK"))));
        InsertStatement actual = new InsertStatementBinder().bind(insertStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(insertStatement));
        assertThat(actual.getTable().getTableName(), not(insertStatement.getTable().getTableName()));
        assertInsertColumns(actual.getDerivedInsertColumns());
        assertInsertSelect(actual);
    }
    
    private static void assertInsertSelect(final InsertStatement actual) {
        assertTrue(actual.getInsertSelect().isPresent());
        Collection<ProjectionSegment> actualProjections = actual.getInsertSelect().get().getSelect().getProjections().getProjections();
        assertThat(actualProjections.size(), is(3));
        Iterator<ProjectionSegment> projectionIterator = actualProjections.iterator();
        ProjectionSegment orderIdProjectionSegment = projectionIterator.next();
        assertThat(orderIdProjectionSegment, instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalColumn().getValue(), is("order_id"));
        ProjectionSegment userIdProjectionSegment = projectionIterator.next();
        assertThat(userIdProjectionSegment, instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalColumn().getValue(), is("user_id"));
        ProjectionSegment statusProjectionSegment = projectionIterator.next();
        assertThat(statusProjectionSegment, instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundedInfo().getOriginalColumn().getValue(), is("status"));
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
