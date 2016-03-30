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

import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.bed.async.AbstractBestEffortsDeliveryJobFactory;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;

/**
 * 最大努力送达型异步作业工厂.
 * 
 * @author zhangliang
 */
public final class BestEffortsDeliveryJobFactory extends AbstractBestEffortsDeliveryJobFactory<BestEffortsDeliveryJobConfiguration> {
    
    public BestEffortsDeliveryJobFactory(SoftTransactionConfiguration transactionConfig) {
        super(transactionConfig);
    }
    
    @Override
    public ZookeeperConfiguration createZookeeperConfiguration(final BestEffortsDeliveryJobConfiguration config) {
        return new ZookeeperConfiguration(config.getZookeeperConnectionString(), config.getJobNamespace(), config.getZookeeperBaseSleepTimeMilliseconds(), config.getZookeeperMaxSleepTimeMilliseconds(), config.getZookeeperMaxRetries());
    }
}
