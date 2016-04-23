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

package com.dangdang.ddframe.rdb.integrate.dbtbl;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractShardingBothDataBasesAndTablesDBUnitTest extends AbstractDBUnitTest {
    
    private final String dataSourceName = "dataSource_%s";
    
    @Override
    protected List<String> getSchemaFiles() {
        return Arrays.asList(
                "integrate/schema/dbtbl/dbtbl_0.sql", 
                "integrate/schema/dbtbl/dbtbl_1.sql", 
                "integrate/schema/dbtbl/dbtbl_2.sql", 
                "integrate/schema/dbtbl/dbtbl_3.sql", 
                "integrate/schema/dbtbl/dbtbl_4.sql", 
                "integrate/schema/dbtbl/dbtbl_5.sql", 
                "integrate/schema/dbtbl/dbtbl_6.sql", 
                "integrate/schema/dbtbl/dbtbl_7.sql", 
                "integrate/schema/dbtbl/dbtbl_8.sql", 
                "integrate/schema/dbtbl/dbtbl_9.sql");
    }
    
    @Override
    protected List<String> getDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/dbtbl/init/dbtbl_0.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_1.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_2.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_3.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_4.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_5.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_6.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_7.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_8.xml", 
                "integrate/dataset/dbtbl/init/dbtbl_9.xml");
    }
    
    protected final ShardingDataSource getShardingDataSource() {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap(dataSourceName));
        TableRule orderTableRule = new TableRule("t_order", Arrays.asList(
                "t_order_0", 
                "t_order_1", 
                "t_order_2", 
                "t_order_3", 
                "t_order_4", 
                "t_order_5", 
                "t_order_6", 
                "t_order_7", 
                "t_order_8", 
                "t_order_9"), dataSourceRule);
        TableRule orderItemTableRule = new TableRule("t_order_item", Arrays.asList(
                "t_order_item_0", 
                "t_order_item_1", 
                "t_order_item_2", 
                "t_order_item_3", 
                "t_order_item_4", 
                "t_order_item_5", 
                "t_order_item_6", 
                "t_order_item_7", 
                "t_order_item_8", 
                "t_order_item_9"), dataSourceRule);
        ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule), 
                Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
                new DatabaseShardingStrategy("user_id", new SingleKeyModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new SingleKeyModuloTableShardingAlgorithm()));
        return new ShardingDataSource(shardingRule);
    }
}
