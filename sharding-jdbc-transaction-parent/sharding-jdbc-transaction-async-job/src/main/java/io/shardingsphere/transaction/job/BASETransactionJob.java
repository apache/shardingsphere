/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.job;

import io.shardingsphere.transaction.exception.TransactionCompensationException;
import io.shardingsphere.transaction.storage.TransactionLog;
import io.shardingsphere.transaction.storage.TransactionLogStorage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * B.A.S.E transaction job.
 *
 * @author wangkai
 */
@Slf4j
public final class BASETransactionJob implements Job {
    
    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        BASETransactionJobConfiguration baseTransactionJobConfiguration = (BASETransactionJobConfiguration) jobExecutionContext.getJobDetail().getJobDataMap().get("baseTransactionJobConfiguration");
        TransactionLogStorage transactionLogStorage = (TransactionLogStorage) jobExecutionContext.getJobDetail().getJobDataMap().get("transactionLogStorage");
        List<TransactionLog> transactionLogList = transactionLogStorage.findEligibleTransactionLogs(baseTransactionJobConfiguration.getJobConfig().getTransactionLogFetchDataCount(),
                baseTransactionJobConfiguration.getJobConfig().getMaxDeliveryTryTimes(), baseTransactionJobConfiguration.getJobConfig().getMaxDeliveryTryDelayMillis());
        if (null == transactionLogList) {
            return;
        }
        for (TransactionLog each : transactionLogList) {
            try (Connection conn = baseTransactionJobConfiguration.getTargetDataSource(each.getDataSource()).getConnection()) {
                transactionLogStorage.processData(conn, each, baseTransactionJobConfiguration.getJobConfig().getMaxDeliveryTryTimes());
            } catch (final SQLException | TransactionCompensationException ex) {
                log.error(String.format("Async delivery times %s error, max try times is %s, exception is %s", each.getAsyncDeliveryTryTimes() + 1,
                        baseTransactionJobConfiguration.getJobConfig().getMaxDeliveryTryTimes(), ex.getMessage()));
            }
        }
    }
}
