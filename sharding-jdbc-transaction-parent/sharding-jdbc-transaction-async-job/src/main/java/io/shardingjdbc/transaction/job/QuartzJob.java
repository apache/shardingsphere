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

package io.shardingjdbc.transaction.job;

import io.shardingjdbc.transaction.exception.TransactionCompensationException;
import io.shardingjdbc.transaction.storage.TransactionLog;
import io.shardingjdbc.transaction.storage.TransactionLogStorage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Soft transaction job base quartz.
 *
 * @author wangkai
 */
@Slf4j
public class QuartzJob implements Job {

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        QuartzJobConfiguration quartzJobConfiguration = (QuartzJobConfiguration)jobExecutionContext.getJobDetail().getJobDataMap().get("quartzJobConfiguration");
        TransactionLogStorage transactionLogStorage = (TransactionLogStorage)jobExecutionContext.getJobDetail().getJobDataMap().get("transactionLogStorage");
        List<TransactionLog> TransactionLogList = transactionLogStorage.findEligibleTransactionLogs(quartzJobConfiguration.getJobConfig().getTransactionLogFetchDataCount(),
                quartzJobConfiguration.getJobConfig().getMaxDeliveryTryTimes(), quartzJobConfiguration.getJobConfig().getMaxDeliveryTryDelayMillis());
        for (TransactionLog data : TransactionLogList) {
            try (Connection conn = quartzJobConfiguration.getTargetDataSource(data.getDataSource()).getConnection()) {
                transactionLogStorage.processData(conn, data, quartzJobConfiguration.getJobConfig().getMaxDeliveryTryTimes());
            } catch (final SQLException | TransactionCompensationException ex) {
                log.error(String.format("Async delivery times %s error, max try times is %s, exception is %s", data.getAsyncDeliveryTryTimes() + 1,
                        quartzJobConfiguration.getJobConfig().getMaxDeliveryTryTimes(), ex.getMessage()));
            }
        }
    }
}
