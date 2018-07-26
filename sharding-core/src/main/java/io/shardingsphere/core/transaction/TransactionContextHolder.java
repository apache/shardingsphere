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

package io.shardingsphere.core.transaction;

/**
 * Hold transaction context for current thread.
 *
 * @author zhaojun
 */
public class TransactionContextHolder {
    
    private static final ThreadLocal<TransactionContext> CONTEXT = new ThreadLocal<TransactionContext>() {
        @Override
        protected TransactionContext initialValue() {
            return new TransactionContext();
        }
    };
    
    /**
     * Get transaction context for current thread.
     *
     * @return TransactionContext
     */
    public static TransactionContext get() {
        return CONTEXT.get();
    }
    
    /**
     * Set transaction context for current thread.
     *
     * @param context Transaction context
     */
    public static void set(final TransactionContext context) {
        CONTEXT.set(context);
    }
}
