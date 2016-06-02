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

package com.dangdang.ddframe.rdb.integrate.nullable;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.fixture.MultipleKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractShardingNullableDBUnitTest extends AbstractDBUnitTest {
    
    private final String dataSourceName = "dataSource_%s";
    
    @Override
    protected List<String> getSchemaFiles() {
        return Arrays.asList(
                "integrate/schema/nullable/nullable_0.sql",
                "integrate/schema/nullable/nullable_1.sql",
                "integrate/schema/nullable/nullable_2.sql",
                "integrate/schema/nullable/nullable_3.sql",
                "integrate/schema/nullable/nullable_4.sql",
                "integrate/schema/nullable/nullable_5.sql",
                "integrate/schema/nullable/nullable_6.sql",
                "integrate/schema/nullable/nullable_7.sql",
                "integrate/schema/nullable/nullable_8.sql",
                "integrate/schema/nullable/nullable_9.sql");
    }
    
    @Override
    protected List<String> getDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/nullable/init/nullable_0.xml",
                "integrate/dataset/nullable/init/nullable_1.xml",
                "integrate/dataset/nullable/init/nullable_2.xml",
                "integrate/dataset/nullable/init/nullable_3.xml",
                "integrate/dataset/nullable/init/nullable_4.xml",
                "integrate/dataset/nullable/init/nullable_5.xml",
                "integrate/dataset/nullable/init/nullable_6.xml",
                "integrate/dataset/nullable/init/nullable_7.xml",
                "integrate/dataset/nullable/init/nullable_8.xml",
                "integrate/dataset/nullable/init/nullable_9.xml");
    }
    
    protected final ShardingDataSource getShardingDataSource() {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap(dataSourceName));
        
        TableRule orderTableRule = TableRule.builder("t_order").dataSourceRule(dataSourceRule).build();
        ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(orderTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Collections.singletonList(orderTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy(Collections.singletonList("user_id"), new MultipleKeysModuloDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy(Collections.singletonList("order_id"), new NoneTableShardingAlgorithm())).build();
        return new ShardingDataSource(shardingRule);
    }
}
