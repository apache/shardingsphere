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

package com.dangdang.ddframe.rdb.transaction.soft.storage;

import java.util.List;

/**
 * 事务日志存储器接口.
 * 
 * @author zhangliang
 */
public interface TransacationLogStorage {
    
    /**
     * 存储事务日志.
     * 
     * @param transactionLog 事务日志
     */
    void add(TransactionLog transactionLog);
    
    /**
     * 根据主键读取事务日志.
     * 
     * @param id 事务日志主键
     */
    TransactionLog load(String id);
    
    /**
     * 根据事务主键批量读取事务日志.
     * 
     * @param transactionId 事务主键
     */
    List<TransactionLog> loadBatch(String transactionId);
    
    /**
     *  根据主键删除事务日志.
     * 
     * @param id 事务日志主键
     */
    void remove(String id);
    
    /**
     * 根据事务主键批量删除事务日志.
     * 
     * @param transactionId 事务主键
     */
    void removeBatch(String transactionId);
}
