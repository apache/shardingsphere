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

package org.apache.shardingsphere.globallogicaltime.spi;

import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Global logical time executor.
 */
public interface GlobalLogicalTimeExecutor {
    
    /**
     * before xa transaction start to commit.
     *
     * @param connectionList connection list
     * @return csnLockId csn lock id
     * @throws SQLException sql exception
     */
    String beforeCommit(Collection<Connection> connectionList) throws SQLException;
    
    /**
     * xa transaction commit completed.
     *
     * @param csnLockId csn lock id
     */
    void afterCommit(String csnLockId);
    
    /**
     * Get global csn when beginning transaction.
     *
     * @param transactionConnectionContext transaction connection context
     */
    void getGlobalCSNWhenBeginTransaction(TransactionConnectionContext transactionConnectionContext);
    
    /**
     * send snapshot csn to a dn after cn send "start transaction" to the dn.
     *
     * @param connection connection
     * @param transactionConnectionContext transaction connection context
     * @throws SQLException sql exception
     */
    void sendGlobalCSNAfterStartTransaction(Connection connection, TransactionConnectionContext transactionConnectionContext) throws SQLException;
    
    /**
     * send snapshot csn to each dns in Read Commit isolation level.
     *
     * @param inputGroups input groups
     * @throws SQLException sql exception
     */
    void sendSnapshotCSNInReadCommit(Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups) throws SQLException;
}
