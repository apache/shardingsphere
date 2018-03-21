/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.merger;

import io.shardingjdbc.core.merger.dql.DQLMergeEngineTest;
import io.shardingjdbc.core.merger.dql.common.DecoratorResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.common.MemoryResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.common.MemoryResultSetRowTest;
import io.shardingjdbc.core.merger.dql.common.StreamResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByMemoryResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByRowComparatorTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByStreamResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByValueTest;
import io.shardingjdbc.core.merger.dql.groupby.aggregation.AllAggregationTests;
import io.shardingjdbc.core.merger.dql.iterator.IteratorStreamResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.orderby.CompareUtilTest;
import io.shardingjdbc.core.merger.dql.orderby.OrderByStreamResultSetMergerTest;
import io.shardingjdbc.core.merger.dql.orderby.OrderByValueTest;
import io.shardingjdbc.core.merger.dql.pagination.LimitDecoratorResultSetMergerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DQLMergeEngineTest.class, 
        StreamResultSetMergerTest.class, 
        MemoryResultSetMergerTest.class, 
        DecoratorResultSetMergerTest.class, 
        MemoryResultSetRowTest.class, 
        IteratorStreamResultSetMergerTest.class, 
        OrderByValueTest.class, 
        OrderByStreamResultSetMergerTest.class, 
        CompareUtilTest.class, 
        GroupByValueTest.class, 
        GroupByRowComparatorTest.class, 
        GroupByStreamResultSetMergerTest.class, 
        GroupByMemoryResultSetMergerTest.class, 
        AllAggregationTests.class, 
        LimitDecoratorResultSetMergerTest.class 
    })
public class AllMergerTests {
}
