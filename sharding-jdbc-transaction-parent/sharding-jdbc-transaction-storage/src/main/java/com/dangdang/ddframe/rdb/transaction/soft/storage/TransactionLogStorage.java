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

package com.dangdang.ddframe.rdb.transaction.soft.storage;

import java.sql.Connection;
import java.util.List;

/**
 * 事务日志存储器接口.
 * 
 * @author zhangliang
 */
public interface TransactionLogStorage {
    
    /**
     * 存储事务日志.
     * 
     * @param transactionLog 事务日志
     */
    void add(TransactionLog transactionLog);
    
    /**
     *  根据主键删除事务日志.
     * 
     * @param id 事务日志主键
     */
    void remove(String id);
    
    /**
     * 读取需要处理的事务日志.
     * 
     * <p>需要处理的事务日志为: </p>
     * <p>1. 异步处理次数小于最大处理次数.</p>
     * <p>2. 异步处理的事务日志早于异步处理的间隔时间.</p>
     * 
     * @param size 获取日志的数量
     * @param maxDeliveryTryTimes 事务送达的最大尝试次数
     * @param maxDeliveryTryDelayMillis 执行送达事务的延迟毫秒数.
     */
    List<TransactionLog> findEligibleTransactionLogs(int size, int maxDeliveryTryTimes, long maxDeliveryTryDelayMillis);
    
    /**
     * 增加事务日志异步重试次数.
     * 
     * @param id 事务主键
     */
    void increaseAsyncDeliveryTryTimes(String id);
    
    /**
     * 增加事务日志异步重试次数.
     *
     * @param connection 业务数据库连接
     * @param transactionLog 事务日志
     * @param maxDeliveryTryTimes 事务送达的最大尝试次数
     */
    boolean processData(Connection connection, TransactionLog transactionLog, int maxDeliveryTryTimes);
}
