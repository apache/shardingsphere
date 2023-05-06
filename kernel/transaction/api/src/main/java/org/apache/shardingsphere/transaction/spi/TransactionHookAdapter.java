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

package org.apache.shardingsphere.transaction.spi;

import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere transaction hook adapter.
 */
public abstract class TransactionHookAdapter implements TransactionHook {
    
    @Override
    public void beforeBegin(final TransactionConnectionContext transactionContext) {
    }
    
    @Override
    public void afterBegin(final TransactionConnectionContext transactionContext) {
    }
    
    @Override
    public void afterCreateConnections(final Collection<Connection> connections, final TransactionConnectionContext transactionContext) throws SQLException {
    }
    
    @Override
    public void beforeExecuteSQL(final Collection<Connection> connections, final TransactionConnectionContext connectionContext, final TransactionIsolationLevel isolationLevel) throws SQLException {
    }
    
    @Override
    public void beforeCommit(final Collection<Connection> connections, final TransactionConnectionContext transactionContext, final LockContext lockContext) throws SQLException {
    }
    
    @Override
    public void afterCommit(final Collection<Connection> connections, final TransactionConnectionContext transactionContext, final LockContext lockContext) {
    }
    
    @Override
    public void beforeRollback(final Collection<Connection> connections, final TransactionConnectionContext transactionContext) {
    }
    
    @Override
    public void afterRollback(final Collection<Connection> connections, final TransactionConnectionContext transactionContext) {
    }
}
