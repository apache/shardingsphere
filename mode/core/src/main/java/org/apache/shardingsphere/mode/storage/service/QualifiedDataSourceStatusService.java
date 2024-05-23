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

package org.apache.shardingsphere.mode.storage.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.storage.QualifiedDataSourceStatus;
import org.apache.shardingsphere.mode.storage.node.QualifiedDataSourceNode;
import org.apache.shardingsphere.mode.storage.yaml.YamlQualifiedDataSourceStatus;
import org.apache.shardingsphere.mode.storage.yaml.YamlQualifiedDataSourceStatusSwapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Qualified data source status service.
 */
@RequiredArgsConstructor
public final class QualifiedDataSourceStatusService {
    
    private final PersistRepository repository;
    
    /**
     * Load qualified data source status.
     *
     * @return qualified data source status
     */
    public Map<String, QualifiedDataSourceStatus> loadStatus() {
        Collection<String> qualifiedDataSourceNodes = repository.getChildrenKeys(QualifiedDataSourceNode.getRootPath());
        Map<String, QualifiedDataSourceStatus> result = new HashMap<>(qualifiedDataSourceNodes.size(), 1F);
        qualifiedDataSourceNodes.forEach(each -> {
            String yamlContent = repository.query(QualifiedDataSourceNode.getQualifiedDataSourceNodePath(new QualifiedDataSource(each)));
            if (!Strings.isNullOrEmpty(yamlContent)) {
                result.put(each, new YamlQualifiedDataSourceStatusSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlQualifiedDataSourceStatus.class)));
            }
        });
        return result;
    }
    
    /**
     * Change qualified data source status.
     *
     * @param databaseName database name
     * @param groupName group name
     * @param storageUnitName storage unit name
     * @param dataSourceState data source state
     */
    public void changeStatus(final String databaseName, final String groupName, final String storageUnitName, final DataSourceState dataSourceState) {
        QualifiedDataSourceStatus status = new QualifiedDataSourceStatus(dataSourceState);
        repository.persist(QualifiedDataSourceNode.getQualifiedDataSourceNodePath(
                new QualifiedDataSource(databaseName, groupName, storageUnitName)), YamlEngine.marshal(new YamlQualifiedDataSourceStatusSwapper().swapToYamlConfiguration(status)));
    }
}
