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

package org.apache.shardingsphere.metadata.persist.service.schema;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlViewSwapper;
import org.apache.shardingsphere.metadata.persist.node.metadata.ViewMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * View meta data persist service.
 */
@RequiredArgsConstructor
public final class ViewMetaDataPersistService implements SchemaMetaDataPersistService<Map<String, ShardingSphereView>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Map<String, ShardingSphereView> views) {
        Collection<MetaDataVersion> metaDataVersions = new LinkedList<>();
        for (Entry<String, ShardingSphereView> entry : views.entrySet()) {
            String viewName = entry.getKey().toLowerCase();
            List<String> versions = repository.getChildrenKeys(ViewMetaDataNode.getViewVersionsNode(databaseName, schemaName, viewName));
            String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(ViewMetaDataNode.getViewVersionNode(databaseName, schemaName, viewName, nextActiveVersion),
                    YamlEngine.marshal(new YamlViewSwapper().swapToYamlConfiguration(entry.getValue())));
            if (Strings.isNullOrEmpty(getActiveVersion(databaseName, schemaName, viewName))) {
                repository.persist(ViewMetaDataNode.getViewActiveVersionNode(databaseName, schemaName, viewName), DEFAULT_VERSION);
            }
            metaDataVersions.add(new MetaDataVersion(ViewMetaDataNode.getViewNode(databaseName, schemaName, viewName), getActiveVersion(databaseName, schemaName, viewName), nextActiveVersion));
        }
        metaDataVersionPersistService.switchActiveVersion(metaDataVersions);
    }
    
    private String getActiveVersion(final String databaseName, final String schemaName, final String viewName) {
        return repository.getDirectly(ViewMetaDataNode.getViewActiveVersionNode(databaseName, schemaName, viewName));
    }
    
    @Override
    public Map<String, ShardingSphereView> load(final String databaseName, final String schemaName) {
        Collection<String> viewNames = repository.getChildrenKeys(ViewMetaDataNode.getMetaDataViewsNode(databaseName, schemaName));
        return viewNames.isEmpty() ? Collections.emptyMap() : getViewMetaDataByViewNames(databaseName, schemaName, viewNames);
    }
    
    @Override
    public Map<String, ShardingSphereView> load(final String databaseName, final String schemaName, final String viewName) {
        return getViewMetaDataByViewNames(databaseName, schemaName, Collections.singletonList(viewName));
    }
    
    private Map<String, ShardingSphereView> getViewMetaDataByViewNames(final String databaseName, final String schemaName, final Collection<String> viewNames) {
        Map<String, ShardingSphereView> result = new LinkedHashMap<>(viewNames.size(), 1F);
        viewNames.forEach(each -> {
            String view = repository.getDirectly(ViewMetaDataNode.getViewVersionNode(databaseName, schemaName, each,
                    repository.getDirectly(ViewMetaDataNode.getViewActiveVersionNode(databaseName, schemaName, each))));
            if (!Strings.isNullOrEmpty(view)) {
                result.put(each.toLowerCase(), new YamlViewSwapper().swapToObject(YamlEngine.unmarshal(view, YamlShardingSphereView.class)));
            }
        });
        return result;
    }
    
    @Override
    public void delete(final String databaseName, final String schemaName, final String viewName) {
        repository.delete(ViewMetaDataNode.getViewNode(databaseName, schemaName, viewName.toLowerCase()));
    }
}
