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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * TODO Rename DataSourcePersistService when metadata structure adjustment completed. #25485
 * New Data source persist service.
 */
@RequiredArgsConstructor
public final class NewDataSourcePersistService implements DatabaseBasedPersistService<Map<String, DataSourceProperties>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final Map<String, DataSourceProperties> dataSourceConfigs) {
        for (Entry<String, DataSourceProperties> entry : dataSourceConfigs.entrySet()) {
            String activeVersion = getDatabaseActiveVersion(databaseName, entry.getKey());
            if (Strings.isNullOrEmpty(activeVersion)) {
                repository.persist(NewDatabaseMetaDataNode.getDataSourceActiveVersionPath(databaseName, entry.getKey()), DEFAULT_VERSION);
            }
            repository.persist(NewDatabaseMetaDataNode.getDataSourcePath(databaseName, entry.getKey(), DEFAULT_VERSION),
                    YamlEngine.marshal(new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue())));
        }
    }
    
    @Override
    public Map<String, DataSourceProperties> load(final String databaseName) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        for (String each : repository.getChildrenKeys(NewDatabaseMetaDataNode.getDataSourcesPath(databaseName))) {
            result.put(each, getDataSourceProps(databaseName, each));
        }
        return result;
    }
    
    // TODO Remove this
    @Override
    public Map<String, DataSourceProperties> load(final String databaseName, final String version) {
        return Collections.emptyMap();
    }
    
    private DataSourceProperties getDataSourceProps(final String databaseName, final String dataSourceName) {
        String result = repository.getDirectly(NewDatabaseMetaDataNode.getDataSourcePath(databaseName, getDatabaseActiveVersion(databaseName, dataSourceName), dataSourceName));
        Preconditions.checkState(!Strings.isNullOrEmpty(result), "Not found `%s` data source config in `%s` database", dataSourceName, databaseName);
        return YamlEngine.unmarshal(result, DataSourceProperties.class);
    }
    
    private String getDatabaseActiveVersion(final String databaseName, final String dataSourceName) {
        return repository.getDirectly(NewDatabaseMetaDataNode.getDataSourceActiveVersionPath(databaseName, dataSourceName));
    }
}
