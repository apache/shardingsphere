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

package org.apache.shardingsphere.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transaction holder.
 * 
 * <p>is transaction or not in current thread.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionHolder {
    
    private static final ThreadLocal<Map<String, Boolean>> TRANSACTION = ThreadLocal.withInitial(() -> initValue());
    
    private static Map<String, Boolean> initValue() {
        Map<String, Boolean> result = new LinkedHashMap<>(1);
        result.put("status", false);
        result.put("readOnly", false);
        return result;
    }
    
    /**
     * Judge is transaction in current thread.
     * 
     * @return is transaction in current thread.
     */
    public static boolean isTransaction() {
        return TRANSACTION.get().get("status");
    }
    
    /**
     * Judge is transaction read only in current thread.
     *
     * @return is transaction read only in current thread.
     */
    public static boolean isTransactionReadOnly() {
        return TRANSACTION.get().get("readOnly");
    }
    
    /**
     * Set transaction in current thread.
     */
    public static void setInTransaction(final boolean readOnly) {
        TRANSACTION.get().put("status", true);
        TRANSACTION.get().put("readOnly", readOnly);
    }
    
    /**
     * Clear transaction.
     */
    public static void clear() {
        TRANSACTION.remove();
    }
}
