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

package io.shardingsphere.transaction.common.config;

import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.transaction.common.spi.TransactionManager;

/**
 * Execute transaction manager configuration.
 *
 * @author zhaojun
 */
public interface TransactionConfiguration {
    
    /**
     * Config transaction context, then binding to current thread.
     *
     * @param transactionType transaction type
     * @return transaction manager
     */
    TransactionManager configTransactionContext(TransactionType transactionType);
    
    /**
     * Subscribe transaction event using listener, register into event bus.
     */
    void registerListener();
  
}
