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

package com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic;

import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyDynamicModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DynamicShardingBothHelper {
    
    public static ShardingDataSource getShardingDataSource(final Map<String, DataSource> dataSourceMap) {
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule orderTableRule = new TableRule("t_order", dataSourceRule);
        TableRule orderItemTableRule = new TableRule("t_order_item", dataSourceRule);
        ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule),
                Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
                new DatabaseShardingStrategy("user_id", new SingleKeyModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new SingleKeyDynamicModuloTableShardingAlgorithm("t_order_")));
        return new ShardingDataSource(shardingRule);
    }
}
