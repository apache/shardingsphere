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

package org.apache.shardingsphere.mode.repository.cluster.transaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public final class TransactionOperation {
    
    public enum Type {
        
        CHECK_EXISTS,
        
        ADD,
        
        UPDATE,
        
        DELETE
    }
    
    private final Type type;
    
    private final String key;
    
    private final String value;
    
    /**
     * Operation add.
     *
     * @param key key
     * @param value value
     * @return TransactionOperation
     */
    public static TransactionOperation opAdd(final String key, final String value) {
        return new TransactionOperation(Type.ADD, key, value);
    }
    
    /**
     * Operation update.
     *
     * @param key key
     * @param value value
     * @return TransactionOperation
     */
    public static TransactionOperation opUpdate(final String key, final String value) {
        return new TransactionOperation(Type.UPDATE, key, value);
    }
    
    /**
     * Operation delete.
     *
     * @param key key
     * @return TransactionOperation
     */
    public static TransactionOperation opDelete(final String key) {
        return new TransactionOperation(Type.DELETE, key, null);
    }
    
    /**
     * Operation check exists.
     *
     * @param key key
     * @return TransactionOperation
     */
    public static TransactionOperation opCheckExists(final String key) {
        return new TransactionOperation(Type.CHECK_EXISTS, key, null);
    }
}
