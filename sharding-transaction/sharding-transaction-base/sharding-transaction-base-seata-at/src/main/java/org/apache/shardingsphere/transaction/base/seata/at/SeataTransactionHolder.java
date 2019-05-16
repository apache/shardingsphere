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

package org.apache.shardingsphere.transaction.base.seata.at;

import io.seata.tm.api.GlobalTransactionContext;

/**
 * Seata transaction holder.
 *
 * @author zhaojun
 */
public final class SeataTransactionHolder {
    
    private static final ThreadLocal<GlobalTransactionContext> CONTEXT = new ThreadLocal<>();
    
    /**
     * Set seata global transaction context.
     *
     * @param transactionContext global transaction context
     */
    public static void set(final GlobalTransactionContext transactionContext) {
        CONTEXT.set(transactionContext);
    }
    
    /**
     * Get seata global transaction context.
     *
     * @return global transaction context
     */
    public static GlobalTransactionContext get() {
        return CONTEXT.get();
    }
    
    /**
     * Clear global transaction context.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
