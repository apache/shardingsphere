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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.ViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateViewStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindCreateViewWithExpressionColumn() {
        CreateViewStatement createViewStatement = createCreateViewStatement(createExpressionSelectStatement());
        CreateViewStatement actual = new CreateViewStatementBinder().bind(createViewStatement, createBinderContext(createViewStatement));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getColumns().get(0).getColumn().getIdentifier().getValue(), is("foo_id"));
        assertThat(actual.getColumns().get(0).getComment().orElse(null), is("id comment"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("foo_id"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    @Test
    void assertBindCreateViewWithProjectionColumnBoundInfo() {
        CreateViewStatement createViewStatement = createCreateViewStatement(createColumnSelectStatement());
        CreateViewStatement actual = new CreateViewStatementBinder().bind(createViewStatement, createBinderContext(createViewStatement));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getColumns().get(0).getColumn().getIdentifier().getValue(), is("foo_id"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    private SQLStatementBinderContext createBinderContext(final CreateViewStatement createViewStatement) {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        return new SQLStatementBinderContext(createMetaData(), "foo_db", hintValueContext, createViewStatement, Collections.emptyList());
    }
    
    private CreateViewStatement createCreateViewStatement(final SelectStatement selectStatement) {
        CreateViewStatement result = new CreateViewStatement(databaseType);
        result.setView(new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("foo_view"))));
        result.setSelect(selectStatement);
        result.getColumns().add(new ViewColumnSegment(9, 14, new ColumnSegment(9, 14, new IdentifierValue("foo_id")), "id comment"));
        return result;
    }
    
    private SelectStatement createExpressionSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(35, 35);
        projections.getProjections().add(new ExpressionProjectionSegment(35, 35, "1", new LiteralExpressionSegment(35, 35, 1)));
        return SelectStatement.builder().databaseType(databaseType).projections(projections).build();
    }
    
    private SelectStatement createColumnSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(35, 35);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(35, 42, new IdentifierValue("order_id"))));
        return SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(48, 54, new IdentifierValue("t_order")))).build();
    }
    
    private ShardingSphereMetaData createMetaData() {
        IdentifierValue databaseName = new IdentifierValue("foo_db");
        IdentifierValue tableName = new IdentifierValue("t_order");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getName()).thenReturn("foo_db");
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(tableName).getAllColumns()).thenReturn(Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.containsDatabase(databaseName)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase(databaseName).getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase(databaseName).containsSchema(databaseName)).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.getDatabase(databaseName).getSchema(databaseName)).thenReturn(schema);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        when(result.getDatabase(databaseName).getSchema(databaseName).containsTable(tableName)).thenReturn(true);
        return result;
    }
}
