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

package org.apache.shardingsphere.infra.metadata.database.resource;

import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage resource.
 */
@Getter
public final class StorageResource {
    
    private final Map<StorageNodeName, DataSource> dataSources;
    
    private final Map<String, StorageNode> storageUnitNodeMap;
    
    private final Map<String, DataSource> wrappedDataSources;
    
    public StorageResource(final Map<StorageNodeName, DataSource> dataSources, final Map<String, StorageNode> storageUnitNodeMap) {
        this.dataSources = dataSources;
        this.storageUnitNodeMap = storageUnitNodeMap;
        wrappedDataSources = createWrappedDataSources();
    }
    
    private Map<String, DataSource> createWrappedDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(storageUnitNodeMap.size(), 1F);
        for (Entry<String, StorageNode> entry : storageUnitNodeMap.entrySet()) {
            StorageNode storageNode = entry.getValue();
            DataSource dataSource = dataSources.get(storageNode.getName());
            if (null != dataSource) {
                result.put(entry.getKey(), new CatalogSwitchableDataSource(dataSource, storageNode.getCatalog(), storageNode.getUrl()));
            }
        }
        return result;
    }
}
