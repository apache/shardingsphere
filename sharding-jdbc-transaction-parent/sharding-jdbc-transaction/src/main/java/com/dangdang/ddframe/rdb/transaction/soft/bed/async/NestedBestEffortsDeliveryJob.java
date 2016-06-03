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

package com.dangdang.ddframe.rdb.transaction.soft.bed.async;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractIndividualThroughputDataFlowElasticJob;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;

/**
 * 最大努力送达型异步作业.
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public class NestedBestEffortsDeliveryJob extends AbstractIndividualThroughputDataFlowElasticJob<TransactionLog> {
    
    @Setter
    private SoftTransactionConfiguration transactionConfig;
    
    @Setter
    private TransactionLogStorage transactionLogStorage;
    
    @Override
    public List<TransactionLog> fetchData(final JobExecutionMultipleShardingContext context) {
        return transactionLogStorage.findEligibleTransactionLogs(context.getFetchDataCount(), 
            transactionConfig.getBestEffortsDeliveryJobConfiguration().get().getAsyncMaxDeliveryTryTimes(), 
            transactionConfig.getBestEffortsDeliveryJobConfiguration().get().getAsyncMaxDeliveryTryDelayMillis());
    }
    
    @Override
    public boolean processData(final JobExecutionMultipleShardingContext context, final TransactionLog data) {
        try {
            return transactionLogStorage.processData(
                    transactionConfig.getTargetConnection(data.getDataSource()), data, transactionConfig.getBestEffortsDeliveryJobConfiguration().get().getAsyncMaxDeliveryTryTimes());
        } catch (final SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    @Override
    public boolean isStreamingProcess() {
        return false;
    }
}
