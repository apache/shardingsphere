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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupByContextEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateGroupByContextWithoutGroupBy() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        GroupByContext actualGroupByContext = new GroupByContextEngine().createGroupByContext(selectStatement);
        assertTrue(actualGroupByContext.getItems().isEmpty());
    }
    
    @Test
    void assertCreateGroupByContextWithGroupBy() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        OrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")), OrderDirection.ASC, NullsOrderType.LAST);
        OrderByItemSegment indexOrderByItemSegment1 = new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, NullsOrderType.LAST);
        OrderByItemSegment indexOrderByItemSegment2 = new IndexOrderByItemSegment(2, 3, 3, OrderDirection.ASC, NullsOrderType.LAST);
        GroupBySegment groupBySegment = new GroupBySegment(0, 10, Arrays.asList(columnOrderByItemSegment, indexOrderByItemSegment1, indexOrderByItemSegment2));
        selectStatement.setGroupBy(groupBySegment);
        GroupByContext actualGroupByContext = new GroupByContextEngine().createGroupByContext(selectStatement);
        OrderByItem expectedOrderByItem1 = new OrderByItem(columnOrderByItemSegment);
        OrderByItem expectedOrderByItem2 = new OrderByItem(indexOrderByItemSegment1);
        expectedOrderByItem2.setIndex(2);
        OrderByItem expectedOrderByItem3 = new OrderByItem(indexOrderByItemSegment2);
        expectedOrderByItem3.setIndex(3);
        assertThat(actualGroupByContext.getItems(), is(Arrays.asList(expectedOrderByItem1, expectedOrderByItem2, expectedOrderByItem3)));
    }
}
