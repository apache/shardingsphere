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
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.config.database.DatabaseBasedPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TODO Rename DataSourcePersistService when metadata structure adjustment completed. #25485
 * New Data source node persist service.
 */
@RequiredArgsConstructor
public final class NewDataSourceNodePersistService implements DatabaseBasedPersistService<Map<String, DataSourcePoolProperties>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            String activeVersion = getDataSourceActiveVersion(databaseName, entry.getKey());
            List<String> versions = repository.getChildrenKeys(NewDatabaseMetaDataNode.getDataSourceNodeVersionsNode(databaseName, entry.getKey()));
            repository.persist(NewDatabaseMetaDataNode.getDataSourceNodeWithVersion(databaseName, entry.getKey(), versions.isEmpty()
                    ? DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(activeVersion)) {
                repository.persist(NewDatabaseMetaDataNode.getDataSourceNodeActiveVersionNode(databaseName, entry.getKey()), DEFAULT_VERSION);
            }
        }
    }
    
    @Override
    public Collection<MetaDataVersion> deleteConfig(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            String delKey = NewDatabaseMetaDataNode.getDataSourceNode(databaseName, entry.getKey());
            repository.delete(delKey);
            result.add(new MetaDataVersion(delKey));
        }
        return result;
    }
    
    @Override
    public Collection<MetaDataVersion> persistConfig(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            List<String> versions = repository.getChildrenKeys(NewDatabaseMetaDataNode.getDataSourceNodeVersionsNode(databaseName, entry.getKey()));
            String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(NewDatabaseMetaDataNode.getDataSourceNodeWithVersion(databaseName, entry.getKey(), nextActiveVersion),
                    YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(getDataSourceActiveVersion(databaseName, entry.getKey()))) {
                repository.persist(NewDatabaseMetaDataNode.getDataSourceNodeActiveVersionNode(databaseName, entry.getKey()), DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(NewDatabaseMetaDataNode.getDataSourceNode(databaseName, entry.getKey()), getDataSourceActiveVersion(databaseName, entry.getKey()), nextActiveVersion));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, DataSourcePoolProperties> load(final String databaseName) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>();
        for (String each : repository.getChildrenKeys(NewDatabaseMetaDataNode.getDataSourceNodesNode(databaseName))) {
            String dataSourceValue = repository.getDirectly(NewDatabaseMetaDataNode.getDataSourceNodeWithVersion(databaseName, each, getDataSourceActiveVersion(databaseName, each)));
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
        String dataSourceValue = repository.getDirectly(NewDatabaseMetaDataNode.getDataSourceNodeWithVersion(databaseName, name, getDataSourceActiveVersion(databaseName, name)));
        if (!Strings.isNullOrEmpty(dataSourceValue)) {
            result.put(name, new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class)));
        }
        return result;
    }
    
    @Override
    public void append(final String databaseName, final Map<String, DataSourcePoolProperties> toBeAppendedPropsMap) {
        persist(databaseName, toBeAppendedPropsMap);
    }
    
    private String getDataSourceActiveVersion(final String databaseName, final String dataSourceName) {
        return repository.getDirectly(NewDatabaseMetaDataNode.getDataSourceNodeActiveVersionNode(databaseName, dataSourceName));
    }
}
