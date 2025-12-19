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
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShardingInsertValuesTokenTest {
    
    @Test
    void assertToStringForInsertValue() {
        assertThat(createInsertValuesToken().toString(createRouteUnit()), is("('foo', 'bar')"));
    }
    
    private ShardingInsertValuesToken createInsertValuesToken() {
        ShardingInsertValuesToken result = new ShardingInsertValuesToken(0, 2);
        Collection<DataNode> dataNodes = Collections.singleton(new DataNode("foo_ds", (String) null, "tbl_0"));
        List<ExpressionSegment> values = Arrays.asList(new LiteralExpressionSegment(0, 0, "foo"), new LiteralExpressionSegment(0, 0, "bar"));
        result.getInsertValues().add(new ShardingInsertValue(values, dataNodes));
        return result;
    }
    
    private RouteUnit createRouteUnit() {
        RouteMapper routeMapper = new RouteMapper("foo_ds", "actual_ds");
        RouteMapper routeMapper1 = new RouteMapper("tbl", "tbl_0");
        RouteMapper routeMapper2 = new RouteMapper("tbl", "tbl_1");
        return new RouteUnit(routeMapper, Arrays.asList(routeMapper1, routeMapper2));
    }
    
    @Test
    void assertToStringForMultipleInsertValues() {
        assertThat(createMultipleInsertValuesToken().toString(), is("('foo', 'bar'), ('foo', 'bar')"));
    }
    
    private ShardingInsertValuesToken createMultipleInsertValuesToken() {
        ShardingInsertValuesToken result = new ShardingInsertValuesToken(0, 2);
        Collection<DataNode> dataNodes = Collections.singleton(new DataNode("foo_ds", (String) null, "tbl_0"));
        List<ExpressionSegment> values = Arrays.asList(new LiteralExpressionSegment(0, 0, "foo"), new LiteralExpressionSegment(0, 0, "bar"));
        result.getInsertValues().add(new ShardingInsertValue(values, dataNodes));
        result.getInsertValues().add(new ShardingInsertValue(values, dataNodes));
        return result;
    }
    
    @Test
    void assertToStringWithEmptyInsertValues() {
        ShardingInsertValuesToken result = new ShardingInsertValuesToken(0, 2);
        Collection<DataNode> dataNodes = Collections.singleton(new DataNode("foo_ds", (String) null, "tbl_0"));
        List<ExpressionSegment> values = Collections.emptyList();
        assertThrows(UnsupportedSQLOperationException.class, () -> result.getInsertValues().add(new ShardingInsertValue(values, dataNodes)));
    }
}
