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

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlViewSwapper;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.metadata.database.ViewMetadataNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * View meta data persist service.
 */
@RequiredArgsConstructor
public final class ViewMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final YamlViewSwapper swapper = new YamlViewSwapper();
    
    /**
     * Load views.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return loaded views
     */
    public Collection<ShardingSphereView> load(final String databaseName, final String schemaName) {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new ViewMetadataNodePath(databaseName, schemaName, null), false)).stream()
                .map(each -> load(databaseName, schemaName, each)).collect(Collectors.toList());
    }
    
    /**
     * Load view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return loaded view
     */
    public ShardingSphereView load(final String databaseName, final String schemaName, final String viewName) {
        VersionNodePath versionNodePath = NodePathGenerator.toVersionPath(new ViewMetadataNodePath(databaseName, schemaName, viewName));
        int activeVersion = Integer.parseInt(repository.query(versionNodePath.getActiveVersionPath()));
        String view = repository.query(versionNodePath.getVersionPath(activeVersion));
        return swapper.swapToObject(YamlEngine.unmarshal(view, YamlShardingSphereView.class));
    }
    
    /**
     * Persist views.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param views views
     */
    public void persist(final String databaseName, final String schemaName, final Collection<ShardingSphereView> views) {
        for (ShardingSphereView each : views) {
            String viewName = each.getName().toLowerCase();
            VersionNodePath versionNodePath = NodePathGenerator.toVersionPath(new ViewMetadataNodePath(databaseName, schemaName, viewName));
            metaDataVersionPersistService.persist(versionNodePath, YamlEngine.marshal(swapper.swapToYamlConfiguration(each)));
        }
    }
    
    /**
     * Drop view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName to be dropped view name
     */
    public void drop(final String databaseName, final String schemaName, final String viewName) {
        repository.delete(NodePathGenerator.toPath(new ViewMetadataNodePath(databaseName, schemaName, viewName.toLowerCase()), false));
    }
}
