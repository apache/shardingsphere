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
import com.dangdang.ddframe.rdb.transaction.soft.api.AbstractBestEffortsDeliveryJobConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionConfiguration;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;

import lombok.RequiredArgsConstructor;

/**
 * 最大努力送达型异步作业的抽象工厂.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractBestEffortsDeliveryJobFactory<T extends AbstractBestEffortsDeliveryJobConfiguration> {
    
    private final SoftTransactionConfiguration transactionConfig;
    
    /**
     * 初始化作业.
     */
    public void init() {
        @SuppressWarnings("unchecked")
        T config = (T) transactionConfig.getBestEffortsDeliveryJobConfiguration();
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(createZookeeperConfiguration(config));
        regCenter.init();
        JobScheduler jobScheduler = new JobScheduler(regCenter, createJobConfiguration(config));
        jobScheduler.setField("transactionConfig", transactionConfig);
        jobScheduler.init();
    }
    
    protected abstract ZookeeperConfiguration createZookeeperConfiguration(final T config);
    
    private JobConfiguration createJobConfiguration(final T config) {
        JobConfiguration result = new JobConfiguration(config.getJobName(), BestEffortsDeliveryJob.class, 1, config.getCron());
        result.setFetchDataCount(config.getTransactionLogFetchDataCount());
        return result;
    }
}
