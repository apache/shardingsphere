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

package org.apache.shardingsphere.infra.binder.context.segment.select.groupby.engine;

import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSimpleSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupByContextEngineTest {
    
    @Test
    void assertCreateGroupByContextWithoutGroupByForMySQL() {
        assertCreateGroupByContextWithoutGroupBy(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithoutGroupByForOracle() {
        assertCreateGroupByContextWithoutGroupBy(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithoutGroupByForPostgreSQL() {
        assertCreateGroupByContextWithoutGroupBy(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithoutGroupByForSQL92() {
        assertCreateGroupByContextWithoutGroupBy(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithoutGroupByForSQLServer() {
        assertCreateGroupByContextWithoutGroupBy(new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateGroupByContextWithoutGroupBy(final SimpleSelectStatement simpleSelectStatement) {
        GroupByContext actualGroupByContext = new GroupByContextEngine().createGroupByContext(simpleSelectStatement);
        assertTrue(actualGroupByContext.getItems().isEmpty());
    }
    
    @Test
    void assertCreateGroupByContextWithGroupByForMySQL() {
        assertCreateGroupByContextWithGroupBy(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithGroupByForOracle() {
        assertCreateGroupByContextWithGroupBy(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithGroupByForPostgreSQL() {
        assertCreateGroupByContextWithGroupBy(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithGroupByForSQL92() {
        assertCreateGroupByContextWithGroupBy(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateGroupByContextWithGroupByForSQLServer() {
        assertCreateGroupByContextWithGroupBy(new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateGroupByContextWithGroupBy(final SimpleSelectStatement simpleSelectStatement) {
        OrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")), OrderDirection.ASC, NullsOrderType.LAST);
        OrderByItemSegment indexOrderByItemSegment1 = new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, NullsOrderType.LAST);
        OrderByItemSegment indexOrderByItemSegment2 = new IndexOrderByItemSegment(2, 3, 3, OrderDirection.ASC, NullsOrderType.LAST);
        GroupBySegment groupBySegment = new GroupBySegment(0, 10, Arrays.asList(columnOrderByItemSegment, indexOrderByItemSegment1, indexOrderByItemSegment2));
        simpleSelectStatement.setGroupBy(groupBySegment);
        GroupByContext actualGroupByContext = new GroupByContextEngine().createGroupByContext(simpleSelectStatement);
        OrderByItem expectedOrderByItem1 = new OrderByItem(columnOrderByItemSegment);
        OrderByItem expectedOrderByItem2 = new OrderByItem(indexOrderByItemSegment1);
        expectedOrderByItem2.setIndex(2);
        OrderByItem expectedOrderByItem3 = new OrderByItem(indexOrderByItemSegment2);
        expectedOrderByItem3.setIndex(3);
        assertThat(actualGroupByContext.getItems(), is(Arrays.asList(expectedOrderByItem1, expectedOrderByItem2, expectedOrderByItem3)));
    }
}
