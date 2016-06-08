/*
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

import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.AggregationResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.NullableAggregationResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.OrderByResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation.AccumulationAggregationUnitTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation.AggregationUnitFactoryTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation.AverageAggregationUnitTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation.ComparableAggregationUnitTest;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer.IteratorResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.MemoryOrderByResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.MemoryResultSetTest;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.GroupByResultSetRowTest;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.OrderByResultSetRowTest;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.ResultSetRowTest;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ShardingResultSetsTest.class, 
    ResultSetMergeContextTest.class, 
    IteratorResultSetTest.class, 
    OrderByResultSetTest.class, 
    MemoryOrderByResultSetTest.class, 
    AggregationResultSetTest.class, 
    NullableAggregationResultSetTest.class, 
    ResultSetRowTest.class, 
    OrderByResultSetRowTest.class, 
    GroupByResultSetRowTest.class, 
    AggregationUnitFactoryTest.class, 
    ComparableAggregationUnitTest.class, 
    AccumulationAggregationUnitTest.class, 
    AverageAggregationUnitTest.class, 
    UnsupportedOperationDerivedColumnResultSetTest.class, 
    MemoryResultSetTest.class, 
    ResultSetUtilTest.class
    })
public class AllMergerTests {
}
