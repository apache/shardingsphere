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

package com.dangdang.ddframe.rdb.transaction.soft;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.bed.BestEffortsDeliveryJobConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.bed.BestEffortsDeliveryJobFactory;

/**
 * 最大努力送达型异步作业启动入口.
 * 
 * @author zhangliang
 */
public final class AsyncJobMain {
    
    /**
     * 启动入口.
     * 
     * @param args 启动参数
     */
    // TODO 目前先写死，等开源了config模块之后再修改
    public static void main(final String[] args) throws SQLException {
        SoftTransactionConfiguration transactionConfig = new SoftTransactionConfiguration(getShardingDataSource());
        transactionConfig.setBestEffortsDeliveryJobConfiguration(new BestEffortsDeliveryJobConfiguration("localhost:2181"));
        transactionConfig.setTransactionLogDataSource(createTransactionLogDataSource());
        new BestEffortsDeliveryJobFactory(transactionConfig).init();
    }
    
    private static ShardingDataSource getShardingDataSource() {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap());
        TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"), dataSourceRule);
        TableRule orderItemTableRule = new TableRule("t_order_item", Arrays.asList("t_order_item_0", "t_order_item_1"), dataSourceRule);
        ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule), Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
                new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));
        return new ShardingDataSource(shardingRule);
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private static DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private static DataSource createTransactionLogDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/trans_log");
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
