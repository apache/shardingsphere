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

package org.apache.shardingsphere.mode.manager.switcher;

import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Resource switch manager.
 */
public final class ResourceSwitchManager {
    
    /**
     * Create switching resource.
     * 
     * @param resource resource
     * @param toBeChangedDataSourceProps to be changed data source properties map
     * @return created switching resource
     */
    public SwitchingResource create(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        return new SwitchingResource(resource, createNewDataSources(resource, toBeChangedDataSourceProps), getStaleDataSources(resource, toBeChangedDataSourceProps));
    }
    
    /**
     * Create switching resource.
     *
     * @param resource resource
     * @param toBeDeletedDataSourceProps to be deleted data source properties map
     * @return created switching resource
     */
    public SwitchingResource createByDropResource(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeDeletedDataSourceProps) {
        return new SwitchingResource(resource, Collections.emptyMap(), getStaleDataSources(resource.getDataSources(), toBeDeletedDataSourceProps));
    }
    
    private Map<String, DataSource> createNewDataSources(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        Map<String, DataSource> result = new LinkedHashMap<>(resource.getDataSources());
        result.keySet().removeAll(getToBeDeletedDataSources(resource, toBeChangedDataSourceProps).keySet());
        result.putAll(createToBeChangedDataSources(resource, toBeChangedDataSourceProps));
        result.putAll(createToBeAddedDataSources(resource, toBeChangedDataSourceProps));
        return result;
    }
    
    private Map<String, DataSource> createToBeChangedDataSources(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        return DataSourcePoolCreator.create(getChangedDataSourceProperties(resource, toBeChangedDataSourceProps));
    }
    
    private Map<String, DataSourceProperties> getChangedDataSourceProperties(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        return toBeChangedDataSourceProps.entrySet().stream().filter(entry -> isModifiedDataSource(resource.getDataSources(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedDataSource(final Map<String, DataSource> originalDataSources, final String dataSourceName, final DataSourceProperties dataSourceProps) {
        return originalDataSources.containsKey(dataSourceName) && !dataSourceProps.equals(DataSourcePropertiesCreator.create(originalDataSources.get(dataSourceName)));
    }
    
    private Map<String, DataSource> createToBeAddedDataSources(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        Map<String, DataSourceProperties> toBeAddedDataSourceProps = toBeChangedDataSourceProps.entrySet().stream()
                .filter(entry -> !resource.getDataSources().containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return DataSourcePoolCreator.create(toBeAddedDataSourceProps);
    }
    
    private Map<String, DataSource> getStaleDataSources(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        Map<String, DataSource> result = new LinkedHashMap<>(resource.getDataSources().size(), 1);
        result.putAll(getToBeChangedDataSources(resource, toBeChangedDataSourceProps));
        return result;
    }
    
    private Map<String, DataSource> getStaleDataSources(final Map<String, DataSource> dataSources, final Map<String, DataSourceProperties> toBeDeletedDataSourceProps) {
        return dataSources.entrySet().stream().filter(entry -> toBeDeletedDataSourceProps.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSource> getToBeDeletedDataSources(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        return resource.getDataSources().entrySet().stream().filter(entry -> !toBeChangedDataSourceProps.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSource> getToBeChangedDataSources(final ShardingSphereResource resource, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        Map<String, DataSourceProperties> changedDataSourceProps = getChangedDataSourceProperties(resource, toBeChangedDataSourceProps);
        return resource.getDataSources().entrySet().stream().filter(entry -> changedDataSourceProps.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
