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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import org.apache.shardingsphere.globallogicaltime.redis.connector.GlobalLogicalTimeRedisConnector;
import org.apache.shardingsphere.globallogicaltime.spi.GlobalLogicalTimeExecutor;
import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Global logical time executor based redis.
 */
@Slf4j
public class BasedRedisGlobalLogicalTimeExecutor implements GlobalLogicalTimeExecutor {
    
    private GlobalLogicalTimeRedisConnector redisConnector;
    
    public BasedRedisGlobalLogicalTimeExecutor(final GlobalLogicalTimeRuleConfiguration configuration) {
        init(configuration);
    }
    
    private void init(final GlobalLogicalTimeRuleConfiguration configuration) {
        redisConnector = new GlobalLogicalTimeRedisConnector(configuration);
    }
    
    /**
     * Try csn lock before transaction starts to  commit, if success get current global csn and send to each dns.
     *
     * @param connectionList the connections to each dns
     * @return csn lock
     * @throws SQLException SQL exception
     */
    @Override
    public String beforeCommit(final Collection<Connection> connectionList) throws SQLException {
        // Try csn lock before transaction starts to commit.
        String csnLockId = GlobalLogicalTimeRedisConnector.tryCSNLockId();
        try {
            redisConnector.lockCSN(csnLockId);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        
        // Set commit csn to each dns.
        String order = "SELECT " + redisConnector.getCurrentCSN() + " AS SETCOMMITCSN;";
        for (Connection connection : connectionList) {
            try (Statement statement = connection.createStatement()) {
                log.debug(order);
                statement.execute(order);
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("can not send commit csn!");
                throw new SQLException(e.getMessage());
            }
        }
        return csnLockId;
    }
    
    /**
     * Add 1 to the global csn in redis, and release csn lock.
     *
     * @param csnLockId csn lock id
     */
    @Override
    public void afterCommit(final String csnLockId) {
        redisConnector.getNextCSN();
        redisConnector.unLockCSN(csnLockId);
    }
    
    @Override
    public void getGlobalCSNWhenBeginTransaction(final TransactionConnectionContext transactionConnectionContext) {
        long csn = redisConnector.getCurrentCSN();
        transactionConnectionContext.setGlobalCSN(csn);
    }
    
    /**
     * Send snapshot csn to each dns after cn send "START TRANSACTION" command.
     *
     * @param connection connection to dn
     * @throws SQLException SQL exception
     */
    @Override
    public void sendGlobalCSNAfterStartTransaction(final Connection connection, final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        long csn = transactionConnectionContext.getGlobalCSN();
        sendSnapshotCSN(connection, csn);
    }
    
    /**
     * Send snapshot csn to each dns in Read Commit isolation level.
     *
     * @param inputGroups input groups
     * @throws SQLException SQL Exception
     */
    @Override
    public void sendSnapshotCSNInReadCommit(final Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups) throws SQLException {
        List<Connection> connections = new ArrayList<>();
        for (ExecutionGroup<JDBCExecutionUnit> entry : inputGroups) {
            List<JDBCExecutionUnit> inputs = entry.getInputs();
            for (JDBCExecutionUnit input : inputs) {
                Connection connection = input.getStorageResource().getConnection();
                connections.add(connection);
            }
        }
        if (!connections.isEmpty()) {
            // By default, shardingsphere transaction is read-commit level.
            boolean inTransaction = !connections.get(0).getAutoCommit();
            // By default, transactions' isolation are read-commit.
            if (inTransaction) {
                long csn = redisConnector.getCurrentCSN();
                for (Connection connection : connections) {
                    sendSnapshotCSN(connection, csn);
                }
            }
        }
    }
    
    private void sendSnapshotCSN(final Connection connection, final long csn) throws SQLException {
        String order = "SELECT " + csn + " AS SETSNAPSHOTCSN;";
        try (Statement statement = connection.createStatement()) {
            log.debug(order);
            statement.execute(order);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("can not send snapshot csn.");
            throw new SQLException(e);
        }
    }
}
