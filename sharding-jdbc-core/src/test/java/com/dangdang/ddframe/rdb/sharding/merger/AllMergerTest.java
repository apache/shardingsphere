/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AccumulationAggregationUnitTest;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AvgAggregationUnitTest;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.ComparableAggregationUnitTest;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.NullableAggregationResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.ResultSetAggregationValueTest;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByValueTest;
import com.dangdang.ddframe.rdb.sharding.merger.iterator.IteratorResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByValueTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ResultSetUtilTest.class, 
    ResultSetQueryIndexTest.class, 
    IteratorResultSetTest.class, 
    OrderByResultSetTest.class, 
    OrderByValueTest.class, 
    AggregationResultSetTest.class, 
    ResultSetAggregationValueTest.class, 
    AccumulationAggregationUnitTest.class, 
    ComparableAggregationUnitTest.class, 
    AvgAggregationUnitTest.class, 
    GroupByValueTest.class, 
    NullableAggregationResultSetTest.class
    })
public class AllMergerTest {
}
