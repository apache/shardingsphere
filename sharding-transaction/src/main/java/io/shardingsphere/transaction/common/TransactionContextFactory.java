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

package io.shardingsphere.transaction.common;

import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.transaction.api.TransactionManager;
import io.shardingsphere.transaction.common.event.WeakXaTransactionEvent;
import io.shardingsphere.transaction.common.event.XaTransactionEvent;
import io.shardingsphere.transaction.api.local.LocalTransactionManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Transaction context factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionContextFactory {
    
    /**
     * Create transaction context of XA.
     *
     * @param transactionManager transaction manager
     * @return XA transaction context
     */
    public static TransactionContext newXAContext(final TransactionManager transactionManager) {
        return new TransactionContext(transactionManager, TransactionType.XA, XaTransactionEvent.class);
    }
    
    /**
     * Create transaction context of weak XA.
     *
     * @return weak XA transaction context
     */
    public static TransactionContext newWeakXAContext() {
        return new TransactionContext(new LocalTransactionManager(), TransactionType.NONE, WeakXaTransactionEvent.class);
    }
}
