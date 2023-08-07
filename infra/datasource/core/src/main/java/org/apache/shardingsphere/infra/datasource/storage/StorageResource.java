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

package org.apache.shardingsphere.infra.datasource.storage;

import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.ShardingSphereStorageDataSourceWrapper;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage resource.
 */
@Getter
public class StorageResource {
    
    private final Map<String, DataSource> storageNodes;
    
    private final Map<String, StorageUnit> storageUnits;
    
    private final Map<String, DataSource> wrappedDataSources;
    
    public StorageResource(final Map<String, DataSource> storageNodes, final Map<String, StorageUnit> storageUnits) {
        this.storageNodes = storageNodes;
        this.storageUnits = storageUnits;
        wrappedDataSources = getWrappedDataSources(storageUnits);
    }
    
    private Map<String, DataSource> getWrappedDataSources(final Map<String, StorageUnit> storageUnits) {
        Map<String, DataSource> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            DataSource dataSource = storageNodes.get(entry.getValue().getNodeName());
            if (null != dataSource) {
                result.put(entry.getKey(), new ShardingSphereStorageDataSourceWrapper(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl()));
            }
        }
        return result;
    }
}
