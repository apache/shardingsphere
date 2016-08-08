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

package com.dangdang.ddframe.rdb.transaction.soft.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 异步柔性事务注册中心配置对象.
 * 
 * @author caohao
 */
@Getter
@Setter
public class AsyncSoftTransactionZookeeperConfiguration {
    
    /**
     * 注册中心的连接地址.
     */
    private String connectionString;
    
    /**
     * 作业的命名空间.
     */
    private String namespace = "Best-Efforts-Delivery-Job";
    
    /**
     * 注册中心的等待重试的间隔时间的初始值.
     */
    private int baseSleepTimeMilliseconds = 1000;
    
    /**
     * 注册中心的等待重试的间隔时间的最大值.
     */
    private int maxSleepTimeMilliseconds = 3000;
    
    /**
     * 注册中心的最大重试次数.
     */
    private int maxRetries = 3;
}
