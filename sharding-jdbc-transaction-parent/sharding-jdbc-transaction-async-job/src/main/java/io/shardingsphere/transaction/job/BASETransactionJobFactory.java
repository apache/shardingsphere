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

import io.shardingsphere.transaction.datasource.impl.RdbTransactionLogDataSource;
import io.shardingsphere.transaction.storage.TransactionLogStorageFactory;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * B.A.S.E transaction job factory.
 *
 * @author wangkai
 */
@RequiredArgsConstructor
public final class BASETransactionJobFactory {
    
    private final BASETransactionJobConfiguration baseTransactionJobConfig;
    
    /**
     * start job.
     *
     * @throws SchedulerException quartz scheduler exception
     */
    public void start() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.scheduleJob(buildJobDetail(), buildTrigger());
        scheduler.start();
    }
    
    private JobDetail buildJobDetail() {
        JobDetail jobDetail = JobBuilder.newJob(BASETransactionJob.class).withIdentity(baseTransactionJobConfig.getJobConfig().getName() + "-Job").build();
        jobDetail.getJobDataMap().put("baseTransactionJobConfiguration", baseTransactionJobConfig);
        jobDetail.getJobDataMap().put("transactionLogStorage",
                TransactionLogStorageFactory.createTransactionLogStorage(new RdbTransactionLogDataSource(baseTransactionJobConfig.getDefaultTransactionLogDataSource())));
        return jobDetail;
    }
    
    private Trigger buildTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(baseTransactionJobConfig.getJobConfig().getName() + "-Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(baseTransactionJobConfig.getJobConfig().getCron())).build();
    }
}
