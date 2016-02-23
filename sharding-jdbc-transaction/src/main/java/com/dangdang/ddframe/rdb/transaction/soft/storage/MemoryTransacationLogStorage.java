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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的事务日志存储器接口.
 * 
 * @author zhangliang
 */
public final class MemoryTransacationLogStorage implements TransacationLogStorage {
    
    private static final ConcurrentHashMap<String, TransactionLog> DATA = new ConcurrentHashMap<>();
    
    @Override
    public void add(final TransactionLog transactionLog) {
        DATA.putIfAbsent(transactionLog.getId(), transactionLog);
    }
    
    @Override
    public TransactionLog load(final String id) {
        return DATA.get(id);
    }
    
    @Override
    public List<TransactionLog> loadBatch(final String transactionId) {
        List<TransactionLog> result = new ArrayList<>(DATA.size());
        for (Entry<String, TransactionLog> entry : DATA.entrySet()) {
            if (transactionId.equals(entry.getValue().getTransactionId())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
   
    @Override
    public void remove(final String id) {
        DATA.remove(id);
    }
    
    @Override
    public void removeBatch(final String transactionId) {
        List<String> toBeRemoved = new ArrayList<>(DATA.size());
        for (Entry<String, TransactionLog> entry : DATA.entrySet()) {
            if (transactionId.equals(entry.getValue().getTransactionId())) {
                toBeRemoved.add(entry.getKey());
            }
        }
        for (String each : toBeRemoved) {
            DATA.remove(each);
        }
    }
}
