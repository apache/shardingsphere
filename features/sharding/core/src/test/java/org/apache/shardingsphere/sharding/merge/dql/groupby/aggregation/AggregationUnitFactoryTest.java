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

package org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation;

import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class AggregationUnitFactoryTest {
    
    @Test
    void assertCreateComparableAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.MIN, false, null), instanceOf(ComparableAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.MAX, false, null), instanceOf(ComparableAggregationUnit.class));
    }
    
    @Test
    void assertCreateAccumulationAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.SUM, false, null), instanceOf(AccumulationAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.COUNT, false, null), instanceOf(AccumulationAggregationUnit.class));
    }
    
    @Test
    void assertCreateAverageAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.AVG, false, null), instanceOf(AverageAggregationUnit.class));
    }
    
    @Test
    void assertCreateDistinctSumAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.SUM, true, null), instanceOf(DistinctSumAggregationUnit.class));
    }
    
    @Test
    void assertCreateDistinctCountAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.COUNT, true, null), instanceOf(DistinctCountAggregationUnit.class));
    }
    
    @Test
    void assertCreateDistinctAverageAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.AVG, true, null), instanceOf(DistinctAverageAggregationUnit.class));
    }
    
    @Test
    void assertCreateBitXorAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.BIT_XOR, false, null), instanceOf(BitXorAggregationUnit.class));
    }
    
    @Test
    void assertGroupConcatAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, false, null), instanceOf(GroupConcatAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, false, " "), instanceOf(GroupConcatAggregationUnit.class));
    }
    
    @Test
    void assertDistinctGroupConcatAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, true, null), instanceOf(DistinctGroupConcatAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, true, " "), instanceOf(DistinctGroupConcatAggregationUnit.class));
    }
}
