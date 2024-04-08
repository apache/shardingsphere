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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import org.apache.shardingsphere.infra.binder.segment.from.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.bounded.ColumnSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class SubquerySegmentBinderTest {
    
    @Test
    void assertBind() {
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        ColumnSegment columnSegment = new ColumnSegment(58, 65, new IdentifierValue("order_id"));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(58, 65);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        mySQLSelectStatement.setProjections(projectionsSegment);
        mySQLSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(72, 78, new IdentifierValue("t_order"))));
        ExpressionSegment whereExpressionSegment = new ColumnSegment(86, 91, new IdentifierValue("status"));
        mySQLSelectStatement.setWhere(new WhereSegment(80, 102, whereExpressionSegment));
        SubquerySegment subquerySegment = new SubquerySegment(39, 103, mySQLSelectStatement, "order_id = (SELECT order_id FROM t_order WHERE status = 'SUBMIT')");
        SQLStatementBinderContext sqlStatementBinderContext =
                new SQLStatementBinderContext(createMetaData(), DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.emptySet());
        ColumnSegment boundedNameColumn = new ColumnSegment(7, 13, new IdentifierValue("user_id"));
        boundedNameColumn.setColumnBoundedInfo(new ColumnSegmentBoundedInfo(new IdentifierValue(DefaultDatabase.LOGIC_NAME), new IdentifierValue(DefaultDatabase.LOGIC_NAME),
                new IdentifierValue("t_order_item"), new IdentifierValue("user_id")));
        sqlStatementBinderContext.getExternalTableBinderContexts().put("t_order_item", new SimpleTableSegmentBinderContext(Collections.singleton(new ColumnProjectionSegment(boundedNameColumn))));
        Map<String, TableSegmentBinderContext> outerTableBinderContexts = new LinkedHashMap<>();
        SubquerySegment actual = SubquerySegmentBinder.bind(subquerySegment, sqlStatementBinderContext, outerTableBinderContexts);
        assertNotNull(actual.getSelect());
        assertTrue(actual.getSelect().getFrom().isPresent());
        assertInstanceOf(SimpleTableSegment.class, actual.getSelect().getFrom().get());
        assertThat(((SimpleTableSegment) actual.getSelect().getFrom().get()).getTableName().getIdentifier().getValue(), is("t_order"));
        assertNotNull(((SimpleTableSegment) actual.getSelect().getFrom().get()).getTableName().getTableBoundedInfo());
        assertThat(((SimpleTableSegment) actual.getSelect().getFrom().get()).getTableName().getTableBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) actual.getSelect().getFrom().get()).getTableName().getTableBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(actual.getSelect().getWhere().isPresent());
        assertInstanceOf(ColumnSegment.class, actual.getSelect().getWhere().get().getExpr());
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getIdentifier().getValue(), is("status"));
        assertNotNull(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundedInfo());
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnSegment) actual.getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertNotNull(actual.getSelect().getProjections());
        assertThat(actual.getSelect().getProjections().getProjections().size(), is(1));
        ProjectionSegment column = actual.getSelect().getProjections().getProjections().iterator().next();
        assertInstanceOf(ColumnProjectionSegment.class, column);
        assertThat(((ColumnProjectionSegment) column).getColumn().getIdentifier().getValue(), is("order_id"));
        assertNotNull(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo());
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
    }
    
    @Test
    void assertBindUseWithClause() {
        ColumnSegment columnSegment = new ColumnSegment(29, 36, new IdentifierValue("order_id"));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(29, 36);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        OracleSelectStatement oracleSubquerySelectStatement = new OracleSelectStatement();
        oracleSubquerySelectStatement.setProjections(projectionsSegment);
        oracleSubquerySelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(43, 49, new IdentifierValue("t_order"))));
        ExpressionSegment whereExpressionSegment = new ColumnSegment(57, 62, new IdentifierValue("status"));
        oracleSubquerySelectStatement.setWhere(new WhereSegment(51, 73, whereExpressionSegment));
        CommonTableExpressionSegment commonTableExpressionSegment = new CommonTableExpressionSegment(0, 1, new IdentifierValue("submit_order"),
                new SubquerySegment(22, 73, oracleSubquerySelectStatement, "SELECT order_id FROM t_order WHERE status = 'SUBMIT'"));
        WithSegment withSegment = new WithSegment(0, 74, Collections.singleton(commonTableExpressionSegment));
        OracleSelectStatement oracleSelectStatement = new OracleSelectStatement();
        oracleSelectStatement.setWithSegment(withSegment);
        oracleSelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubquerySegment subquerySegment = new SubquerySegment(0, 74, oracleSelectStatement, "WITH submit_order AS (SELECT order_id FROM t_order WHERE status = 'SUBMIT')");
        SQLStatementBinderContext sqlStatementBinderContext =
                new SQLStatementBinderContext(createMetaData(), DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.emptySet());
        Map<String, TableSegmentBinderContext> outerTableBinderContexts = new LinkedHashMap<>();
        SubquerySegment actual = SubquerySegmentBinder.bind(subquerySegment, sqlStatementBinderContext, outerTableBinderContexts);
        assertNotNull(actual.getSelect());
        assertInstanceOf(OracleSelectStatement.class, actual.getSelect());
        assertTrue(((OracleSelectStatement) actual.getSelect()).getWithSegment().isPresent());
        assertNotNull(((OracleSelectStatement) actual.getSelect()).getWithSegment().get().getCommonTableExpressions());
        assertThat(((OracleSelectStatement) actual.getSelect()).getWithSegment().get().getCommonTableExpressions().size(), is(1));
        CommonTableExpressionSegment expressionSegment = ((OracleSelectStatement) actual.getSelect()).getWithSegment().get().getCommonTableExpressions().iterator().next();
        assertNotNull(expressionSegment.getSubquery().getSelect());
        assertInstanceOf(OracleSelectStatement.class, expressionSegment.getSubquery().getSelect());
        assertTrue(expressionSegment.getSubquery().getSelect().getFrom().isPresent());
        assertInstanceOf(SimpleTableSegment.class, expressionSegment.getSubquery().getSelect().getFrom().get());
        assertThat(((SimpleTableSegment) expressionSegment.getSubquery().getSelect().getFrom().get()).getTableName().getIdentifier().getValue(), is("t_order"));
        assertNotNull(((SimpleTableSegment) expressionSegment.getSubquery().getSelect().getFrom().get()).getTableName().getTableBoundedInfo());
        assertThat(((SimpleTableSegment) expressionSegment.getSubquery().getSelect().getFrom().get()).getTableName().getTableBoundedInfo().getOriginalSchema().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SimpleTableSegment) expressionSegment.getSubquery().getSelect().getFrom().get()).getTableName().getTableBoundedInfo().getOriginalDatabase().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertNotNull(expressionSegment.getSubquery().getSelect().getProjections());
        assertNotNull(expressionSegment.getSubquery().getSelect().getProjections().getProjections());
        assertThat(expressionSegment.getSubquery().getSelect().getProjections().getProjections().size(), is(1));
        ProjectionSegment column = expressionSegment.getSubquery().getSelect().getProjections().getProjections().iterator().next();
        assertInstanceOf(ColumnProjectionSegment.class, column);
        assertThat(((ColumnProjectionSegment) column).getColumn().getIdentifier().getValue(), is("order_id"));
        assertNotNull(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo());
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalColumn().getValue(), is("order_id"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) column).getColumn().getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(expressionSegment.getSubquery().getSelect().getWhere().isPresent());
        assertNotNull(((ColumnSegment) expressionSegment.getSubquery().getSelect().getWhere().get().getExpr()).getColumnBoundedInfo());
        assertThat(((ColumnSegment) expressionSegment.getSubquery().getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalColumn().getValue(), is("status"));
        assertThat(((ColumnSegment) expressionSegment.getSubquery().getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnSegment) expressionSegment.getSubquery().getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnSegment) expressionSegment.getSubquery().getSelect().getWhere().get().getExpr()).getColumnBoundedInfo().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
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
