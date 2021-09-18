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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Storage node status service.
 */
@RequiredArgsConstructor
public final class StorageNodeStatusService {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Load storage node names.
     *
     * @param schemaName schema name to be loaded
     * @param status storage node status to be loaded
     * @return loaded storage node names
     */
    public Collection<String> loadStorageNodes(final String schemaName, final StorageNodeStatus status) {
        Collection<String> disabledStorageNodes = repository.getChildrenKeys(StorageStatusNode.getStatusPath(status));
        return disabledStorageNodes.stream().map(ClusterSchema::new).filter(each -> each.getSchemaName().equals(schemaName)).map(ClusterSchema::getDataSourceName).collect(Collectors.toList());
    }
}
