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
 * 异步柔性事务作业配置对象.
 *
 * @author caohao
 */
@Getter
@Setter
public class AsyncSoftTransactionJobConfiguration {
    
    /**
     * 作业名称.
     */
    private String name = "bestEffortsDeliveryJob";
    
    /**
     * 触发作业的cron表达式.
     */
    private String cron = "0/5 * * * * ?";
    
    /**
     * 每次作业获取的事务日志最大数量.
     */
    private int transactionLogFetchDataCount = 100;
    
    /**
     * 事务送达的最大尝试次数.
     */
    private int maxDeliveryTryTimes = 3;
    
    /**
     * 执行事务的延迟毫秒数.
     *
     * <p>早于此间隔时间的入库事务才会被作业执行.</p>
     */
    private long maxDeliveryTryDelayMillis = 60  * 1000L;
}
