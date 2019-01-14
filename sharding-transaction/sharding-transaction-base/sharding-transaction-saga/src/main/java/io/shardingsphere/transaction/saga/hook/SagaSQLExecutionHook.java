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

package io.shardingsphere.transaction.saga.hook;

import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.spi.executor.SQLExecutionHook;
import io.shardingsphere.transaction.saga.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.SagaSubTransaction;
import io.shardingsphere.transaction.saga.SagaTransaction;
import io.shardingsphere.transaction.saga.constant.SagaRecoveryPolicy;

import java.util.Map;

/**
 * Saga SQL execution hook.
 *
 * @author yangyi
 */
public final class SagaSQLExecutionHook implements SQLExecutionHook {
    
    private static final String TRANSACTION_KEY = "transaction";
    
    private SagaTransaction sagaTransaction;
    
    private SagaSubTransaction sagaSubTransaction;
    
    @Override
    public void start(final RouteUnit routeUnit, final DataSourceMetaData dataSourceMetaData, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
        if (shardingExecuteDataMap.containsKey(TRANSACTION_KEY)) {
            sagaTransaction = (SagaTransaction) shardingExecuteDataMap.get(TRANSACTION_KEY);
            sagaSubTransaction = new SagaSubTransaction(routeUnit.getDataSourceName(), routeUnit.getSqlUnit().getSql(), routeUnit.getSqlUnit().getParameterSets());
            sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.EXECUTING);
        }
    }
    
    @Override
    public void finishSuccess() {
        if (null != sagaTransaction && null != sagaSubTransaction) {
            sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.SUCCESS);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        if (null != sagaTransaction && null != sagaSubTransaction) {
            ExecutorExceptionHandler.setExceptionThrown(SagaRecoveryPolicy.BACKWARD == sagaTransaction.getSagaConfiguration().getRecoveryPolicy());
            sagaTransaction.recordResult(sagaSubTransaction, ExecutionResult.FAILURE);
        }
    }
}
