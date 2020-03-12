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

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AverageAggregationUnitTest {
    
    @Test
    public void assertAvgAggregation() {
        AverageAggregationUnit avgAggregationUnit = new AverageAggregationUnit();
        avgAggregationUnit.merge(null);
        avgAggregationUnit.merge(Arrays.asList(null, null));
        avgAggregationUnit.merge(Arrays.asList(1, null));
        avgAggregationUnit.merge(Arrays.asList(10, 50));
        avgAggregationUnit.merge(Arrays.asList(10, 20));
        avgAggregationUnit.merge(Arrays.asList(5, 40));
        assertThat(avgAggregationUnit.getResult(), is(new BigDecimal("4.4000")));
    }
    
    @Test
    public void assertDivideZero() {
        AverageAggregationUnit avgAggregationUnit = new AverageAggregationUnit();
        avgAggregationUnit.merge(Arrays.asList(0, 50));
        avgAggregationUnit.merge(Arrays.asList(0, 20));
        avgAggregationUnit.merge(Arrays.asList(0, 40));
        assertThat(avgAggregationUnit.getResult(), is(new BigDecimal(0)));
    }
}
