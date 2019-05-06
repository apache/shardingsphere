/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.merger;

import io.shardingsphere.core.merger.dal.DALMergeEngineTest;
import io.shardingsphere.core.merger.dal.show.ShowCreateTableMergedResultTest;
import io.shardingsphere.core.merger.dal.show.ShowDatabasesMergedResultTest;
import io.shardingsphere.core.merger.dal.show.ShowOtherMergedResultTest;
import io.shardingsphere.core.merger.dal.show.ShowTablesMergedResultTest;
import io.shardingsphere.core.merger.dql.DQLMergeEngineTest;
import io.shardingsphere.core.merger.dql.common.DecoratorMergedResultTest;
import io.shardingsphere.core.merger.dql.common.MemoryMergedResultTest;
import io.shardingsphere.core.merger.dql.common.MemoryQueryResultRowTest;
import io.shardingsphere.core.merger.dql.common.StreamMergedResultTest;
import io.shardingsphere.core.merger.dql.groupby.GroupByMemoryMergedResultTest;
import io.shardingsphere.core.merger.dql.groupby.GroupByRowComparatorTest;
import io.shardingsphere.core.merger.dql.groupby.GroupByStreamMergedResultTest;
import io.shardingsphere.core.merger.dql.groupby.GroupByValueTest;
import io.shardingsphere.core.merger.dql.groupby.aggregation.AllAggregationTests;
import io.shardingsphere.core.merger.dql.iterator.IteratorStreamMergedResultTest;
import io.shardingsphere.core.merger.dql.orderby.CompareUtilTest;
import io.shardingsphere.core.merger.dql.orderby.OrderByStreamMergedResultTest;
import io.shardingsphere.core.merger.dql.orderby.OrderByValueTest;
import io.shardingsphere.core.merger.dql.pagination.LimitDecoratorMergedResultTest;
import io.shardingsphere.core.merger.dql.pagination.RowNumberDecoratorMergedResultTest;
import io.shardingsphere.core.merger.dql.pagination.TopAndRowNumberDecoratorMergedResultTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
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
public final class AllMergerTests {
}
