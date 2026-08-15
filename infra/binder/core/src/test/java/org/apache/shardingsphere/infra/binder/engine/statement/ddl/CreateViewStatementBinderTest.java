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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.ViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateViewStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindCreateViewWithColumns() {
        CreateViewStatement createViewStatement = createCreateViewStatement();
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        CreateViewStatement actual = new CreateViewStatementBinder().bind(createViewStatement,
                new SQLStatementBinderContext(createMetaData(), "foo_db", hintValueContext, createViewStatement, Collections.emptyList()));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getColumns().get(0).getColumn().getIdentifier().getValue(), is("foo_id"));
        assertThat(actual.getColumns().get(0).getComment().orElse(null), is("id comment"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("foo_id"));
        assertThat(actual.getColumns().get(0).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    private CreateViewStatement createCreateViewStatement() {
        CreateViewStatement result = new CreateViewStatement(databaseType);
        result.setView(new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("foo_view"))));
        result.setSelect(createSelectStatement());
        result.getColumns().add(new ViewColumnSegment(9, 14, new ColumnSegment(9, 14, new IdentifierValue("foo_id")), "id comment"));
        return result;
    }
    
    private SelectStatement createSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(35, 35);
        projections.getProjections().add(new ExpressionProjectionSegment(35, 35, "1", new LiteralExpressionSegment(35, 35, 1)));
        return SelectStatement.builder().databaseType(databaseType).projections(projections).build();
    }
    
    private ShardingSphereMetaData createMetaData() {
        IdentifierValue databaseName = new IdentifierValue("foo_db");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.containsDatabase(eq(databaseName))).thenReturn(true);
        when(result.getDatabase(databaseName)).thenReturn(database);
        when(result.getDatabase(databaseName.getValue())).thenReturn(database);
        return result;
    }
}
