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

import com.dangdang.ddframe.rdb.sharding.merger.common.DecoratorResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.common.MemoryResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.common.MemoryResultSetRowTest;
import com.dangdang.ddframe.rdb.sharding.merger.common.StreamResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByMemoryResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByRowComparatorTest;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByStreamResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByValueTest;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AllAggregationTests;
import com.dangdang.ddframe.rdb.sharding.merger.iterator.IteratorStreamResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.limit.LimitDecoratorResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByStreamResultSetMergerTest;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByValueTest;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MergeEngineTest.class, 
        StreamResultSetMergerTest.class, 
        MemoryResultSetMergerTest.class, 
        DecoratorResultSetMergerTest.class, 
        MemoryResultSetRowTest.class, 
        IteratorStreamResultSetMergerTest.class, 
        OrderByValueTest.class, 
        OrderByStreamResultSetMergerTest.class, 
        GroupByValueTest.class, 
        GroupByRowComparatorTest.class, 
        GroupByStreamResultSetMergerTest.class, 
        GroupByMemoryResultSetMergerTest.class, 
        AllAggregationTests.class, 
        LimitDecoratorResultSetMergerTest.class, 
        ResultSetUtilTest.class
    })
public class AllMergerTests {
}
