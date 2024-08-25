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

package org.apache.shardingsphere.driver.executor.engine.facade;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Driver executor facade.
 */
public interface DriverExecutorFacade extends AutoCloseable {
    
    /**
     * Execute query.
     *
     * @param database database
     * @param queryContext query context
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return result set
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    ResultSet executeQuery(ShardingSphereDatabase database, QueryContext queryContext,
                           Statement statement, Map<String, Integer> columnLabelAndIndexMap, StatementAddCallback addCallback, StatementReplayCallback replayCallback) throws SQLException;
    
    /**
     * Execute update.
     *
     * @param database database
     * @param queryContext query context
     * @param executeUpdateCallback statement execute update callback
     * @param replayCallback statement replay callback
     * @param addCallback statement add callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    int executeUpdate(ShardingSphereDatabase database,
                      QueryContext queryContext, StatementExecuteUpdateCallback executeUpdateCallback, StatementAddCallback addCallback, StatementReplayCallback replayCallback) throws SQLException;
    
    /**
     * Execute.
     *
     * @param database database
     * @param queryContext query context
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    boolean execute(ShardingSphereDatabase database,
                    QueryContext queryContext, StatementExecuteCallback executeCallback, StatementAddCallback addCallback, StatementReplayCallback replayCallback) throws SQLException;
    
    /**
     * Get result set.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @param statement statement
     * @param statements statements
     * @return result set
     * @throws SQLException SQL exception
     */
    Optional<ResultSet> getResultSet(ShardingSphereDatabase database, SQLStatementContext sqlStatementContext, Statement statement, List<? extends Statement> statements) throws SQLException;
    
    @Override
    void close() throws SQLException;
}
