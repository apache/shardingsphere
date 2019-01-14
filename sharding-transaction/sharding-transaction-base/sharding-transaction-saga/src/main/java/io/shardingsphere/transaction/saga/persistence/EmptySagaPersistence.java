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

package io.shardingsphere.transaction.saga.persistence;

import org.apache.servicecomb.saga.core.EventEnvelope;
import org.apache.servicecomb.saga.core.SagaEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Empty saga persistence.
 *
 * @author yangyi
 */
public final class EmptySagaPersistence implements SagaPersistence {
    
    @Override
    public void persistSnapshot(final SagaSnapshot snapshot) {
    
    }
    
    @Override
    public void updateSnapshotStatus(final String transactionId, final int snapshotId, final String executeStatus) {
    
    }
    
    @Override
    public void cleanSnapshot(final String transactionId) {
    
    }
    
    @Override
    public void cleanSagaEvent(final String sagaId) {
    
    }
    
    @Override
    public Map<String, List<EventEnvelope>> findPendingSagaEvents() {
        return new HashMap<>();
    }
    
    @Override
    public void offer(final SagaEvent sagaEvent) {
    
    }
}
