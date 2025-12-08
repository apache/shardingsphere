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
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingInValueItemTest {
    
    @Test
    void assertToStringWithParameterMarkerQuestion() {
        ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(0, 1, 0, ParameterMarkerType.QUESTION);
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.emptyList());
        assertThat(item.toString(), is("?"));
    }
    
    @Test
    void assertToStringWithParameterMarkerDollar() {
        ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(0, 1, 0, ParameterMarkerType.DOLLAR);
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.emptyList());
        assertThat(item.toString(), is("$1"));
    }
    
    @Test
    void assertToStringWithLiteralStringValue() {
        LiteralExpressionSegment segment = new LiteralExpressionSegment(0, 5, "test");
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.emptyList());
        assertThat(item.toString(), is("'test'"));
    }
    
    @Test
    void assertToStringWithLiteralIntegerValue() {
        LiteralExpressionSegment segment = new LiteralExpressionSegment(0, 3, 100);
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.emptyList());
        assertThat(item.toString(), is("100"));
    }
    
    @Test
    void assertToStringWithLiteralNullValue() {
        LiteralExpressionSegment segment = new LiteralExpressionSegment(0, 3, null);
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.emptyList());
        assertThat(item.toString(), is("NULL"));
    }
    
    @Test
    void assertToStringWithOtherExpressionSegment() {
        ExpressionSegment segment = mock(ExpressionSegment.class);
        when(segment.getText()).thenReturn("col + 1");
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.emptyList());
        assertThat(item.toString(), is("col + 1"));
    }
    
    @Test
    void assertGetDataNodes() {
        LiteralExpressionSegment segment = new LiteralExpressionSegment(0, 3, 100);
        DataNode dataNode = new DataNode("ds_0", (String) null, "t_user_0");
        ShardingInValueItem item = new ShardingInValueItem(segment, Collections.singleton(dataNode));
        assertThat(item.getDataNodes().size(), is(1));
        assertThat(item.getDataNodes().iterator().next(), is(dataNode));
    }
}
