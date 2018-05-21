/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.storage;

import io.shardingsphere.transaction.datasource.TransactionLogDataSource;
import io.shardingsphere.transaction.storage.impl.MemoryTransactionLogStorage;
import io.shardingsphere.transaction.storage.impl.RdbTransactionLogStorage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Transaction log storage factory.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionLogStorageFactory {
    
    /**
     * Create transaction log storage.
     *
     * @param transactionLogDataSource transaction log data source
     * @return transaction log storage object
     */
    public static TransactionLogStorage createTransactionLogStorage(final TransactionLogDataSource transactionLogDataSource) {
        switch (transactionLogDataSource.getType()) {
            case MEMORY:
                return new MemoryTransactionLogStorage();
            case RDB:
                return new RdbTransactionLogStorage(transactionLogDataSource.getDataSource());
            default:
                throw new UnsupportedOperationException();
        }
    }
}
