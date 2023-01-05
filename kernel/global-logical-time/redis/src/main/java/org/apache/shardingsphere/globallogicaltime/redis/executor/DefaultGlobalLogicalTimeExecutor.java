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

package org.apache.shardingsphere.globallogicaltime.redis.executor;

import org.apache.shardingsphere.globallogicaltime.spi.GlobalLogicalTimeExecutor;
import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Default global logical time executor, which is turned off by default.
 */
public class DefaultGlobalLogicalTimeExecutor implements GlobalLogicalTimeExecutor {
    
    @Override
    public String beforeCommit(final Collection<Connection> connectionList) throws SQLException {
        return null;
    }
    
    @Override
    public void afterCommit(final String csnLockId) {
        
    }
    
    @Override
    public void getGlobalCSNWhenBeginTransaction(final TransactionConnectionContext transactionConnectionContext) {
        
    }
    
    @Override
    public void sendGlobalCSNAfterStartTransaction(final Connection connection, final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        
    }
    
    @Override
    public void sendSnapshotCSNInReadCommit(final Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups) throws SQLException {
        
    }
}
