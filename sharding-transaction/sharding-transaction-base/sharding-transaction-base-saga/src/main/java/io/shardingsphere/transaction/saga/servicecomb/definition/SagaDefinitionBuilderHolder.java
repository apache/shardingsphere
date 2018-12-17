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

package io.shardingsphere.transaction.saga.servicecomb.definition;

import io.shardingsphere.api.config.SagaConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga definition builder holder.
 *
 * @author yangyi
 */
public final class SagaDefinitionBuilderHolder {
    
    private final Map<String, SagaDefinitionBuilder> sagaDefinitionBuilderMap = new ConcurrentHashMap<>();
    
    /**
     * Create saga definition builder according transaction id and saga configuration.
     *
     * @param transactionId transaction id
     * @param sagaConfiguration saga configuration
     */
    public void createSagaDefinitionBuilder(final String transactionId, final SagaConfiguration sagaConfiguration) {
        sagaDefinitionBuilderMap.put(transactionId, new SagaDefinitionBuilder(sagaConfiguration.getRecoveryPolicy().getName(),
            sagaConfiguration.getTransactionMaxRetries(), sagaConfiguration.getCompensationMaxRetries(), sagaConfiguration.getTransactionRetryDelay()));
    
    }
    
    /**
     * Get saga definition builder according transaction id.
     *
     * @param transactionId transaction id
     * @return saga definition builder for transaction id
     */
    public SagaDefinitionBuilder getSagaDefinitionBuilder(final String transactionId) {
        return sagaDefinitionBuilderMap.get(transactionId);
    }
    
    /**
     * Remove and get saga definition builder according transaction id.
     *
     * @param transactionId transaction id
     * @return saga definition builder for transaction id
     */
    public SagaDefinitionBuilder removeSagaDefinitionBuilder(final String transactionId) {
        return sagaDefinitionBuilderMap.remove(transactionId);
    }
}
