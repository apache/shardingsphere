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

import org.apache.servicecomb.saga.core.PersistentStore;

/**
 * Saga persistence.
 *
 * @author yangyi
 */
public interface SagaPersistence extends PersistentStore {
    
    /**
     * Persist saga snapshot.
     *
     * @param snapshot saga snapshot
     */
    void persistSnapshot(SagaSnapshot snapshot);
    
    /**
     * Update snapshot execute status.
     *
     * @param transactionId transaction id
     * @param snapshotId snapshot id
     * @param executeStatus new execute status
     */
    void updateSnapshotStatus(String transactionId, int snapshotId, String executeStatus);
    
    /**
     * Clean snapshot for target transaction.
     *
     * @param transactionId transaction id
     */
    void cleanSnapshot(String transactionId);
    
    /**
     * Clean saga event.
     *
     * @param sagaId saga id
     */
    void cleanSagaEvent(String sagaId);
}
