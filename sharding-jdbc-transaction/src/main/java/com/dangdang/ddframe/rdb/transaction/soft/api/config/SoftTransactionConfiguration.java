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

import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorageType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 柔性事务配置对象.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SoftTransactionConfiguration {
    
    /**
     * 事务管理器管理的数据源.
     */
    private final ShardingDataSource targetDataSource;
    
    /**
     * 同步的事务送达的最大尝试次数.
     */
    private int syncMaxDeliveryTryTimes = 3;
    
    /**
     * 异步的事务送达的最大尝试次数.
     */
    private int asyncMaxDeliveryTryTimes = 3;
    
    /**
     * 执行异步送达事务的延迟毫秒数.
     * 
     * <p>早于此间隔时间的入库事务才会被异步作业执行.</p>
     */
    private long asyncMaxDeliveryTryDelayMillis = 60  * 1000L;
    
    /**
     * 事务日志存储类型.
     */
    private TransactionLogStorageType storageType = TransactionLogStorageType.DATABASE;
    
    /**
     * 存储事务日志的数据源.
     */
    private DataSource transactionLogDataSource;
    
    /**
     * 是否使用内嵌的作业处理异步事务送达.
     */
    private boolean nestedJob;
    
    /**
     * 内嵌的最大努力送达型异步作业配置对象.
     */
    private AbstractBestEffortsDeliveryJobConfiguration bestEffortsDeliveryJobConfiguration;
}
