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

package io.shardingsphere.spi.transaction;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Sharding transaction manager loader.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTransactionManagerRegistry {
    
    private static final Map<TransactionType, ShardingTransactionManager<ShardingTransactionEvent>> TRANSACTION_MANAGER_MAP = new HashMap<>();
    
    private static final ShardingTransactionManagerRegistry INSTANCE = new ShardingTransactionManagerRegistry();
    
    /**
     * Load sharding transaction manager.
     */
    @SuppressWarnings("unchecked")
    public static void load() {
        for (ShardingTransactionManager each : ServiceLoader.load(ShardingTransactionManager.class)) {
            TRANSACTION_MANAGER_MAP.put(each.getTransactionType(), (ShardingTransactionManager<ShardingTransactionEvent>) each);
        }
    }
    
    /**
     * Get instance of sharding transaction manager registry.
     *
     * @return sharding transaction manager registry
     */
    public static ShardingTransactionManagerRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get transaction manager by type.
     *
     * @param transactionType transaction type
     * @return sharding transaction manager implement
     */
    public ShardingTransactionManager<ShardingTransactionEvent> getTransactionManager(final TransactionType transactionType) {
        ShardingTransactionManager<ShardingTransactionEvent> result = TRANSACTION_MANAGER_MAP.get(transactionType);
        Preconditions.checkNotNull(result, String.format("Cannot find transaction manager of [%s]", transactionType));
        return result;
    }
}
