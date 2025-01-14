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

package org.apache.shardingsphere.mode.metadata.persist.service.config.database;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.mode.node.path.metadata.DataSourceMetaDataNodePath;
import org.apache.shardingsphere.mode.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source node persist service.
 */
public final class DataSourceNodePersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    public DataSourceNodePersistService(final PersistRepository repository) {
        this.repository = repository;
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
    }
    
    /**
     * Load data source pool properties map.
     *
     * @param databaseName database name
     * @return data source pool properties map
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourcePoolProperties> load(final String databaseName) {
        Collection<String> childrenKeys = repository.getChildrenKeys(DataSourceMetaDataNodePath.getStorageNodesPath(databaseName));
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(childrenKeys.size(), 1F);
        for (String each : childrenKeys) {
            String dataSourceValue = repository.query(DataSourceMetaDataNodePath.getStorageNodeVersionPath(databaseName, each, getDataSourceActiveVersion(databaseName, each)));
            if (!Strings.isNullOrEmpty(dataSourceValue)) {
                result.put(each, new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class)));
            }
        }
        return result;
    }
    
    /**
     * Load data source pool properties.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source pool properties
     */
    @SuppressWarnings("unchecked")
    public DataSourcePoolProperties load(final String databaseName, final String dataSourceName) {
        String dataSourceValue = repository.query(DataSourceMetaDataNodePath.getStorageNodeVersionPath(databaseName, dataSourceName, getDataSourceActiveVersion(databaseName, dataSourceName)));
        return new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class));
    }
    
    /**
     * Persist data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceConfigs data source pool configurations
     */
    public void persist(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourceConfigs) {
        for (Entry<String, DataSourcePoolProperties> entry : dataSourceConfigs.entrySet()) {
            String activeVersion = getDataSourceActiveVersion(databaseName, entry.getKey());
            List<String> versions = metaDataVersionPersistService.getVersions(DataSourceMetaDataNodePath.getStorageNodeVersionsPath(databaseName, entry.getKey()));
            repository.persist(DataSourceMetaDataNodePath.getStorageNodeVersionPath(databaseName, entry.getKey(), versions.isEmpty()
                    ? MetaDataVersion.DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(activeVersion)) {
                repository.persist(DataSourceMetaDataNodePath.getStorageNodeActiveVersionPath(databaseName, entry.getKey()), MetaDataVersion.DEFAULT_VERSION);
            }
        }
    }
    
    private String getDataSourceActiveVersion(final String databaseName, final String dataSourceName) {
        return repository.query(DataSourceMetaDataNodePath.getStorageNodeActiveVersionPath(databaseName, dataSourceName));
    }
    
    /**
     * Delete data source pool configuration.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     */
    public void delete(final String databaseName, final String dataSourceName) {
        repository.delete(DataSourceMetaDataNodePath.getStorageNodePath(databaseName, dataSourceName));
    }
}
