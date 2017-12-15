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

package io.shardingjdbc.transaction.bed.async;

import io.shardingjdbc.transaction.api.config.NestedBestEffortsDeliveryJobConfiguration;
import io.shardingjdbc.transaction.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;

/**
 * Best efforts delivery B.A.S.E transaction asynchronized job factory.
 * 
 * @author zhangliang
 */
public final class NestedBestEffortsDeliveryJobFactory extends AbstractBestEffortsDeliveryJobFactory<NestedBestEffortsDeliveryJobConfiguration> {
    
    public NestedBestEffortsDeliveryJobFactory(final SoftTransactionConfiguration transactionConfig) {
        super(transactionConfig);
    }
    
    @Override
    protected ZookeeperConfiguration createZookeeperConfiguration(final NestedBestEffortsDeliveryJobConfiguration config) {
        ZookeeperConfiguration result = new ZookeeperConfiguration(String.format("localhost:%s", config.getZookeeperPort()), 
                config.getJobNamespace(), config.getZookeeperBaseSleepTimeMilliseconds(), config.getZookeeperMaxSleepTimeMilliseconds(), config.getZookeeperMaxRetries());
        result.setNestedPort(config.getZookeeperPort());
        result.setNestedDataDir(config.getZookeeperDataDir());
        return result;
    }
}
