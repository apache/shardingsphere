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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class AggregationUnitFactoryTest {
    
    @Test
    void assertCreateComparableAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.MIN, false, null), isA(ComparableAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.MAX, false, null), isA(ComparableAggregationUnit.class));
    }
    
    @Test
    void assertCreateAccumulationAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.SUM, false, null), isA(AccumulationAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.COUNT, false, null), isA(AccumulationAggregationUnit.class));
    }
    
    @Test
    void assertCreateAverageAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.AVG, false, null), isA(AverageAggregationUnit.class));
    }
    
    @Test
    void assertCreateDistinctSumAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.SUM, true, null), isA(DistinctSumAggregationUnit.class));
    }
    
    @Test
    void assertCreateDistinctCountAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.COUNT, true, null), isA(DistinctCountAggregationUnit.class));
    }
    
    @Test
    void assertCreateDistinctAverageAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.AVG, true, null), isA(DistinctAverageAggregationUnit.class));
    }
    
    @Test
    void assertCreateBitXorAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.BIT_XOR, false, null), isA(BitXorAggregationUnit.class));
    }
    
    @Test
    void assertGroupConcatAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, false, null), isA(GroupConcatAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, false, " "), isA(GroupConcatAggregationUnit.class));
    }
    
    @Test
    void assertDistinctGroupConcatAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, true, null), isA(DistinctGroupConcatAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.GROUP_CONCAT, true, " "), isA(DistinctGroupConcatAggregationUnit.class));
    }
}
