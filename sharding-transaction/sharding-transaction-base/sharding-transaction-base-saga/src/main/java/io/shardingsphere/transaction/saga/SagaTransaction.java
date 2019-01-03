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

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.SagaRecoveryPolicy;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.core.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.revert.EmptyRevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertEngineImpl;
import io.shardingsphere.transaction.saga.revert.RevertResult;
import io.shardingsphere.transaction.saga.servicecomb.definition.SagaDefinitionBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

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
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final Map<SagaSubTransaction, ExecutionResult> executionResultMap = new ConcurrentHashMap<>();
    
    private volatile boolean containException;
    
    /**
     * Record and cache result for sub transaction.
     *
     * @param sagaSubTransaction saga sub transaction
     * @param result execution result
     */
    public void recordResult(final SagaSubTransaction sagaSubTransaction, final ExecutionResult result) {
        if (ExecutionResult.FAILURE == result) {
            containException = true;
        }
        executionResultMap.put(sagaSubTransaction, result);
    }
    
    /**
     * Get saga definition builder.
     *
     * @return saga definition builder
     */
    public SagaDefinitionBuilder getSagaDefinitionBuilder() {
        SagaDefinitionBuilder result = createDefinitionBuilder();
        RevertEngine revertEngine = SagaRecoveryPolicy.FORWARD == sagaConfiguration.getRecoveryPolicy() ? new EmptyRevertEngine() : new RevertEngineImpl(dataSourceMap);
        for (SagaSubTransaction each : executionResultMap.keySet()) {
            try {
                RevertResult revertResult = revertEngine.revert(each.getDataSourceName(), each.getSql(), each.getParameterSets());
                result.addChildRequest(String.valueOf(each.hashCode()), each.getDataSourceName(), each.getSql(), each.getParameterSets(),
                                       revertResult.getRevertSQL(), revertResult.getRevertSQLParams());
            } catch (SQLException ex) {
                throw new ShardingException(String.format("Revert SQL %s failed: ", each.toString()), ex);
            }
        }
        return result;
    }
    
    private SagaDefinitionBuilder createDefinitionBuilder() {
        return new SagaDefinitionBuilder(sagaConfiguration.getRecoveryPolicy().getName(), sagaConfiguration.getTransactionMaxRetries(),
                                         sagaConfiguration.getCompensationMaxRetries(), sagaConfiguration.getTransactionRetryDelay());
    }
}
