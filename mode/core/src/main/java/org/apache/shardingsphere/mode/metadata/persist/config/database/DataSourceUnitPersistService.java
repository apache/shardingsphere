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

package org.apache.shardingsphere.mode.metadata.persist.config.database;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.datasource.StorageUnitNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Data source unit persist service.
 */
public final class DataSourceUnitPersistService {
    
    private final PersistRepository repository;
    
    private final VersionPersistService versionPersistService;
    
    private final YamlDataSourceConfigurationSwapper yamlDataSourceConfigurationSwapper;
    
    public DataSourceUnitPersistService(final PersistRepository repository) {
        this.repository = repository;
        versionPersistService = new VersionPersistService(repository);
        yamlDataSourceConfigurationSwapper = new YamlDataSourceConfigurationSwapper();
    }
    
    /**
     * Load data source pool properties map.
     *
     * @param databaseName database name
     * @return data source pool properties map
     */
    public Map<String, DataSourcePoolProperties> load(final String databaseName) {
        Collection<String> childrenKeys = repository.getChildrenKeys(NodePathGenerator.toPath(new StorageUnitNodePath(databaseName, null)));
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(childrenKeys.size(), 1F);
        for (String each : childrenKeys) {
            load(databaseName, each).ifPresent(dataSourcePoolProps -> result.put(each, dataSourcePoolProps));
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
    public Optional<DataSourcePoolProperties> load(final String databaseName, final String dataSourceName) {
        VersionNodePath versionNodePath = new VersionNodePath(new StorageUnitNodePath(databaseName, dataSourceName));
        String activeVersion = repository.query(versionNodePath.getActiveVersionPath());
        if (Strings.isNullOrEmpty(activeVersion)) {
            return Optional.empty();
        }
        String dataSourceContent = repository.query(versionNodePath.getVersionPath(Integer.parseInt(activeVersion)));
        if (Strings.isNullOrEmpty(dataSourceContent)) {
            return Optional.empty();
        }
        return Optional.of(yamlDataSourceConfigurationSwapper.swapToDataSourcePoolProperties(YamlEngine.unmarshal(dataSourceContent, Map.class)));
    }
    
    /**
     * Persist data source pool properties map.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap to be persisted data source properties map
     */
    public void persist(final String databaseName, final Map<String, DataSourcePoolProperties> dataSourcePropsMap) {
        for (Entry<String, DataSourcePoolProperties> entry : dataSourcePropsMap.entrySet()) {
            VersionNodePath versionNodePath = new VersionNodePath(new StorageUnitNodePath(databaseName, entry.getKey()));
            versionPersistService.persist(versionNodePath, YamlEngine.marshal(yamlDataSourceConfigurationSwapper.swapToMap(entry.getValue())));
        }
    }
    
    /**
     * Delete data source pool configurations.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     */
    public void delete(final String databaseName, final String dataSourceName) {
        repository.delete(NodePathGenerator.toPath(new StorageUnitNodePath(databaseName, dataSourceName)));
    }
}
