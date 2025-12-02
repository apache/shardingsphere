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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindInsertValues() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("order_id")),
                new ColumnSegment(0, 0, new IdentifierValue("user_id")), new ColumnSegment(0, 0, new IdentifierValue("status")))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "OK"))));
        InsertStatement actual = new InsertStatementBinder().bind(insertStatement, new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), insertStatement));
        assertThat(actual, not(insertStatement));
        assertTrue(actual.getTable().isPresent());
        assertTrue(insertStatement.getTable().isPresent());
        assertThat(actual.getTable().get().getTableName(), not(insertStatement.getTable().get().getTableName()));
        assertTrue(actual.getInsertColumns().isPresent());
        assertInsertColumns(actual.getInsertColumns().get().getColumns());
    }
    
    private static void assertInsertColumns(final Collection<ColumnSegment> insertColumns) {
        assertThat(insertColumns.size(), is(3));
        Iterator<ColumnSegment> iterator = insertColumns.iterator();
        ColumnSegment orderIdColumnSegment = iterator.next();
        assertThat(orderIdColumnSegment.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(orderIdColumnSegment.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(orderIdColumnSegment.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(orderIdColumnSegment.getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        ColumnSegment userIdColumnSegment = iterator.next();
        assertThat(userIdColumnSegment.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(userIdColumnSegment.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(userIdColumnSegment.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(userIdColumnSegment.getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        ColumnSegment statusColumnSegment = iterator.next();
        assertThat(statusColumnSegment.getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(statusColumnSegment.getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(statusColumnSegment.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(statusColumnSegment.getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
    }
    
    @Test
    void assertBindInsertSelectWithColumns() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("order_id")),
                new ColumnSegment(0, 0, new IdentifierValue("user_id")), new ColumnSegment(0, 0, new IdentifierValue("status")))));
        SelectStatement subSelectStatement = new SelectStatement(databaseType);
        subSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
        subSelectStatement.setProjections(projections);
        insertStatement.setInsertSelect(new SubquerySegment(0, 0, subSelectStatement, ""));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "OK"))));
        InsertStatement actual = new InsertStatementBinder().bind(insertStatement, new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), insertStatement));
        assertThat(actual, not(insertStatement));
        assertTrue(actual.getTable().isPresent());
        assertTrue(insertStatement.getTable().isPresent());
        assertThat(actual.getTable().get().getTableName(), not(insertStatement.getTable().get().getTableName()));
        assertTrue(actual.getInsertColumns().isPresent());
        assertInsertColumns(actual.getInsertColumns().get().getColumns());
        assertInsertSelect(actual);
    }
    
    @Test
    void assertBindInsertSelectWithoutColumns() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        SelectStatement subSelectStatement = new SelectStatement(databaseType);
        subSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status"))));
        subSelectStatement.setProjections(projections);
        insertStatement.setInsertSelect(new SubquerySegment(0, 0, subSelectStatement, ""));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "OK"))));
        InsertStatement actual = new InsertStatementBinder().bind(insertStatement, new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), insertStatement));
        assertThat(actual, not(insertStatement));
        assertTrue(actual.getTable().isPresent());
        assertTrue(insertStatement.getTable().isPresent());
        assertThat(actual.getTable().get().getTableName(), not(insertStatement.getTable().get().getTableName()));
        assertInsertColumns(actual.getDerivedInsertColumns());
        assertInsertSelect(actual);
    }
    
    private static void assertInsertSelect(final InsertStatement actual) {
        assertTrue(actual.getInsertSelect().isPresent());
        Collection<ProjectionSegment> actualProjections = actual.getInsertSelect().get().getSelect().getProjections().getProjections();
        assertThat(actualProjections.size(), is(3));
        Iterator<ProjectionSegment> projectionIterator = actualProjections.iterator();
        ProjectionSegment orderIdProjectionSegment = projectionIterator.next();
        assertThat(orderIdProjectionSegment, isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) orderIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        ProjectionSegment userIdProjectionSegment = projectionIterator.next();
        assertThat(userIdProjectionSegment, isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) userIdProjectionSegment).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("user_id"));
        ProjectionSegment statusProjectionSegment = projectionIterator.next();
        assertThat(statusProjectionSegment, isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) statusProjectionSegment).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
    }
    
    private ShardingSphereMetaData createMetaData() {
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
