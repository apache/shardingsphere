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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.constant.TransactionIsolationLevel;

import java.sql.Connection;

/**
 * Transaction util class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionUtil {
    
    /**
     * Get the value of type int according to TransactionIsolationLevel.
     *
     * @param isolationLevel value of type TransactionIsolationLevel
     * @return isolation level
     */
    public static int getTransactionIsolationLevel(final TransactionIsolationLevel isolationLevel) {
        switch (isolationLevel) {
            case READ_UNCOMMITTED:
                return Connection.TRANSACTION_READ_UNCOMMITTED;
            case READ_COMMITTED:
                return Connection.TRANSACTION_READ_COMMITTED;
            case REPEATABLE_READ:
                return Connection.TRANSACTION_REPEATABLE_READ;
            case SERIALIZABLE:
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                return Connection.TRANSACTION_NONE;
        }
    }
    
    /**
     * Get the value of type TransactionIsolationLevel according to int.
     *
     * @param isolationLevel value of type int
     * @return isolation level
     */
    public static TransactionIsolationLevel getTransactionIsolationLevel(final int isolationLevel) {
        switch (isolationLevel) {
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return TransactionIsolationLevel.READ_UNCOMMITTED;
            case Connection.TRANSACTION_READ_COMMITTED:
                return TransactionIsolationLevel.READ_COMMITTED;
            case Connection.TRANSACTION_REPEATABLE_READ:
                return TransactionIsolationLevel.REPEATABLE_READ;
            case Connection.TRANSACTION_SERIALIZABLE:
                return TransactionIsolationLevel.SERIALIZABLE;
            default:
                return TransactionIsolationLevel.NONE;
        }
    }
}
