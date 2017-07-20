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

package com.dangdang.ddframe.rdb.sharding.routing;

import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDynamicRouteSqlTest extends AbstractBaseRouteSqlTest {
    
    protected void assertSingleTargetWithoutParameter(final List<ShardingValuePair> shardingValuePairs, final String originSql, final String targetDataSource, final String targetSQL) {
        assertMultipleTargetsWithoutParameter(shardingValuePairs, originSql, 1, Collections.singletonList(targetDataSource), Collections.singletonList(targetSQL));
    }
    
    protected void assertSingleTargetWithParameters(
            final List<ShardingValuePair> shardingValuePairs, final String originSql, final List<Object> parameters, final String targetDataSource, final String targetSQL) {
        assertMultipleTargetsWithParameters(shardingValuePairs, originSql, parameters, 1, Collections.singletonList(targetDataSource), Collections.singletonList(targetSQL));
    }
    
    protected void assertMultipleTargetsWithoutParameter(final List<ShardingValuePair> shardingValuePairs, final String originSql, final int expectedSize,
                                                     final Collection<String> targetDataSources, final Collection<String> targetSQLs) {
        try (HintManager hintManager = HintManager.getInstance()) {
            for (ShardingValuePair each : shardingValuePairs) {
                hintManager.addDatabaseShardingValue(each.logicTable, "order_id", each.shardingOperator, each.shardingValue);
                hintManager.addTableShardingValue(each.logicTable, "order_id", each.shardingOperator, each.shardingValue);
            }
            assertMultipleTargetsWithoutParameter(originSql, expectedSize, targetDataSources, targetSQLs);
        }
    }
    
    protected void assertMultipleTargetsWithParameters(final List<ShardingValuePair> shardingValuePairs, final String originSql, final List<Object> parameters, final int expectedSize, 
                                       final Collection<String> targetDataSources, final Collection<String> targetSQLs) {
        try (HintManager hintManager = HintManager.getInstance()) {
            for (ShardingValuePair each : shardingValuePairs) {
                hintManager.addDatabaseShardingValue(each.logicTable, "order_id", each.shardingOperator, each.shardingValue);
                hintManager.addTableShardingValue(each.logicTable, "order_id", each.shardingOperator, each.shardingValue);
            }
            assertMultipleTargetsWithParameters(originSql, parameters, expectedSize, targetDataSources, targetSQLs);
        }
    }
    
    protected static class ShardingValuePair {
    
        private final String logicTable;
    
        private final ShardingOperator shardingOperator;
    
        private final Integer[] shardingValue;
        
        protected ShardingValuePair(final String logicTable, final Integer... shardingValue) {
            this(logicTable, ShardingOperator.EQUAL, shardingValue);
        }
        
        protected ShardingValuePair(final String logicTable, final ShardingOperator shardingOperator, final Integer... shardingValue) {
            this.logicTable = logicTable;
            this.shardingOperator = shardingOperator;
            this.shardingValue = shardingValue;
        }
    }
}
