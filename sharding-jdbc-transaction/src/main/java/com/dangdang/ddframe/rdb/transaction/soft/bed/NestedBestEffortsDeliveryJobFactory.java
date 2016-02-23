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
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;

/**
 * 内嵌的最大努力送达型异步作业工厂.
 * 
 * @author zhangliang
 */
public final class NestedBestEffortsDeliveryJobFactory {
    
    private final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:4181", "Nested-Best-Efforts-Delivery-Job", 1000, 3000, 3);
    
    private final CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    private final JobConfiguration jobConfig = new JobConfiguration("nestedBestEffortsDeliveryJob", NestedBestEffortsDeliveryJob.class, 1, "0/5 * * * * ?");
    
    public void init() {
        zkConfig.setNestedPort(4181);
        zkConfig.setNestedDataDir(String.format("target/test_zk_data/%s/", System.nanoTime()));
        regCenter.init();
        jobConfig.setFetchDataCount(100);
        new JobScheduler(regCenter, jobConfig).init();
    }
}
