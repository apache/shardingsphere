/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.saga;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.saga.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.config.SagaConfiguration;
import io.shardingsphere.transaction.saga.constant.SagaRecoveryPolicy;
import io.shardingsphere.transaction.saga.persistence.SagaPersistence;
import io.shardingsphere.transaction.saga.persistence.SagaSnapshot;
import io.shardingsphere.transaction.saga.revert.EmptyRevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertEngineImpl;
import io.shardingsphere.transaction.saga.revert.RevertResult;
import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Saga transaction object.
 *
 * @author yangyi
 */
@Getter
@RequiredArgsConstructor
public final class SagaTransaction {
    
    private final String id = UUID.randomUUID().toString();
    
    private final SagaConfiguration sagaConfiguration;
    
    private final SagaPersistence persistence;
    
    private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    
    private final Map<SagaSubTransaction, ExecutionResult> executionResultMap = new ConcurrentHashMap<>();
    
    private final Map<SagaSubTransaction, RevertResult> revertResultMap = new ConcurrentHashMap<>();
    
    private final List<Queue<SagaSubTransaction>> logicSQLs = new LinkedList<>();
    
    private Queue<SagaSubTransaction> currentLogicSQL;
    
    private volatile boolean containException;
    
    /**
     * Record and cache result for sub transaction.
     *
     * @param sagaSubTransaction saga sub transaction
     * @param executionResult execution result
     */
    public void recordResult(final SagaSubTransaction sagaSubTransaction, final ExecutionResult executionResult) {
        switch (executionResult) {
            case EXECUTING:
                currentLogicSQL.add(sagaSubTransaction);
                sqlRevert(sagaSubTransaction);
                persistence.persistSnapshot(new SagaSnapshot(id, sagaSubTransaction.hashCode(), sagaSubTransaction.toString(),
                    revertResultMap.get(sagaSubTransaction).toString(), executionResult.name()));
                break;
            default:
                containException = ExecutionResult.FAILURE == executionResult;
                persistence.updateSnapshotStatus(id, sagaSubTransaction.hashCode(), executionResult.name());
        }
        executionResultMap.put(sagaSubTransaction, executionResult);
    }
    
    /**
     * Transaction start next logic SQL.
     */
    public void nextLogicSQL() {
        currentLogicSQL = new ConcurrentLinkedQueue<>();
        logicSQLs.add(currentLogicSQL);
    }
    
    /**
     * Get saga definition builder.
     *
     * @return saga definition builder
     */
    public SagaDefinitionBuilder getSagaDefinitionBuilder() {
        SagaDefinitionBuilder result = createDefinitionBuilder();
        for (Queue<SagaSubTransaction> each : logicSQLs) {
            result.switchParents();
            initSagaDefinitionForLogicSQL(result, each);
        }
        return result;
    }
    
    /**
     * Clean snapshot in persistence.
     */
    public void cleanSnapshot() {
        persistence.cleanSnapshot(id);
    }
    
    private void sqlRevert(final SagaSubTransaction sagaSubTransaction) {
        RevertEngine revertEngine = SagaRecoveryPolicy.FORWARD == sagaConfiguration.getRecoveryPolicy() ? new EmptyRevertEngine() : new RevertEngineImpl(connectionMap);
        try {
            revertResultMap.put(sagaSubTransaction, revertEngine.revert(sagaSubTransaction.getDataSourceName(), sagaSubTransaction.getSql(), sagaSubTransaction.getParameterSets()));
        } catch (SQLException ex) {
            throw new ShardingException(String.format("Revert SQL %s failed: ", sagaSubTransaction.toString()), ex);
        }
    }
    
    private void initSagaDefinitionForLogicSQL(final SagaDefinitionBuilder sagaDefinitionBuilder, final Queue<SagaSubTransaction> sagaSubTransactions) {
        for (SagaSubTransaction each : sagaSubTransactions) {
            RevertResult revertResult = revertResultMap.get(each);
            sagaDefinitionBuilder.addChildRequest(String.valueOf(each.hashCode()), each.getDataSourceName(), each.getSql(), each.getParameterSets(),
                                                  revertResult.getRevertSQL(), revertResult.getRevertSQLParams());
        }
    }
    
    private SagaDefinitionBuilder createDefinitionBuilder() {
        return new SagaDefinitionBuilder(sagaConfiguration.getRecoveryPolicy().getName(), sagaConfiguration.getTransactionMaxRetries(),
                                         sagaConfiguration.getCompensationMaxRetries(), sagaConfiguration.getTransactionRetryDelay());
    }
}
