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

package io.shardingsphere.transaction.api;

/**
 * Hold transaction type for current thread.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class TransactionTypeHolder {
    
    private static final ThreadLocal<TransactionType> CONTEXT = new ThreadLocal<TransactionType>() {
        
        @Override
        protected TransactionType initialValue() {
            return TransactionType.LOCAL;
        }
    };
    
    /**
     * Get transaction type for current thread.
     *
     * @return transaction type
     */
    public static TransactionType get() {
        return CONTEXT.get();
    }
    
    /**
     * Set transaction type for current thread.
     *
     * @param transactionType transaction type
     */
    public static void set(final TransactionType transactionType) {
        CONTEXT.set(transactionType);
    }
    
    /**
     * Clear transaction type for current thread.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
