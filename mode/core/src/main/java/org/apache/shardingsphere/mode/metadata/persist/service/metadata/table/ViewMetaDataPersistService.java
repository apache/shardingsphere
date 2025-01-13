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

package org.apache.shardingsphere.mode.metadata.persist.service.metadata.table;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlViewSwapper;
import org.apache.shardingsphere.mode.node.path.metadata.ViewMetaDataNodePath;
import org.apache.shardingsphere.mode.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View meta data persist service.
 */
@RequiredArgsConstructor
public final class ViewMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    /**
     * Load views.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return loaded views
     */
    public Collection<ShardingSphereView> load(final String databaseName, final String schemaName) {
        return repository.getChildrenKeys(ViewMetaDataNodePath.getMetaDataViewsPath(databaseName, schemaName)).stream().map(each -> load(databaseName, schemaName, each)).collect(Collectors.toList());
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
        String view = repository.query(ViewMetaDataNodePath.getViewVersionPath(databaseName, schemaName, viewName,
                repository.query(ViewMetaDataNodePath.getViewActiveVersionPath(databaseName, schemaName, viewName))));
        return new YamlViewSwapper().swapToObject(YamlEngine.unmarshal(view, YamlShardingSphereView.class));
    }
    
    /**
     * Persist views.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param views views
     */
    public void persist(final String databaseName, final String schemaName, final Collection<ShardingSphereView> views) {
        Collection<MetaDataVersion> metaDataVersions = new LinkedList<>();
        for (ShardingSphereView each : views) {
            String viewName = each.getName().toLowerCase();
            List<String> versions = metaDataVersionPersistService.getVersions(ViewMetaDataNodePath.getViewVersionsPath(databaseName, schemaName, viewName));
            String nextActiveVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(ViewMetaDataNodePath.getViewVersionPath(databaseName, schemaName, viewName, nextActiveVersion),
                    YamlEngine.marshal(new YamlViewSwapper().swapToYamlConfiguration(each)));
            if (Strings.isNullOrEmpty(getActiveVersion(databaseName, schemaName, viewName))) {
                repository.persist(ViewMetaDataNodePath.getViewActiveVersionPath(databaseName, schemaName, viewName), MetaDataVersion.DEFAULT_VERSION);
            }
            metaDataVersions.add(new MetaDataVersion(ViewMetaDataNodePath.getViewPath(databaseName, schemaName, viewName), getActiveVersion(databaseName, schemaName, viewName), nextActiveVersion));
        }
        metaDataVersionPersistService.switchActiveVersion(metaDataVersions);
    }
    
    private String getActiveVersion(final String databaseName, final String schemaName, final String viewName) {
        return repository.query(ViewMetaDataNodePath.getViewActiveVersionPath(databaseName, schemaName, viewName));
    }
    
    /**
     * Delete view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     */
    public void delete(final String databaseName, final String schemaName, final String viewName) {
        repository.delete(ViewMetaDataNodePath.getViewPath(databaseName, schemaName, viewName.toLowerCase()));
    }
}
