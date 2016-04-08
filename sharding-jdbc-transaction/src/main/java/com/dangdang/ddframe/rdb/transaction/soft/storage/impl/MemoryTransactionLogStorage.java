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

package com.dangdang.ddframe.rdb.transaction.soft.storage.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionType;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;

import lombok.RequiredArgsConstructor;

/**
 * 基于内存的事务日志存储器接口.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MemoryTransactionLogStorage implements TransactionLogStorage {
    
    private static final ConcurrentHashMap<String, TransactionLog> DATA = new ConcurrentHashMap<>();
    
    private final SoftTransactionConfiguration transactionConfig;
    
    @Override
    public void add(final TransactionLog transactionLog) {
        DATA.putIfAbsent(transactionLog.getId(), transactionLog);
    }
    
    @Override
    public void remove(final String id) {
        DATA.remove(id);
    }
    
    @Override
    public List<TransactionLog> findEligibleTransactionLogs(final int size, final SoftTransactionType type) {
        List<TransactionLog> result = new ArrayList<>();
        int count = 0;
        for (TransactionLog each : DATA.values()) {
            if (count >= size) {
                break;
            }
            if (each.getAsyncDeliveryTryTimes() < transactionConfig.getAsyncMaxDeliveryTryTimes() 
                    && type == each.getTransactionType() && each.getCreationTime() < System.currentTimeMillis() - transactionConfig.getAsyncMaxDeliveryTryDelayMillis()) {
                result.add(each);
            }
            count++;
        }
        return result;
    }
    
    @Override
    public void increaseAsyncDeliveryTryTimes(final String id) {
        if (DATA.containsKey(id)) {
            TransactionLog transactionLog = DATA.get(id);
            transactionLog.setAsyncDeliveryTryTimes(transactionLog.getAsyncDeliveryTryTimes() + 1);
            DATA.put(id, transactionLog);
        }
    }
}
