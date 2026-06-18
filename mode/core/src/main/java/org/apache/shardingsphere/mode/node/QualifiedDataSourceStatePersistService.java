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

package org.apache.shardingsphere.mode.node;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.yaml.YamlQualifiedDataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.yaml.YamlQualifiedDataSourceStateSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.node.storage.QualifiedDataSourceNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Qualified data source state persist service.
 */
@RequiredArgsConstructor
public final class QualifiedDataSourceStatePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Load qualified data source states.
     *
     * @return loaded qualified data source states
     */
    public Map<String, QualifiedDataSourceState> load() {
        Collection<String> qualifiedDataSourceNodes = repository.getChildrenKeys(NodePathGenerator.toPath(new QualifiedDataSourceNodePath((String) null)));
        Map<String, QualifiedDataSourceState> result = new HashMap<>(qualifiedDataSourceNodes.size(), 1F);
        qualifiedDataSourceNodes.forEach(each -> {
            String yamlContent = repository.query(NodePathGenerator.toPath(new QualifiedDataSourceNodePath(new QualifiedDataSource(each))));
            if (!Strings.isNullOrEmpty(yamlContent)) {
                result.put(each, new YamlQualifiedDataSourceStateSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlQualifiedDataSourceState.class)));
            }
        });
        return result;
    }
    
    /**
     * Update qualified data source state.
     *
     * @param databaseName to be updated database name
     * @param groupName to be updated group name
     * @param storageUnitName to be updated storage unit name
     * @param dataSourceState to be updated data source state
     */
    public void update(final String databaseName, final String groupName, final String storageUnitName, final DataSourceState dataSourceState) {
        QualifiedDataSourceState status = new QualifiedDataSourceState(dataSourceState);
        repository.persist(NodePathGenerator.toPath(new QualifiedDataSourceNodePath(new QualifiedDataSource(databaseName, groupName, storageUnitName))),
                YamlEngine.marshal(new YamlQualifiedDataSourceStateSwapper().swapToYamlConfiguration(status)));
    }
}
