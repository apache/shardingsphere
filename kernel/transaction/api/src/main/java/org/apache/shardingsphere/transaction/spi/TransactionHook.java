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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPI;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere transaction hook.
 * 
 * @param <T> type of rule
 */
@SingletonSPI
public interface TransactionHook<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Process before opening the transaction.
     *
     * @param rule rule
     * @param databaseType database type
     * @param transactionContext transaction context
     */
    void beforeBegin(T rule, DatabaseType databaseType, TransactionConnectionContext transactionContext);
    
    /**
     * Process after opening the transaction.
     *
     * @param rule rule
     * @param databaseType database type
     * @param transactionContext transaction context
     */
    void afterBegin(T rule, DatabaseType databaseType, TransactionConnectionContext transactionContext);
    
    /**
     * Process after connection is created.
     *
     * @param rule rule
     * @param databaseType database type
     * @param connections connections
     * @param transactionContext transaction context
     * @throws SQLException SQL exception
     */
    void afterCreateConnections(T rule, DatabaseType databaseType, Collection<Connection> connections, TransactionConnectionContext transactionContext) throws SQLException;
    
    /**
     * Process before executing SQL.
     *
     * @param rule rule
     * @param databaseType database type
     * @param connections connections
     * @param transactionContext transaction context
     * @param isolationLevel isolation level
     * @throws SQLException SQL exception
     */
    void beforeExecuteSQL(T rule, DatabaseType databaseType, Collection<Connection> connections, TransactionConnectionContext transactionContext,
                          TransactionIsolationLevel isolationLevel) throws SQLException;
    
    /**
     * Process before committing the transaction.
     *
     * @param rule rule
     * @param databaseType database type
     * @param connections connections
     * @param transactionContext transaction context
     * @throws SQLException SQL exception
     */
    void beforeCommit(T rule, DatabaseType databaseType, Collection<Connection> connections, TransactionConnectionContext transactionContext) throws SQLException;
    
    /**
     * Process after committing the transaction.
     *
     * @param rule rule
     * @param databaseType database type
     * @param connections connections
     * @param transactionContext transaction context
     */
    void afterCommit(T rule, DatabaseType databaseType, Collection<Connection> connections, TransactionConnectionContext transactionContext);
    
    /**
     * Whether to need lock when transaction committed.
     *
     * @param rule rule
     * @return need lock or not
     */
    boolean isNeedLockWhenCommit(T rule);
    
    /**
     * Process before rolling back the transaction.
     *
     * @param rule rule
     * @param databaseType database type
     * @param connections connections
     * @param transactionContext transaction context
     */
    void beforeRollback(T rule, DatabaseType databaseType, Collection<Connection> connections, TransactionConnectionContext transactionContext);
    
    /**
     * Process after rolling back the transaction.
     *
     * @param rule rule
     * @param databaseType database type
     * @param connections connections
     * @param transactionContext transaction context
     */
    void afterRollback(T rule, DatabaseType databaseType, Collection<Connection> connections, TransactionConnectionContext transactionContext);
}
