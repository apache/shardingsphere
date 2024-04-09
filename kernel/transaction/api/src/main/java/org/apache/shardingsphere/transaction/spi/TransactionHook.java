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
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere transaction hook.
 */
@SingletonSPI
public interface TransactionHook extends TypedSPI {
    
    /**
     * Process before opening the transaction.
     *
     * @param transactionContext transaction context
     */
    void beforeBegin(TransactionConnectionContext transactionContext);
    
    /**
     * Process after opening the transaction.
     *
     * @param transactionContext transaction context
     */
    void afterBegin(TransactionConnectionContext transactionContext);
    
    /**
     * Process after connection is created.
     *
     * @param connections connections
     * @param transactionContext transaction context
     * @throws SQLException SQL exception
     */
    void afterCreateConnections(Collection<Connection> connections, TransactionConnectionContext transactionContext) throws SQLException;
    
    /**
     * Process before executing sql.
     *
     * @param connections connections
     * @param transactionContext transaction context
     * @param isolationLevel isolation level
     * @throws SQLException SQL exception
     */
    void beforeExecuteSQL(Collection<Connection> connections, TransactionConnectionContext transactionContext, TransactionIsolationLevel isolationLevel) throws SQLException;
    
    /**
     * Process before committing the transaction.
     *
     * @param connections connections
     * @param transactionContext transaction context
     * @param lockContext lock context
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    void beforeCommit(Collection<Connection> connections, TransactionConnectionContext transactionContext, LockContext lockContext) throws SQLException;
    
    /**
     * Process after committing the transaction.
     *
     * @param connections connections
     * @param transactionContext transaction context
     * @param lockContext lock context
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    void afterCommit(Collection<Connection> connections, TransactionConnectionContext transactionContext, LockContext lockContext) throws SQLException;
    
    /**
     * Process before rolling back the transaction.
     *
     * @param connections connections
     * @param transactionContext transaction context
     * @throws SQLException SQL exception
     */
    void beforeRollback(Collection<Connection> connections, TransactionConnectionContext transactionContext) throws SQLException;
    
    /**
     * Process after rolling back the transaction.
     *
     * @param connections connections
     * @param transactionContext transaction context
     * @throws SQLException SQL exception
     */
    void afterRollback(Collection<Connection> connections, TransactionConnectionContext transactionContext) throws SQLException;
}
