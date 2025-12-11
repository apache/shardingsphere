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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.delete;

import org.apache.calcite.sql.SqlDelete;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlWith;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

class DeleteStatementConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertSingleTableWithoutOrderByAndLimit() {
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(createSimpleTableSegment("t_order"));
        SqlDelete actual = (SqlDelete) new DeleteStatementConverter().convert(deleteStatement);
        assertThat(((SqlIdentifier) actual.getTargetTable()).getSimple(), is("t_order"));
        assertNull(actual.getCondition());
        assertNull(actual.getAlias());
    }
    
    @Test
    void assertConvertSingleTableWithAliasAndOrderBy() {
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(createSimpleTableSegmentWithAlias("t_order", "do"));
        deleteStatement.setWhere(new WhereSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0)));
        deleteStatement.setOrderBy(createOrderBySegment());
        SqlOrderBy actual = (SqlOrderBy) new DeleteStatementConverter().convert(deleteStatement);
        assertThat(actual.query, isA(SqlDelete.class));
        SqlDelete sqlDelete = (SqlDelete) actual.query;
        assertThat(((SqlIdentifier) sqlDelete.getTargetTable()).getSimple(), is("t_order"));
        assertNotNull(sqlDelete.getAlias());
        assertThat(sqlDelete.getAlias().toString(), is("do"));
        assertThat(actual.orderList.size(), is(1));
        assertThat(sqlDelete.getCondition(), isA(SqlDynamicParam.class));
    }
    
    @Test
    void assertConvertSingleTableWithLimit() {
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(createSimpleTableSegment("t_order"));
        LimitSegment limit = new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), new ParameterMarkerLimitValueSegment(0, 0, 0));
        deleteStatement.setLimit(limit);
        SqlOrderBy actual = (SqlOrderBy) new DeleteStatementConverter().convert(deleteStatement);
        assertThat(actual.offset, isA(SqlLiteral.class));
        assertNotNull(actual.offset);
        assertThat(actual.offset.toString(), is("1"));
        assertThat(actual.fetch, isA(SqlDynamicParam.class));
        assertNotNull(actual.fetch);
        assertThat(((SqlDynamicParam) actual.fetch).getIndex(), is(0));
    }
    
    @Test
    void assertConvertMultiTableWithAliasesAndWith() {
        DeleteMultiTableSegment multiTableSegment = new DeleteMultiTableSegment();
        multiTableSegment.setRelationTable(createJoinTableSegment());
        List<SimpleTableSegment> actualDeleteTables = new LinkedList<>();
        actualDeleteTables.add(createSimpleTableSegment("l"));
        SimpleTableSegment tableWithOwner = createSimpleTableSegment("r");
        tableWithOwner.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
        actualDeleteTables.add(tableWithOwner);
        actualDeleteTables.add(createSimpleTableSegment("DUAL"));
        multiTableSegment.setActualDeleteTables(actualDeleteTables);
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(multiTableSegment);
        deleteStatement.setWhere(new WhereSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0)));
        deleteStatement.setWith(createWithSegment());
        SqlWith actual = (SqlWith) new DeleteStatementConverter().convert(deleteStatement);
        SqlDelete sqlDelete = (SqlDelete) actual.body;
        assertThat(((SqlIdentifier) sqlDelete.getTargetTable()).getSimple(), is("left_table"));
        assertNotNull(sqlDelete.getAlias());
        assertThat(sqlDelete.getAlias().toString(), is("l"));
        assertThat(sqlDelete.getCondition(), isA(SqlDynamicParam.class));
        assertNotNull(sqlDelete.getCondition());
        assertThat(((SqlDynamicParam) sqlDelete.getCondition()).getIndex(), is(0));
    }
    
    @Test
    void assertConvertMultiTableWithoutAliasMapping() {
        DeleteMultiTableSegment multiTableSegment = new DeleteMultiTableSegment();
        SimpleTableSegment relationTable = createSimpleTableSegment("target_table");
        multiTableSegment.setRelationTable(relationTable);
        multiTableSegment.setActualDeleteTables(Collections.singletonList(createSimpleTableSegment("target_table")));
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(multiTableSegment);
        SqlDelete actual = (SqlDelete) new DeleteStatementConverter().convert(deleteStatement);
        assertThat(((SqlIdentifier) actual.getTargetTable()).getSimple(), is("target_table"));
        assertNotNull(actual.getAlias());
        assertThat(actual.getAlias().toString(), is("target_table"));
    }
    
    @Test
    void assertConvertSingleTableWithWithSegment() {
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(createSimpleTableSegment("t_order"));
        deleteStatement.setWhere(new WhereSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0)));
        deleteStatement.setWith(createWithSegment());
        SqlWith actual = (SqlWith) new DeleteStatementConverter().convert(deleteStatement);
        SqlDelete sqlDelete = (SqlDelete) actual.body;
        assertThat(((SqlIdentifier) sqlDelete.getTargetTable()).getSimple(), is("t_order"));
        assertNull(sqlDelete.getAlias());
        assertThat(sqlDelete.getCondition(), isA(SqlDynamicParam.class));
    }
    
    @Test
    void assertConvertMultiTableAliasEmptyBranch() {
        DeleteMultiTableSegment multiTableSegment = new DeleteMultiTableSegment();
        List<SimpleTableSegment> tables = spy(new LinkedList<>());
        tables.add(createSimpleTableSegment("target_table"));
        doAnswer((Answer<Void>) invocation -> null).when(tables).forEach(any());
        multiTableSegment.setActualDeleteTables(tables);
        multiTableSegment.setRelationTable(createSimpleTableSegmentWithAlias("target_table", "t"));
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(multiTableSegment);
        SqlDelete actual = (SqlDelete) new DeleteStatementConverter().convert(deleteStatement);
        assertNull(actual.getAlias());
    }
    
    @Test
    void assertConvertSingleTableWithoutTargetTable() {
        DeleteStatement deleteStatement = new DeleteStatement(databaseType);
        deleteStatement.setTable(createSimpleTableSegment("DUAL"));
        assertThrows(IllegalStateException.class, () -> new DeleteStatementConverter().convert(deleteStatement));
    }
    
    private SimpleTableSegment createSimpleTableSegment(final String tableName) {
        return new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
    }
    
    private SimpleTableSegment createSimpleTableSegmentWithAlias(final String tableName, final String alias) {
        SimpleTableSegment result = createSimpleTableSegment(tableName);
        result.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        return result;
    }
    
    private OrderBySegment createOrderBySegment() {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("order_col"));
        Collection<OrderByItemSegment> orderByItems = Collections.singleton(new ColumnOrderByItemSegment(column, OrderDirection.ASC, NullsOrderType.FIRST));
        return new OrderBySegment(0, 0, orderByItems);
    }
    
    private WithSegment createWithSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        selectStatement.setProjections(projectionsSegment);
        CommonTableExpressionSegment cte = new CommonTableExpressionSegment(
                0, 0, new AliasSegment(0, 0, new IdentifierValue("cte")), new SubquerySegment(0, 0, selectStatement, "subquery"));
        return new WithSegment(0, 0, Collections.singleton(cte), false);
    }
    
    private JoinTableSegment createJoinTableSegment() {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(createSimpleTableSegmentWithAlias("left_table", "l"));
        result.setRight(createSimpleTableSegmentWithAlias("right_table", "r"));
        return result;
    }
}
