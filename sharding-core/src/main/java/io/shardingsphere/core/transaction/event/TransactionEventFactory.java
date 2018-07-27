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

package io.shardingsphere.core.transaction.event;

import io.shardingsphere.core.transaction.TransactionContextHolder;

/**
 * Create TransactionEvent for current thread.
 *
 * @author zhaojun
 */
public class TransactionEventFactory {
    
    /**
     * Create transaction event.
     *
     * @return TransactionEvent
     */
    public static TransactionEvent create() {
        TransactionEvent transactionEvent = null;
        switch (TransactionContextHolder.get().getTransactionType()) {
            case XA:
                if (TransactionContextHolder.get().getTransactionEventClazz().isAssignableFrom(XaTransactionEvent.class)) {
                    transactionEvent = new XaTransactionEvent("");
                } else {
                    transactionEvent = new WeakXaTransactionEvent();
                }
                break;
            case BASE:
            default:
        }
        return transactionEvent;
    }
}
