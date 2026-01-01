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

package org.apache.shardingsphere.proxy.frontend.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Connection ID generator.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ConnectionIdGenerator {
    
    private static final ConnectionIdGenerator INSTANCE = new ConnectionIdGenerator();
    
    private static final String CLUSTER_CONNECTION_ID_RESERVATION_PATH = "/nodes/compute_nodes/proxy_connection_id_reservation";
    
    private static final int MAX_CLUSTER_GENERATE_ATTEMPTS = 128;
    
    private int currentId;
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static ConnectionIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get next connection ID.
     *
     * @return next connection ID
     */
    public synchronized int nextId() {
        Integer clusterGeneratedId = tryGenerateClusterConnectionId();
        if (null != clusterGeneratedId) {
            return clusterGeneratedId;
        }
        if (currentId == Integer.MAX_VALUE) {
            currentId = 0;
        }
        return ++currentId;
    }
    
    /**
     * Release connection ID.
     *
     * <p>
     * In cluster mode, connection IDs may be reserved in a shared repository.
     * This method cleans up the reservation to avoid exhausting the 32-bit ID space.
     * </p>
     *
     * @param connectionId connection ID
     */
    public void releaseId(final int connectionId) {
        ClusterPersistRepository repository = getClusterRepository();
        if (null == repository) {
            return;
        }
        String instanceId = getInstanceId();
        if (null == instanceId) {
            return;
        }
        String key = getClusterReservationKey(connectionId);
        String owner = repository.query(key);
        if (instanceId.equals(owner)) {
            repository.delete(key);
        }
    }
    
    private Integer tryGenerateClusterConnectionId() {
        ClusterPersistRepository repository = getClusterRepository();
        if (null == repository) {
            return null;
        }
        String instanceId = getInstanceId();
        if (null == instanceId) {
            return null;
        }
        for (int i = 0; i < MAX_CLUSTER_GENERATE_ATTEMPTS; i++) {
            int candidateId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
            if (repository.persistExclusiveEphemeral(getClusterReservationKey(candidateId), instanceId)) {
                return candidateId;
            }
        }
        return null;
    }
    
    private String getInstanceId() {
        if (null == ProxyContext.getInstance().getContextManager()) {
            return null;
        }
        if (null == ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext()) {
            return null;
        }
        if (null == ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance()) {
            return null;
        }
        if (null == ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance().getMetaData()) {
            return null;
        }
        return ProxyContext.getInstance().getContextManager()
                .getComputeNodeInstanceContext()
                .getInstance()
                .getMetaData()
                .getId();
    }
    
    private ClusterPersistRepository getClusterRepository() {
        if (null == ProxyContext.getInstance().getContextManager()) {
            return null;
        }
        if (null == ProxyContext.getInstance().getContextManager().getPersistServiceFacade()) {
            return null;
        }
        PersistRepository repository = ProxyContext.getInstance()
                .getContextManager()
                .getPersistServiceFacade()
                .getRepository();
        return repository instanceof ClusterPersistRepository ? (ClusterPersistRepository) repository : null;
    }
    
    private String getClusterReservationKey(final int connectionId) {
        return CLUSTER_CONNECTION_ID_RESERVATION_PATH + PersistRepository.PATH_SEPARATOR + connectionId;
    }
}
