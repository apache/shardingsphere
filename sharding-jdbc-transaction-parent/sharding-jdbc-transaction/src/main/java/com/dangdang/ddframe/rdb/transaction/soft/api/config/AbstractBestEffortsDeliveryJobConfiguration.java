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

package com.dangdang.ddframe.rdb.transaction.soft.api.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Best efforts delivery job's configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public abstract class AbstractBestEffortsDeliveryJobConfiguration {
    
    /**
     * Job namespace.
     */
    private String jobNamespace = "Best-Efforts-Delivery-Job";
    
    /**
     * Initial sleep milliseconds for zookeeper.
     */
    private int zookeeperBaseSleepTimeMilliseconds = 1000;
    
    /**
     * Max sleep milliseconds for zookeeper.
     */
    private int zookeeperMaxSleepTimeMilliseconds = 3000;
    
    /**
     * Max retry times for zookeeper.
     */
    private int zookeeperMaxRetries = 3;
    
    /**
     * Best efforts delivery job's name.
     */
    private String jobName = "bestEffortsDeliveryJob";
    
    /**
     * Cron expression for trigger job.
     */
    private String cron = "0/5 * * * * ?";
    
    /**
     * Transaction logs fetch data count.
     */
    private int transactionLogFetchDataCount = 100;
}
