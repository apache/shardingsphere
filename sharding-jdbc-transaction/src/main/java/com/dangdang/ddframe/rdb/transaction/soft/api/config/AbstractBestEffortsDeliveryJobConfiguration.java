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

package com.dangdang.ddframe.rdb.transaction.soft.api.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 最大努力送达型异步作业配置的抽象对象.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public abstract class AbstractBestEffortsDeliveryJobConfiguration {
    
    /**
     * 作业的命名空间.
     */
    private String jobNamespace = "Best-Efforts-Delivery-Job";
    
    /**
     * 注册中心的等待重试的间隔时间的初始值.
     */
    private int zookeeperBaseSleepTimeMilliseconds = 1000;
    
    /**
     * 注册中心的等待重试的间隔时间的最大值.
     */
    private int zookeeperMaxSleepTimeMilliseconds = 3000;
    
    /**
     * 注册中心的最大重试次数.
     */
    private int zookeeperMaxRetries = 3;
    
    /**
     * 最大努力送达型异步作业名称.
     */
    private String jobName = "bestEffortsDeliveryJob";
    
    /**
     * 触发作业的cron表达式.
     */
    private String cron = "0/5 * * * * ?";
    
    /**
     * 每次作业获取的事务日志最大数量.
     */
    private int transactionLogFetchDataCount = 100;
}
