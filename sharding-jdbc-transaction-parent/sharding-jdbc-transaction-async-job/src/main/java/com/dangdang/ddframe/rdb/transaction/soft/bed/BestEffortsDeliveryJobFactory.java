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

package com.dangdang.ddframe.rdb.transaction.soft.bed;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.rdb.transaction.soft.config.AsyncSoftTransactionZookeeperConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.datasource.impl.RdbTransactionLogDataSource;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorageFactory;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.RequiredArgsConstructor;

/**
 * 最大努力送达型异步作业工厂.
 *
 * @author zhangliang 
 * @author caohao
 */
@RequiredArgsConstructor
public final class BestEffortsDeliveryJobFactory {
    
    private final BestEffortsDeliveryConfiguration bedConfig;
    
    /**
     * 初始化作业.
     */
    public void init() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(createZookeeperConfiguration(bedConfig));
        regCenter.init();
        JobScheduler jobScheduler = new JobScheduler(regCenter, createBedJobConfiguration(bedConfig));
        jobScheduler.setField("bedConfig", bedConfig);
        jobScheduler.setField("transactionLogStorage", TransactionLogStorageFactory.createTransactionLogStorage(new RdbTransactionLogDataSource(bedConfig.getDefaultTransactionLogDataSource())));
        jobScheduler.init();
    }
    
    public ZookeeperConfiguration createZookeeperConfiguration(final BestEffortsDeliveryConfiguration bedConfig) {
        AsyncSoftTransactionZookeeperConfiguration zkConfig = bedConfig.getZkConfig();
        return new ZookeeperConfiguration(zkConfig.getConnectionString(), zkConfig.getNamespace(), zkConfig.getBaseSleepTimeMilliseconds(),
            zkConfig.getMaxSleepTimeMilliseconds(), zkConfig.getMaxRetries());
    }
    
    private JobConfiguration createBedJobConfiguration(final BestEffortsDeliveryConfiguration bedJobConfig) {
        JobConfiguration result = new JobConfiguration(bedJobConfig.getJobConfig().getName(), BestEffortsDeliveryJob.class, 1, bedJobConfig.getJobConfig().getCron());
        result.setFetchDataCount(bedJobConfig.getJobConfig().getTransactionLogFetchDataCount());
        result.setOverwrite(true);
        return result;
    }
}
