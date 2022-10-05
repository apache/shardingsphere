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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage node status service.
 */
@RequiredArgsConstructor
public final class StorageNodeStatusService {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Load storage node names.
     *
     * @return loaded storage node names
     */
    public Map<String, StorageNodeDataSource> loadStorageNodes() {
        Collection<String> storageNodes = repository.getChildrenKeys(StorageNode.getRootPath());
        Map<String, StorageNodeDataSource> result = new HashMap<>(storageNodes.size(), 1);
        storageNodes.forEach(each -> {
            String yamlContext = repository.get(StorageNode.getStorageNodesDataSourcePath(each));
            if (!Strings.isNullOrEmpty(yamlContext)) {
                result.put(each, YamlEngine.unmarshal(yamlContext, StorageNodeDataSource.class));
            }
        });
        return result;
    }
}
