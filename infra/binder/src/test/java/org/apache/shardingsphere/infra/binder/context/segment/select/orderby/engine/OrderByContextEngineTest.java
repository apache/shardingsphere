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

package org.apache.shardingsphere.infra.binder.context.segment.select.orderby.engine;

import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderByContextEngineTest {
    
    @Test
    void assertCreateOrderByWithoutOrderByForMySQL() {
        assertCreateOrderByWithoutOrderBy(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithoutOrderByForOracle() {
        assertCreateOrderByWithoutOrderBy(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithoutOrderByForPostgreSQL() {
        assertCreateOrderByWithoutOrderBy(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithoutOrderByForSQL92() {
        assertCreateOrderByWithoutOrderBy(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithoutOrderByForSQLServer() {
        assertCreateOrderByWithoutOrderBy(new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateOrderByWithoutOrderBy(final GenericSelectStatement selectStatement) {
        OrderByItem orderByItem1 = new OrderByItem(new IndexOrderByItemSegment(0, 1, 1, OrderDirection.ASC, NullsOrderType.LAST));
        OrderByItem orderByItem2 = new OrderByItem(new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, NullsOrderType.LAST));
        Collection<OrderByItem> orderByItems = Arrays.asList(orderByItem1, orderByItem2);
        GroupByContext groupByContext = new GroupByContext(orderByItems);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(selectStatement, groupByContext);
        assertThat(actualOrderByContext.getItems(), is(orderByItems));
        assertTrue(actualOrderByContext.isGenerated());
    }
    
    @Test
    void assertCreateOrderByWithOrderByForMySQL() {
        assertCreateOrderByWithOrderBy(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithOrderByForOracle() {
        assertCreateOrderByWithOrderBy(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithOrderByForPostgreSQL() {
        assertCreateOrderByWithOrderBy(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithOrderByForSQL92() {
        assertCreateOrderByWithOrderBy(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderByWithOrderByForSQLServer() {
        assertCreateOrderByWithOrderBy(new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateOrderByWithOrderBy(final GenericSelectStatement selectStatement) {
        OrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")), OrderDirection.ASC, NullsOrderType.FIRST);
        OrderByItemSegment indexOrderByItemSegment1 = new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, NullsOrderType.LAST);
        OrderByItemSegment indexOrderByItemSegment2 = new IndexOrderByItemSegment(2, 3, 3, OrderDirection.ASC, NullsOrderType.LAST);
        OrderBySegment orderBySegment = new OrderBySegment(0, 1, Arrays.asList(columnOrderByItemSegment, indexOrderByItemSegment1, indexOrderByItemSegment2));
        selectStatement.setOrderBy(orderBySegment);
        GroupByContext emptyGroupByContext = new GroupByContext(Collections.emptyList());
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(selectStatement, emptyGroupByContext);
        OrderByItem expectedOrderByItem1 = new OrderByItem(columnOrderByItemSegment);
        OrderByItem expectedOrderByItem2 = new OrderByItem(indexOrderByItemSegment1);
        expectedOrderByItem2.setIndex(2);
        OrderByItem expectedOrderByItem3 = new OrderByItem(indexOrderByItemSegment2);
        expectedOrderByItem3.setIndex(3);
        assertThat(actualOrderByContext.getItems(), is(Arrays.asList(expectedOrderByItem1, expectedOrderByItem2, expectedOrderByItem3)));
        assertFalse(actualOrderByContext.isGenerated());
    }
    
    @Test
    void assertCreateOrderInDistinctByWithoutOrderByForMySQL() {
        assertCreateOrderInDistinctByWithoutOrderBy(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderInDistinctByWithoutOrderByForOracle() {
        assertCreateOrderInDistinctByWithoutOrderBy(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderInDistinctByWithoutOrderByForPostgreSQL() {
        assertCreateOrderInDistinctByWithoutOrderBy(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderInDistinctByWithoutOrderByForSQL92() {
        assertCreateOrderInDistinctByWithoutOrderBy(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateOrderInDistinctByWithoutOrderByForSQLServer() {
        assertCreateOrderInDistinctByWithoutOrderBy(new SQLServerGenericSelectStatement());
    }
    
    void assertCreateOrderInDistinctByWithoutOrderBy(final GenericSelectStatement selectStatement) {
        ColumnProjectionSegment columnProjectionSegment1 = new ColumnProjectionSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")));
        ColumnProjectionSegment columnProjectionSegment2 = new ColumnProjectionSegment(new ColumnSegment(1, 2, new IdentifierValue("column2")));
        List<ProjectionSegment> list = Arrays.asList(columnProjectionSegment1, columnProjectionSegment2);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 1);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().addAll(list);
        selectStatement.setProjections(projectionsSegment);
        GroupByContext groupByContext = new GroupByContext(Collections.emptyList());
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(selectStatement, groupByContext);
        assertThat(actualOrderByContext.getItems().size(), is(list.size()));
        List<OrderByItem> items = (List<OrderByItem>) actualOrderByContext.getItems();
        assertThat(((ColumnOrderByItemSegment) items.get(0).getSegment()).getColumn(), is(columnProjectionSegment1.getColumn()));
        assertThat(((ColumnOrderByItemSegment) items.get(1).getSegment()).getColumn(), is(columnProjectionSegment2.getColumn()));
        assertTrue(actualOrderByContext.isGenerated());
    }
}
