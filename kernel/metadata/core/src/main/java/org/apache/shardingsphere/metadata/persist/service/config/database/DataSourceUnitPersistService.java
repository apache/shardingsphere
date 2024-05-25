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

package org.apache.shardingsphere.metadata.persist.service.config.database;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.metadata.persist.node.metadata.DataSourceMetaDataNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source unit persist service.
 */
@RequiredArgsConstructor
public final class DataSourceUnitPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceConfigs data source pool configurations
     */
    public void persist(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            String activeVersion = getDataSourceActiveVersion(databaseName, entry.getKey());
            List<String> versions = repository.getChildrenKeys(DataSourceMetaDataNode.getDataSourceUnitVersionsNode(databaseName, entry.getKey()));
            repository.persist(DataSourceMetaDataNode.getDataSourceUnitVersionNode(databaseName, entry.getKey(), versions.isEmpty()
                    ? MetaDataVersion.DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(activeVersion)) {
                repository.persist(DataSourceMetaDataNode.getDataSourceUnitActiveVersionNode(databaseName, entry.getKey()), MetaDataVersion.DEFAULT_VERSION);
            }
        }
    }
    
    /**
     * Load data source pool configurations.
     *
     * @param databaseName database name
     * @return data source pool configurations
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourcePoolProperties> load(final String databaseName) {
        Collection<String> childrenKeys = repository.getChildrenKeys(DataSourceMetaDataNode.getDataSourceUnitsNode(databaseName));
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(childrenKeys.size(), 1F);
        for (String each : childrenKeys) {
            String dataSourceValue = repository.query(DataSourceMetaDataNode.getDataSourceUnitVersionNode(databaseName, each, getDataSourceActiveVersion(databaseName, each)));
            if (!Strings.isNullOrEmpty(dataSourceValue)) {
                result.put(each, new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class)));
            }
        }
        return result;
    }
    
    /**
     * Load data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source pool configurations
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourcePoolProperties> load(final String databaseName, final String dataSourceName) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(1, 1F);
        String dataSourceValue = repository.query(DataSourceMetaDataNode.getDataSourceUnitVersionNode(databaseName, dataSourceName, getDataSourceActiveVersion(databaseName, dataSourceName)));
        if (!Strings.isNullOrEmpty(dataSourceValue)) {
            result.put(dataSourceName, new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class)));
        }
        return result;
    }
    
    /**
     * Delete data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     */
    public void delete(final String databaseName, final String dataSourceName) {
        repository.delete(DataSourceMetaDataNode.getDataSourceUnitNode(databaseName, dataSourceName));
    }
    
    /**
     * Persist data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceConfigs to be persisted configurations
     * @return meta data versions
     */
    public Collection<MetaDataVersion> persistConfigurations(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            List<String> versions = repository.getChildrenKeys(DataSourceMetaDataNode.getDataSourceUnitVersionsNode(databaseName, entry.getKey()));
            String nextActiveVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(DataSourceMetaDataNode.getDataSourceUnitVersionNode(databaseName, entry.getKey(), nextActiveVersion),
                    YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(getDataSourceActiveVersion(databaseName, entry.getKey()))) {
                repository.persist(DataSourceMetaDataNode.getDataSourceUnitActiveVersionNode(databaseName, entry.getKey()), MetaDataVersion.DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(DataSourceMetaDataNode.getDataSourceUnitNode(databaseName, entry.getKey()), getDataSourceActiveVersion(databaseName, entry.getKey()), nextActiveVersion));
        }
        return result;
    }
    
    private String getDataSourceActiveVersion(final String databaseName, final String dataSourceName) {
        return repository.query(DataSourceMetaDataNode.getDataSourceUnitActiveVersionNode(databaseName, dataSourceName));
    }
}
