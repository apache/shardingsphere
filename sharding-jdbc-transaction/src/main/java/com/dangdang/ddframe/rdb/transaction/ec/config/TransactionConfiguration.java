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

package com.dangdang.ddframe.rdb.transaction.ec.config;

import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.transaction.ec.storage.TransactionLogStroageType;

import lombok.Getter;
import lombok.Setter;

/**
 * 事务配置对象.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class TransactionConfiguration {
    
    /**
     * 同步的事务送达的最大尝试次数.
     */
    private int syncMaxDeliveryTryTimes = 3;
    
    /**
     * 异步的事务送达的最大尝试次数.
     */
    // TODO 使用elastic-job做异步重试
    private int asyncMaxDeliveryTryTimes = 3;
    
    /**
     * 事务日志存储类型.
     */
    private TransactionLogStroageType stroageType = TransactionLogStroageType.DATABASE;
    
    /**
     * 存储事务日志的数据源.
     */
    private DataSource transactionLogDataSource;
    
    /**
     * 是否使用内嵌的作业处理异步事务送达.
     */
    private boolean nestedJob;
}
