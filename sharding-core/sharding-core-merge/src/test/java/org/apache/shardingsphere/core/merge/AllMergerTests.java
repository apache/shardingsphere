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

package org.apache.shardingsphere.core.merge;

import org.apache.shardingsphere.core.merge.dal.DALMergeEngineTest;
import org.apache.shardingsphere.core.merge.dal.show.ShowCreateTableMergedResultTest;
import org.apache.shardingsphere.core.merge.dal.show.ShowDatabasesMergedResultTest;
import org.apache.shardingsphere.core.merge.dal.show.ShowOtherMergedResultTest;
import org.apache.shardingsphere.core.merge.dal.show.ShowTablesMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.DQLMergeEngineTest;
import org.apache.shardingsphere.core.merge.dql.common.DecoratorMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.common.MemoryMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.common.MemoryQueryResultRowTest;
import org.apache.shardingsphere.core.merge.dql.common.StreamMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByMemoryMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByRowComparatorTest;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByStreamMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByValueTest;
import org.apache.shardingsphere.core.merge.dql.groupby.aggregation.AllAggregationTests;
import org.apache.shardingsphere.core.merge.dql.iterator.IteratorStreamMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.orderby.CompareUtilTest;
import org.apache.shardingsphere.core.merge.dql.orderby.OrderByStreamMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.orderby.OrderByValueTest;
import org.apache.shardingsphere.core.merge.dql.pagination.LimitDecoratorMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.pagination.RowNumberDecoratorMergedResultTest;
import org.apache.shardingsphere.core.merge.dql.pagination.TopAndRowNumberDecoratorMergedResultTest;
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
