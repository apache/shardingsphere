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

package org.apache.shardingsphere.sql.parser.sql.common.constant;

/**
 * Transaction isolation level enum.
 */
public enum TransactionIsolationLevel {
    NONE("NONE"),
    REPEATABLE_READ("REPEATABLE READ"),
    READ_UNCOMMITTED("READ UNCOMMITTED"),
    SERIALIZABLE("SERIALIZABLE"),
    READ_COMMITTED("READ COMMITTED");


    private final String isolationLevel;

    TransactionIsolationLevel(final String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    /**
     * Get transaction isolation level.
     *
     * @return transaction isolation level
     */
    public String getIsolationLevel() {
        return isolationLevel;
    }
}
