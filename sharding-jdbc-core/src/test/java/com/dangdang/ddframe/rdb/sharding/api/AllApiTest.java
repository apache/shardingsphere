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

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.props.ShardingPropertiesConstantTest;
import com.dangdang.ddframe.rdb.sharding.api.props.ShardingPropertiesTest;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRuleTest;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRuleTest;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRuleTest;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRuleTest;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.ShardingStrategyTest;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategyTest;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithmTest;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithmTest;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategyTest;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseTypeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DatabaseTypeTest.class, 
    ShardingPropertiesTest.class,
    ShardingPropertiesConstantTest.class, 
    ShardingDataSourceTest.class, 
    ShardingValueTest.class,
    DataSourceRuleTest.class, 
    ShardingRuleTest.class, 
    TableRuleTest.class, 
    BindingTableRuleTest.class, 
    ShardingStrategyTest.class, 
    DatabaseShardingStrategyTest.class, 
    NoneDatabaseShardingAlgorithmTest.class, 
    TableShardingStrategyTest.class, 
    NoneTableShardingAlgorithmTest.class,
    HintManagerTest.class
    })
public class AllApiTest {
}
