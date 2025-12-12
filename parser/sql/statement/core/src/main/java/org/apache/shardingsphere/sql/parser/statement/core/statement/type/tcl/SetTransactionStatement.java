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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OperationScope;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;

import java.util.Optional;

/**
 * Set transaction statement.
 */
public final class SetTransactionStatement extends TCLStatement {
    
    private final OperationScope scope;
    
    private final TransactionIsolationLevel isolationLevel;
    
    private final TransactionAccessType accessMode;
    
    public SetTransactionStatement(final DatabaseType databaseType) {
        this(databaseType, null, null, null);
    }
    
    public SetTransactionStatement(final DatabaseType databaseType, final OperationScope scope, final TransactionIsolationLevel isolationLevel, final TransactionAccessType accessMode) {
        super(databaseType);
        this.scope = scope;
        this.isolationLevel = isolationLevel;
        this.accessMode = accessMode;
    }
    
    /**
     *  Whether to contain transaction scope.
     *
     * @return contains transaction scope or not
     */
    public boolean containsScope() {
        return null != scope;
    }
    
    /**
     * Whether is desired transaction scope.
     *
     * @param scope desired transaction scope
     * @return is desired transaction scope or not
     */
    public boolean isDesiredScope(final OperationScope scope) {
        return this.scope == scope;
    }
    
    /**
     * Get transaction isolation level.
     *
     * @return transaction isolation level
     */
    public Optional<TransactionIsolationLevel> getIsolationLevel() {
        return Optional.ofNullable(isolationLevel);
    }
    
    /**
     * Whether is desired transaction access mode.
     *
     * @param accessMode desired transaction access mode
     * @return is desired transaction access mode or not
     */
    public boolean isDesiredAccessMode(final TransactionAccessType accessMode) {
        return this.accessMode == accessMode;
    }
}
