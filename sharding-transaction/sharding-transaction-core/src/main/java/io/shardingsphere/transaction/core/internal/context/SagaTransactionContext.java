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
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
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
    
    private final SagaSQLExecutionEvent sagaSQLExecutionEvent;
    
    private boolean destroyComponent;
    
    /**
     * Create begin saga transaction event.
     *
     * @param dataSourceMap sharding data source map
     * @param sagaConfiguration saga configuration
     * @return begin saga transaction event
     */
    public static SagaTransactionEvent createBeginSagaTransactionEvent(final Map<String, DataSource> dataSourceMap, final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionEvent(TransactionOperationType.BEGIN, dataSourceMap, sagaConfiguration, null);
    }
    
    /**
     * Create commit saga transaction event.
     *
     * @param sagaConfiguration saga configuration
     * @return commit saga transaction event
     */
    public static SagaTransactionEvent createCommitSagaTransactionEvent(final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionEvent(TransactionOperationType.COMMIT, null, sagaConfiguration, null);
    }
    
    /**
     * Create rollback saga transaction event.
     *
     * @param sagaConfiguration saga configuration
     * @return rollback saga transaction event
     */
    public static SagaTransactionEvent createRollbackSagaTransactionEvent(final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionEvent(TransactionOperationType.ROLLBACK, null, sagaConfiguration, null);
    }
    
    /**
     * Create execution saga transaction event.
     *
     * @param sagaSQLExecutionEvent saga SQL execution event
     * @return execution saga transaction event
     */
    public static SagaTransactionEvent createExecutionSagaTransactionEvent(final SagaSQLExecutionEvent sagaSQLExecutionEvent) {
        return new SagaTransactionEvent(null, null, null, sagaSQLExecutionEvent);
    }
    
    /**
     * Create destroy component event.
     *
     * @param sagaConfiguration saga configuration
     * @return destroy component event
     */
    public static SagaTransactionEvent createDestroyComponentEvent(final SagaConfiguration sagaConfiguration) {
        return new SagaTransactionEvent(null, null, sagaConfiguration, null, true);
    }
    
    /**
     * Judge whether event is execution event.
     *
     * @return true if event is execution event, else false
     */
    public boolean isExecutionEvent() {
        return null != sagaSQLExecutionEvent;
    }
}
