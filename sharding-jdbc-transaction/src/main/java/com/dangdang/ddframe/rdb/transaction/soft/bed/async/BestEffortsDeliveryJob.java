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

package com.dangdang.ddframe.rdb.transaction.soft.bed.async;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractIndividualThroughputDataFlowElasticJob;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionType;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 内嵌的最大努力送达型异步作业.
 * 
 * @author zhangliang
 */
@Slf4j
public class BestEffortsDeliveryJob extends AbstractIndividualThroughputDataFlowElasticJob<TransactionLog> {
    
    @Setter
    private SoftTransactionConfiguration transactionConfig;
    
    @Setter
    private TransactionLogStorage transactionLogStorage;
    
    @Override
    public List<TransactionLog> fetchData(final JobExecutionMultipleShardingContext context) {
        return transactionLogStorage.findEligibleTransactionLogs(context.getFetchDataCount(), SoftTransactionType.BestEffortsDelivery);
    }
    
    @Override
    public boolean processData(final JobExecutionMultipleShardingContext context, final TransactionLog data) {
        try (
                Connection conn = transactionConfig.getTargetDataSource().getConnection().getConnection(data.getDataSource());
                PreparedStatement preparedStatement = conn.prepareStatement(data.getSql())) {
            for (int parameterIndex = 0; parameterIndex < data.getParameters().size(); parameterIndex++) {
                preparedStatement.setObject(parameterIndex + 1, data.getParameters().get(parameterIndex));
            }
            preparedStatement.executeUpdate();
        } catch (final SQLException ex) {
            transactionLogStorage.increaseAsyncDeliveryTryTimes(data.getId());
            log.error(String.format("Async delivery times %s error, max try times is %s", data.getAsyncDeliveryTryTimes() + 1, transactionConfig.getAsyncMaxDeliveryTryTimes()), ex);
            return false;
        }
        transactionLogStorage.remove(data.getId());
        return true;
    }
    
    @Override
    public boolean isStreamingProcess() {
        return false;
    }
}
