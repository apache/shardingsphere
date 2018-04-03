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

import io.shardingjdbc.core.merger.dal.DALMergeEngineTest;
import io.shardingjdbc.core.merger.dal.show.ShowCreateTableMergedResultTest;
import io.shardingjdbc.core.merger.dal.show.ShowDatabasesMergedResultTest;
import io.shardingjdbc.core.merger.dal.show.ShowOtherMergedResultTest;
import io.shardingjdbc.core.merger.dal.show.ShowTablesMergedResultTest;
import io.shardingjdbc.core.merger.dql.DQLMergeEngineTest;
import io.shardingjdbc.core.merger.dql.common.DecoratorMergedResultTest;
import io.shardingjdbc.core.merger.dql.common.MemoryMergedResultTest;
import io.shardingjdbc.core.merger.dql.common.MemoryQueryResultRowTest;
import io.shardingjdbc.core.merger.dql.common.StreamMergedResultTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByMemoryMergedResultTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByRowComparatorTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByStreamMergedResultTest;
import io.shardingjdbc.core.merger.dql.groupby.GroupByValueTest;
import io.shardingjdbc.core.merger.dql.groupby.aggregation.AllAggregationTests;
import io.shardingjdbc.core.merger.dql.iterator.IteratorStreamMergedResultTest;
import io.shardingjdbc.core.merger.dql.orderby.CompareUtilTest;
import io.shardingjdbc.core.merger.dql.orderby.OrderByStreamMergedResultTest;
import io.shardingjdbc.core.merger.dql.orderby.OrderByValueTest;
import io.shardingjdbc.core.merger.dql.pagination.LimitDecoratorMergedResultTest;
import io.shardingjdbc.core.merger.dql.pagination.RowNumberDecoratorMergedResultTest;
import io.shardingjdbc.core.merger.dql.pagination.TopAndRowNumberDecoratorMergedResultTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DQLMergeEngineTest.class, 
        StreamMergedResultTest.class, 
        MemoryMergedResultTest.class, 
        DecoratorMergedResultTest.class, 
        MemoryQueryResultRowTest.class, 
        IteratorStreamMergedResultTest.class, 
        OrderByValueTest.class, 
        OrderByStreamMergedResultTest.class, 
        CompareUtilTest.class, 
        GroupByValueTest.class, 
        GroupByRowComparatorTest.class, 
        GroupByStreamMergedResultTest.class, 
        GroupByMemoryMergedResultTest.class, 
        AllAggregationTests.class, 
        LimitDecoratorMergedResultTest.class,
        RowNumberDecoratorMergedResultTest.class,
        TopAndRowNumberDecoratorMergedResultTest.class,
        DALMergeEngineTest.class,
        ShowCreateTableMergedResultTest.class,
        ShowDatabasesMergedResultTest.class,
        ShowOtherMergedResultTest.class,
        ShowTablesMergedResultTest.class,
        MergeEngineFactoryTest.class
    })
public class AllMergerTests {
}
