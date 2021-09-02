/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.transaction.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Hold transaction type for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionTypeHolder {
    
    private static final ThreadLocal<TransactionType> CONTEXT = ThreadLocal.withInitial(() -> TransactionType.LOCAL);
    
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
