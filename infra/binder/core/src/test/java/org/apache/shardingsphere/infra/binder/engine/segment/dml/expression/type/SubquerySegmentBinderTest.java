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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubquerySegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBind() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        ColumnSegment columnSegment = new ColumnSegment(58, 65, new IdentifierValue("order_id"));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(58, 65);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(72, 78, new IdentifierValue("t_order"))));
        ExpressionSegment whereExpressionSegment = new ColumnSegment(86, 91, new IdentifierValue("status"));
        selectStatement.setWhere(new WhereSegment(80, 102, whereExpressionSegment));
        SubquerySegment subquerySegment = new SubquerySegment(39, 103, selectStatement, "order_id = (SELECT order_id FROM t_order WHERE status = 'SUBMIT')");
        SQLStatementBinderContext sqlStatementBinderContext = new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), selectStatement);
        ColumnSegment boundNameColumn = new ColumnSegment(7, 13, new IdentifierValue("user_id"));
        boundNameColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(
                new IdentifierValue("foo_db"), new IdentifierValue("foo_db")), new IdentifierValue("t_order_item"), new IdentifierValue("user_id"), TableSourceType.TEMPORARY_TABLE));
        sqlStatementBinderContext.getExternalTableBinderContexts().put(new CaseInsensitiveString("t_order_item"),
                new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundNameColumn)), TableSourceType.TEMPORARY_TABLE));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        SubquerySegment actual = SubquerySegmentBinder.bind(subquerySegment, sqlStatementBinderContext, outerTableBinderContexts);
        assertNotNull(actual.getSelect());
        assertTrue(actual.getSelect().getFrom().isPresent());
        assertThat(actual.getSelect().getFrom().get(), isA(SimpleTableSegment.class));
        assertThat(((SimpleTableSegment) actual.getSelect().getFrom().get()).getTableName().getIdentifier().getValue(), is("t_order"));
        assertTrue(actual.getSelect().getWhere().isPresent());
        assertThat(actual.getSelect().getWhere().get().getExpr(), isA(ColumnSegment.class));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getIdentifier().getValue(), is("status"));
        assertNotNull(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundInfo());
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
        assertNotNull(actual.getSelect().getProjections());
        assertThat(actual.getSelect().getProjections().getProjections().size(), is(1));
        ProjectionSegment column = actual.getSelect().getProjections().getProjections().iterator().next();
        assertThat(column, isA(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) column).getColumn().getIdentifier().getValue(), is("order_id"));
        assertNotNull(((ColumnProjectionSegment) column).getColumn().getColumnBoundInfo());
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is("foo_db"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is("foo_db"));
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
