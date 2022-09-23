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
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingInsertValuesTokenTest {
    
    private ShardingInsertValuesToken shardingInsertValuesToken;
    
    private RouteUnit routeUnit;
    
    @Before
    public void setup() {
        shardingInsertValuesToken = new ShardingInsertValuesToken(0, 2);
        RouteMapper routeMapper = new RouteMapper("logic_ds", "actual_ds");
        RouteMapper routeMapper1 = new RouteMapper("tbl", "tbl_0");
        RouteMapper routeMapper2 = new RouteMapper("tbl", "tbl_1");
        routeUnit = new RouteUnit(routeMapper, Arrays.asList(routeMapper1, routeMapper2));
        ExpressionSegment expressionSegment1 = new LiteralExpressionSegment(0, 0, "shardingsphere");
        ExpressionSegment expressionSegment2 = new LiteralExpressionSegment(0, 0, "test");
        List<ExpressionSegment> expressionSegment = new LinkedList<>();
        expressionSegment.add(expressionSegment1);
        expressionSegment.add(expressionSegment2);
        Collection<DataNode> dataNodes = new LinkedList<>();
        ShardingInsertValue shardingInsertValue = new ShardingInsertValue(expressionSegment, dataNodes);
        List<InsertValue> insertValues = shardingInsertValuesToken.getInsertValues();
        insertValues.add(shardingInsertValue);
    }
    
    @Test
    public void assertToString() {
        assertThat(shardingInsertValuesToken.toString(routeUnit), is("('shardingsphere', 'test')"));
    }
}
