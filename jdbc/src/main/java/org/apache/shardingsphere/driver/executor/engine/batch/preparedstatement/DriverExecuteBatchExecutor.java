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

package org.apache.shardingsphere.driver.executor.engine.batch.preparedstatement;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.keygen.GeneratedKeyCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.PreparedStatementParametersReplayCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Driver execute batch executor.
 */
public final class DriverExecuteBatchExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final BatchPreparedStatementExecutor batchPreparedStatementExecutor;
    
    private final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine;
    
    private ExecutionContext executionContext;
    
    public DriverExecuteBatchExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final StatementOption statementOption, final StatementManager statementManager,
                                      final ShardingSphereDatabase database) {
        this.connection = connection;
        this.metaData = metaData;
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        batchPreparedStatementExecutor = new BatchPreparedStatementExecutor(database, jdbcExecutor, connection.getProcessId());
        prepareEngine = createDriverExecutionPrepareEngine(statementOption, statementManager, database, metaData);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final StatementOption statementOption, final StatementManager statementManager,
                                                                                                           final ShardingSphereDatabase database, final ShardingSphereMetaData metaData) {
        int maxConnectionsSizePerQuery = connection.getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, maxConnectionsSizePerQuery, connection.getDatabaseConnectionManager(), statementManager, statementOption,
                database.getRuleMetaData().getRules(), metaData);
    }
    
    /**
     * Add batch.
     *
     * @param queryContext query context
     * @param database database
     */
    public void addBatch(final QueryContext queryContext, final ShardingSphereDatabase database) {
        executionContext = createExecutionContext(queryContext, database);
        batchPreparedStatementExecutor.addBatchForExecutionUnits(executionContext.getExecutionUnits());
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext, final ShardingSphereDatabase database) {
        SQLAuditEngine.audit(queryContext, database);
        return new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps());
    }
    
    /**
     * Execute batch.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @param generatedValues generated values
     * @param statementOption statement option
     * @param addCallback statement add callback
     * @param replayCallback prepared statement parameters replay callback
     * @param generatedKeyCallback generated key callback
     * @return generated keys
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int[] executeBatch(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext, final Collection<Comparable<?>> generatedValues,
                              final StatementOption statementOption, final StatementAddCallback addCallback, final PreparedStatementParametersReplayCallback replayCallback,
                              final GeneratedKeyCallback generatedKeyCallback) throws SQLException {
        if (null == executionContext) {
            return new int[0];
        }
        // TODO add raw SQL executor
        return doExecuteBatch(database, batchPreparedStatementExecutor, sqlStatementContext, generatedValues, statementOption, executionContext, addCallback, replayCallback, generatedKeyCallback);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private int[] doExecuteBatch(final ShardingSphereDatabase database, final BatchPreparedStatementExecutor batchExecutor,
                                 final SQLStatementContext sqlStatementContext, final Collection<Comparable<?>> generatedValues, final StatementOption statementOption,
                                 final ExecutionContext executionContext, final StatementAddCallback addCallback, final PreparedStatementParametersReplayCallback replayCallback,
                                 final GeneratedKeyCallback generatedKeyCallback) throws SQLException {
        initBatchPreparedStatementExecutor(database, batchExecutor, executionContext, replayCallback);
        int[] result = batchExecutor.executeBatch(sqlStatementContext);
        if (statementOption.isReturnGeneratedKeys() && generatedValues.isEmpty()) {
            addCallback.add(batchExecutor.getStatements(), Collections.emptyList());
            generatedKeyCallback.generateKeys();
        }
        return result;
    }
    
    private void initBatchPreparedStatementExecutor(final ShardingSphereDatabase database, final BatchPreparedStatementExecutor batchExecutor,
                                                    final ExecutionContext executionContext, final PreparedStatementParametersReplayCallback replayCallback) throws SQLException {
        List<ExecutionUnit> executionUnits = new ArrayList<>(batchExecutor.getBatchExecutionUnits().size());
        for (BatchExecutionUnit each : batchExecutor.getBatchExecutionUnits()) {
            ExecutionUnit executionUnit = each.getExecutionUnit();
            executionUnits.add(executionUnit);
        }
        batchExecutor.init(prepareEngine
                .prepare(database.getName(), executionContext, executionUnits, new ExecutionGroupReportContext(connection.getProcessId(), database.getName())));
        setBatchParameters(replayCallback);
    }
    
    private void setBatchParameters(final PreparedStatementParametersReplayCallback replayCallback) throws SQLException {
        for (Statement each : batchPreparedStatementExecutor.getStatements()) {
            List<List<Object>> parameterSets = batchPreparedStatementExecutor.getParameterSet(each);
            List<Integer> originalBatchIndices = batchPreparedStatementExecutor.getOriginalBatchIndices(each);
            for (int i = 0; i < parameterSets.size(); i++) {
                int originalBatchIndex = i < originalBatchIndices.size() ? originalBatchIndices.get(i) : i;
                replayCallback.replay((PreparedStatement) each, parameterSets.get(i), originalBatchIndex);
                ((PreparedStatement) each).addBatch();
            }
        }
    }
    
    /**
     * Clear.
     */
    public void clear() {
        batchPreparedStatementExecutor.clear();
    }
}
