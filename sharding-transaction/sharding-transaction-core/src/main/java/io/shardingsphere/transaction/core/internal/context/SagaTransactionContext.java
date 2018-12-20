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

package io.shardingsphere.transaction.core.internal.context;

import io.shardingsphere.api.config.SagaConfiguration;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Saga transaction context.
 *
 * @author yangyi
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class SagaTransactionContext implements ShardingTransactionContext {
    
    private final TransactionOperationType operationType;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final SagaConfiguration sagaConfiguration;
    
    private final SagaSQLExecutionContext sagaSQLExecutionContext;
    
    private boolean destroyComponent;
    
    /**
     * Create begin saga transaction context.
     *
     * @param dataSourceMap sharding data source map
     * @param sagaConfiguration saga configuration
     * @return begin saga transaction context
     */
    public static SagaTransactionContext createBeginSagaTransactionContext(final Map<String, DataSource> dataSourceMap, final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionContext(TransactionOperationType.BEGIN, dataSourceMap, sagaConfiguration, null);
    }
    
    /**
     * Create commit saga transaction context.
     *
     * @param sagaConfiguration saga configuration
     * @return commit saga transaction context
     */
    public static SagaTransactionContext createCommitSagaTransactionContext(final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionContext(TransactionOperationType.COMMIT, null, sagaConfiguration, null);
    }
    
    /**
     * Create rollback saga transaction context.
     *
     * @param sagaConfiguration saga configuration
     * @return rollback saga transaction context
     */
    public static SagaTransactionContext createRollbackSagaTransactionContext(final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionContext(TransactionOperationType.ROLLBACK, null, sagaConfiguration, null);
    }
    
    /**
     * Create execution saga transaction context.
     *
     * @param sagaSQLExecutionContext saga SQL execution context
     * @return execution saga transaction context
     */
    public static SagaTransactionContext createExecutionSagaTransactionContext(final SagaSQLExecutionContext sagaSQLExecutionContext) {
        return new SagaTransactionContext(null, null, null, sagaSQLExecutionContext);
    }
    
    /**
     * Create destroy component context.
     *
     * @param sagaConfiguration saga configuration
     * @return destroy component context
     */
    public static SagaTransactionContext createDestroyComponentContext(final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionContext(null, null, sagaConfiguration, null, true);
    }
    
    /**
     * Judge whether context is execution context.
     *
     * @return true if context is execution context, else false
     */
    public boolean isExecutionEvent() {
        return null != sagaSQLExecutionContext;
    }
}
