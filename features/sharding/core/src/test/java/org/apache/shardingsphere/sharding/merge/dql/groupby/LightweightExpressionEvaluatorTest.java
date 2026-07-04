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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LightweightExpressionEvaluatorTest {
    
    @Test
    void assertEvaluateIfNullWithMemoryRowFallback() {
        FunctionSegment functionSegment = new FunctionSegment(0, 20, "IFNULL", "IFNULL(SUM(x), 0)");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(7, 12, AggregationType.SUM, "SUM(x)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(15, 15, 0);
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegment, literalSegment));
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        when(memoryRow.getCell(1)).thenReturn(null);
        
        Object actual = LightweightExpressionEvaluator.evaluate(functionSegment, Collections.singletonList(derivedAggr), memoryRow);
        assertThat(actual, is(0));
    }
    
    @Test
    void assertEvaluateIfNullWithStreamRowSuccess() {
        FunctionSegment functionSegment = new FunctionSegment(0, 20, "IFNULL", "IFNULL(SUM(x), 0)");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(7, 12, AggregationType.SUM, "SUM(x)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(15, 15, 0);
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegment, literalSegment));
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(2);
        
        Object actual = LightweightExpressionEvaluator.evaluate(functionSegment, Collections.singletonList(derivedAggr), Arrays.asList("other", 50));
        assertThat(actual, is(50));
    }
}
