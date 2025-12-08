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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingInValuesTokenTest {
    
    @Test
    void assertToStringWithRouteUnit() {
        ShardingInValuesToken token = createInValuesToken();
        RouteUnit routeUnit = createRouteUnit();
        assertThat(token.toString(routeUnit), is("(100, 101)"));
    }
    
    @Test
    void assertToStringWithoutRouteUnit() {
        ShardingInValuesToken token = createInValuesTokenWithEmptyDataNodes();
        assertThat(token.toString(), is("(100, 101)"));
    }
    
    @Test
    void assertToStringWithNullRouteUnitAndNonEmptyDataNodes() {
        ShardingInValuesToken token = createInValuesToken();
        assertThat(token.toString(null), is("(100, 101)"));
    }
    
    @Test
    void assertToStringWithFilteredValues() {
        ShardingInValuesToken token = new ShardingInValuesToken(0, 10);
        DataNode dataNode1 = new DataNode("ds_0", (String) null, "t_user_0");
        DataNode dataNode2 = new DataNode("ds_1", (String) null, "t_user_1");
        token.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(0, 3, 100), Collections.singleton(dataNode1)));
        token.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(5, 8, 101), Collections.singleton(dataNode2)));
        RouteUnit routeUnit = createRouteUnit();
        assertThat(token.toString(routeUnit), is("(100)"));
    }
    
    @Test
    void assertToStringWithEmptyInValueItems() {
        ShardingInValuesToken token = new ShardingInValuesToken(0, 10);
        assertThat(token.toString(), is("(NULL)"));
    }
    
    @Test
    void assertToStringWithAllValuesFiltered() {
        ShardingInValuesToken token = new ShardingInValuesToken(0, 10);
        DataNode dataNode = new DataNode("ds_other", (String) null, "t_other");
        token.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(0, 3, 100), Collections.singleton(dataNode)));
        RouteUnit routeUnit = createRouteUnit();
        assertThat(token.toString(routeUnit), is("(NULL)"));
    }
    
    @Test
    void assertGetStopIndex() {
        ShardingInValuesToken token = new ShardingInValuesToken(5, 15);
        assertThat(token.getStopIndex(), is(15));
    }
    
    private ShardingInValuesToken createInValuesToken() {
        ShardingInValuesToken result = new ShardingInValuesToken(0, 10);
        DataNode dataNode = new DataNode("ds_0", (String) null, "t_user_0");
        result.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(0, 3, 100), Collections.singleton(dataNode)));
        result.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(5, 8, 101), Collections.singleton(dataNode)));
        return result;
    }
    
    private ShardingInValuesToken createInValuesTokenWithEmptyDataNodes() {
        ShardingInValuesToken result = new ShardingInValuesToken(0, 10);
        result.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(0, 3, 100), Collections.emptyList()));
        result.getInValueItems().add(new ShardingInValueItem(new LiteralExpressionSegment(5, 8, 101), Collections.emptyList()));
        return result;
    }
    
    private RouteUnit createRouteUnit() {
        RouteMapper dataSourceMapper = new RouteMapper("ds_0", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_user", "t_user_0");
        return new RouteUnit(dataSourceMapper, Arrays.asList(tableMapper));
    }
}
