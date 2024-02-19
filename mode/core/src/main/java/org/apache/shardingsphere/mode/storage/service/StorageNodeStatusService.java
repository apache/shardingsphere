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

package org.apache.shardingsphere.mode.storage.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.storage.node.StorageNode;
import org.apache.shardingsphere.mode.storage.yaml.YamlStorageNodeDataSource;
import org.apache.shardingsphere.mode.storage.yaml.YamlStorageNodeDataSourceSwapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage node status service.
 */
@RequiredArgsConstructor
public final class StorageNodeStatusService {
    
    private final PersistRepository repository;
    
    /**
     * Load storage node names.
     *
     * @return loaded storage node names
     */
    public Map<String, StorageNodeDataSource> loadStorageNodes() {
        Collection<String> storageNodes = repository.getChildrenKeys(StorageNode.getRootPath());
        Map<String, StorageNodeDataSource> result = new HashMap<>(storageNodes.size(), 1F);
        storageNodes.forEach(each -> {
            String yamlContent = repository.getDirectly(StorageNode.getStorageNodesDataSourcePath(each));
            if (!Strings.isNullOrEmpty(yamlContent)) {
                result.put(each, new YamlStorageNodeDataSourceSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlStorageNodeDataSource.class)));
            }
        });
        return result;
    }
    
    /**
     * Change member storage node status.
     *
     * @param databaseName database name
     * @param groupName group name
     * @param storageUnitName storage unit name
     * @param dataSourceState data source state
     */
    public void changeMemberStorageNodeStatus(final String databaseName, final String groupName, final String storageUnitName, final DataSourceState dataSourceState) {
        StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, dataSourceState);
        repository.persist(StorageNode.getStorageNodeDataSourcePath(new QualifiedDatabase(databaseName, groupName, storageUnitName)),
                YamlEngine.marshal(new YamlStorageNodeDataSourceSwapper().swapToYamlConfiguration(storageNodeDataSource)));
    }
}
