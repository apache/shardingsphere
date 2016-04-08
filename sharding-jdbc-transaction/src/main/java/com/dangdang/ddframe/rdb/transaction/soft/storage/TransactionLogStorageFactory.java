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

import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.impl.DatabaseTransactionLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.impl.MemoryTransactionLogStorage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 事务日志存储器工厂.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionLogStorageFactory {
    
    /**
     * 创建事务日志存储器.
     * 
     * @param transactionConfiguration 柔性事务配置对象
     * @return 事务日志存储器
     */
    public static TransactionLogStorage createTransactionLogStorageFactory(final SoftTransactionConfiguration transactionConfiguration) {
        switch (transactionConfiguration.getStorageType()) {
            case MEMORY: 
                return new MemoryTransactionLogStorage(transactionConfiguration);
            case DATABASE: 
                return new DatabaseTransactionLogStorage(transactionConfiguration, transactionConfiguration);
            default: 
                throw new UnsupportedOperationException(transactionConfiguration.getStorageType().toString());
        }
    }
}
