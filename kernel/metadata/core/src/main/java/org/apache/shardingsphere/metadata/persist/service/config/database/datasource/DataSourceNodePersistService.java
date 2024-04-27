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

package org.apache.shardingsphere.metadata.persist.service.config.database.datasource;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.config.database.DatabaseBasedPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source node persist service.
 */
@RequiredArgsConstructor
public final class DataSourceNodePersistService implements DatabaseBasedPersistService<Map<String, DataSourcePoolProperties>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            String activeVersion = getDataSourceActiveVersion(databaseName, entry.getKey());
            List<String> versions = repository.getChildrenKeys(DataSourceMetaDataNode.getDataSourceNodeVersionsNode(databaseName, entry.getKey()));
            repository.persist(DataSourceMetaDataNode.getDataSourceNodeVersionNode(databaseName, entry.getKey(), versions.isEmpty()
                    ? DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(activeVersion)) {
                repository.persist(DataSourceMetaDataNode.getDataSourceNodeActiveVersionNode(databaseName, entry.getKey()), DEFAULT_VERSION);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, DataSourcePoolProperties> load(final String databaseName) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>();
        for (String each : repository.getChildrenKeys(DataSourceMetaDataNode.getDataSourceNodesNode(databaseName))) {
            String dataSourceValue = repository.getDirectly(DataSourceMetaDataNode.getDataSourceNodeVersionNode(databaseName, each, getDataSourceActiveVersion(databaseName, each)));
            if (!Strings.isNullOrEmpty(dataSourceValue)) {
                result.put(each, new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class)));
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, DataSourcePoolProperties> load(final String databaseName, final String name) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>();
        String dataSourceValue = repository.getDirectly(DataSourceMetaDataNode.getDataSourceNodeVersionNode(databaseName, name, getDataSourceActiveVersion(databaseName, name)));
        if (!Strings.isNullOrEmpty(dataSourceValue)) {
            result.put(name, new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class)));
        }
        return result;
    }
    
    @Override
    public void delete(final String databaseName, final String name) {
        repository.delete(DataSourceMetaDataNode.getDataSourceNodeNode(databaseName, name));
    }
    
    @Override
    public Collection<MetaDataVersion> deleteConfigurations(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            String delKey = DataSourceMetaDataNode.getDataSourceNodeNode(databaseName, entry.getKey());
            repository.delete(delKey);
            result.add(new MetaDataVersion(delKey));
        }
        return result;
    }
    
    @Override
    public Collection<MetaDataVersion> persistConfigurations(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            List<String> versions = repository.getChildrenKeys(DataSourceMetaDataNode.getDataSourceNodeVersionsNode(databaseName, entry.getKey()));
            String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(DataSourceMetaDataNode.getDataSourceNodeVersionNode(databaseName, entry.getKey(), nextActiveVersion),
                    YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(getDataSourceActiveVersion(databaseName, entry.getKey()))) {
                repository.persist(DataSourceMetaDataNode.getDataSourceNodeActiveVersionNode(databaseName, entry.getKey()), DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(DataSourceMetaDataNode.getDataSourceNodeNode(databaseName, entry.getKey()), getDataSourceActiveVersion(databaseName, entry.getKey()), nextActiveVersion));
        }
        return result;
    }
    
    private String getDataSourceActiveVersion(final String databaseName, final String dataSourceName) {
        return repository.getDirectly(DataSourceMetaDataNode.getDataSourceNodeActiveVersionNode(databaseName, dataSourceName));
    }
}
