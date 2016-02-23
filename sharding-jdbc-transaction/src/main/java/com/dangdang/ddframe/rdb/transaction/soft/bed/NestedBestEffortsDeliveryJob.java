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

package com.dangdang.ddframe.rdb.transaction.soft.bed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractIndividualThroughputDataFlowElasticJob;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransacationLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransacationLogStorageFactory;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 内嵌的最大努力送达型异步作业.
 * 
 * @author zhangliang
 */
@Slf4j
public class NestedBestEffortsDeliveryJob extends AbstractIndividualThroughputDataFlowElasticJob<TransactionLog> {
    
    // TODO elastic-job支持自定义属性注入
    @Setter
    private ShardingDataSource shardingDataSource;
    
    // TODO elastic-job支持自定义属性注入
    @Setter
    private SoftTransactionConfiguration transactionConfig;
    
    public NestedBestEffortsDeliveryJob() {
        try {
            shardingDataSource = getShardingDataSource();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        transactionConfig = new SoftTransactionConfiguration();
        transactionConfig.setNestedJob(true);
        transactionConfig.setTransactionLogDataSource(createTransactionLogDataSource());
    }
    
    private static ShardingDataSource getShardingDataSource() throws SQLException {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap());
        TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"), dataSourceRule);
        TableRule orderItemTableRule = new TableRule("t_order_item", Arrays.asList("t_order_item_0", "t_order_item_1"), dataSourceRule);
        ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule),
                Arrays.asList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
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
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private static DataSource createTransactionLogDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl("jdbc:mysql://localhost:3306/trans_log");
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    @Override
    public List<TransactionLog> fetchData(final JobExecutionMultipleShardingContext context) {
        TransacationLogStorage transacationLogStorage = TransacationLogStorageFactory.createTransacationLogStorageFactory(transactionConfig);
        return transacationLogStorage.findAllForLessThanMaxAsyncProcessTimes(context.getFetchDataCount());
    }
    
    @Override
    public boolean processData(final JobExecutionMultipleShardingContext context, final TransactionLog data) {
        TransacationLogStorage transacationLogStorage = TransacationLogStorageFactory.createTransacationLogStorageFactory(transactionConfig);
        try (
                Connection conn = shardingDataSource.getConnection().getConnection(data.getDataSource());
                PreparedStatement pstmt = conn.prepareStatement(data.getSql())) {
            for (int parameterIndex = 0; parameterIndex < data.getParameters().size(); parameterIndex++) {
                pstmt.setObject(parameterIndex + 1, data.getParameters().get(parameterIndex));
            }
            pstmt.executeUpdate();
        } catch (final SQLException ex) {
            transacationLogStorage.increaseAsyncDeliveryTryTimes(data.getId());
            log.error(String.format("Async delivery times %s error, max try times is %s", data.getAsyncDeliveryTryTimes() + 1, transactionConfig.getAsyncMaxDeliveryTryTimes()), ex);
            return false;
        }
        transacationLogStorage.remove(data.getId());
        return true;
    }
    
    @Override
    public boolean isStreamingProcess() {
        return false;
    }
}
