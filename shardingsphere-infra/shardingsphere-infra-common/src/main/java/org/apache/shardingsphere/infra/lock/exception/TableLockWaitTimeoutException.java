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

package org.apache.shardingsphere.infra.lock.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Table lock wait timeout exception.
 */
public final class TableLockWaitTimeoutException extends LockSQLException {
    
    private static final long serialVersionUID = 2599713085782288003L;
    
    public TableLockWaitTimeoutException(final String schemaName, final String tableName, final long timeoutMilliseconds) {
        super(XOpenSQLState.GENERAL_ERROR, 1, "The table `%s` of schema `%s` lock wait timeout of `%s` milliseconds exceeded.", tableName, schemaName, String.valueOf(timeoutMilliseconds));
    }
}
