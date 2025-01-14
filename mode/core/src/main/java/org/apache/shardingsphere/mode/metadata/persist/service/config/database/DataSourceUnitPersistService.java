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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source unit persist service.
 */
public final class DataSourceUnitPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    public DataSourceUnitPersistService(final PersistRepository repository) {
        this.repository = repository;
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
    }
    
    /**
     * Load data source pool properties map.
     *
     * @param databaseName database name
     * @return data source pool properties map
     */
    public Map<String, DataSourcePoolProperties> load(final String databaseName) {
        Collection<String> childrenKeys = repository.getChildrenKeys(DataSourceMetaDataNodePath.getStorageUnitsPath(databaseName));
        return childrenKeys.stream().collect(Collectors.toMap(each -> each, each -> load(databaseName, each), (a, b) -> b, () -> new LinkedHashMap<>(childrenKeys.size(), 1F)));
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
        String dataSourceValue = repository.query(DataSourceMetaDataNodePath.getStorageUnitVersionPath(databaseName, dataSourceName, getDataSourceActiveVersion(databaseName, dataSourceName)));
        return new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceValue, Map.class));
    }
    
    /**
     * Persist data source pool properties map.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap to be persisted data source properties map
     * @return meta data versions
     */
    public Collection<MetaDataVersion> persist(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourcePropsMap) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : dataSourcePropsMap.entrySet()) {
            String activeVersion = getDataSourceActiveVersion(databaseName, entry.getKey());
            List<String> versions = metaDataVersionPersistService.getVersions(DataSourceMetaDataNodePath.getStorageUnitVersionsPath(databaseName, entry.getKey()));
            String nextActiveVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(DataSourceMetaDataNodePath.getStorageUnitVersionPath(databaseName, entry.getKey(), nextActiveVersion),
                    YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
            if (Strings.isNullOrEmpty(activeVersion)) {
                repository.persist(DataSourceMetaDataNodePath.getStorageUnitActiveVersionPath(databaseName, entry.getKey()), MetaDataVersion.DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(DataSourceMetaDataNodePath.getStorageUnitPath(databaseName, entry.getKey()), activeVersion, nextActiveVersion));
        }
        return result;
    }
    
    private String getDataSourceActiveVersion(final String databaseName, final String dataSourceName) {
        return repository.query(DataSourceMetaDataNodePath.getStorageUnitActiveVersionPath(databaseName, dataSourceName));
    }
    
    /**
     * Delete data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     */
    public void delete(final String databaseName, final String dataSourceName) {
        repository.delete(DataSourceMetaDataNodePath.getStorageUnitPath(databaseName, dataSourceName));
    }
}
