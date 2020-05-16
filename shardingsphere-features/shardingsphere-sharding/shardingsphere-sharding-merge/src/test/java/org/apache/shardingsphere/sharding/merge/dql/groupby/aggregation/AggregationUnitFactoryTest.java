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

import org.apache.shardingsphere.sql.parser.sql.constant.AggregationType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class AggregationUnitFactoryTest {
    
    @Test
    public void assertCreateComparableAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.MIN, false), instanceOf(ComparableAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.MAX, false), instanceOf(ComparableAggregationUnit.class));
    }
    
    @Test
    public void assertCreateAccumulationAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.SUM, false), instanceOf(AccumulationAggregationUnit.class));
        assertThat(AggregationUnitFactory.create(AggregationType.COUNT, false), instanceOf(AccumulationAggregationUnit.class));
    }
    
    @Test
    public void assertCreateAverageAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.AVG, false), instanceOf(AverageAggregationUnit.class));
    }
    
    @Test
    public void assertCreateDistinctSumAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.SUM, true), instanceOf(DistinctSumAggregationUnit.class));
    }
    
    @Test
    public void assertCreateDistinctCountAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.COUNT, true), instanceOf(DistinctCountAggregationUnit.class));
    }
    
    @Test
    public void assertCreateDistinctAverageAggregationUnit() {
        assertThat(AggregationUnitFactory.create(AggregationType.AVG, true), instanceOf(DistinctAverageAggregationUnit.class));
    }
}
