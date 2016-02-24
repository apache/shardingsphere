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

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.rdb.transaction.soft.api.NestedBestEffortsDeliveryJobConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionConfiguration;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;

import lombok.RequiredArgsConstructor;

/**
 * 内嵌的最大努力送达型异步作业工厂.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class NestedBestEffortsDeliveryJobFactory {
    
    private final SoftTransactionConfiguration transactionConfig;
    
    public void init() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(createZookeeperConfiguration(transactionConfig.getNestedBestEffortsDeliveryJobConfiguration()));
        regCenter.init();
        JobScheduler jobScheduler = new JobScheduler(regCenter, createJobConfiguration(transactionConfig.getNestedBestEffortsDeliveryJobConfiguration()));
        jobScheduler.setField("transactionConfig", transactionConfig);
        jobScheduler.init();
    }
    
    private ZookeeperConfiguration createZookeeperConfiguration(final NestedBestEffortsDeliveryJobConfiguration config) {
        ZookeeperConfiguration result = new ZookeeperConfiguration(String.format("localhost:%s", config.getZookeeperPort()), 
                config.getJobNamespace(), config.getZookeeperBaseSleepTimeMilliseconds(), config.getZookeeperMaxSleepTimeMilliseconds(), config.getZookeeperMaxRetries());
        result.setNestedPort(config.getZookeeperPort());
        result.setNestedDataDir(config.getZookeeperDataDir());
        return result;
    }
    
    private JobConfiguration createJobConfiguration(final NestedBestEffortsDeliveryJobConfiguration config) {
        JobConfiguration result = new JobConfiguration(config.getJobName(), NestedBestEffortsDeliveryJob.class, 1, config.getCron());
        result.setFetchDataCount(config.getTransactionLogFetchDataCount());
        return result;
    }
}
