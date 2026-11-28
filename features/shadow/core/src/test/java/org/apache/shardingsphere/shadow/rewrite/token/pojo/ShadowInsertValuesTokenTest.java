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

package org.apache.shardingsphere.shadow.rewrite.token.pojo;

import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShadowInsertValuesTokenTest {
    
    @Test
    void assertToStringWithRouteUnit() {
        ShadowInsertValuesToken token = createToken();
        assertThat(token.toString(new RouteUnit(new RouteMapper("logic_ds", "ds_0"), Collections.emptyList())), is("(1, 'foo')"));
        assertThat(token.toString(new RouteUnit(new RouteMapper("logic_ds", "ds_1"), Collections.emptyList())), is("(2, 'bar')"));
    }
    
    @Test
    void assertToStringWithoutRouteUnit() {
        ShadowInsertValuesToken token = createToken();
        assertThat(token.toString(), is("(1, 'foo'), (2, 'bar')"));
        assertThat(token.toString(new RouteUnit(new RouteMapper("logic_ds", "unknown_ds"), Collections.emptyList())), is("(1, 'foo'), (2, 'bar')"));
    }
    
    private ShadowInsertValuesToken createToken() {
        Map<String, Collection<Integer>> dataSourceInsertValueIndexes = new LinkedHashMap<>(2, 1F);
        dataSourceInsertValueIndexes.put("ds_0", Collections.singletonList(0));
        dataSourceInsertValueIndexes.put("ds_1", Collections.singletonList(1));
        ShadowInsertValuesToken result = new ShadowInsertValuesToken(0, 30, dataSourceInsertValueIndexes);
        result.getInsertValues().addAll(Arrays.asList(createInsertValue(1, "foo"), createInsertValue(2, "bar")));
        return result;
    }
    
    private InsertValue createInsertValue(final Object orderId, final String cipher) {
        return new InsertValue(Arrays.asList(new LiteralExpressionSegment(0, 0, orderId), new LiteralExpressionSegment(0, 0, cipher)));
    }
}
