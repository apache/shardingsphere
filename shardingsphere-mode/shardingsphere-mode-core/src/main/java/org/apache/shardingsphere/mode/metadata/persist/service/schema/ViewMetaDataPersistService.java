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

package org.apache.shardingsphere.mode.metadata.persist.service.schema;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlViewSwapper;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * View meta data persist service.
 */
@RequiredArgsConstructor
public final class ViewMetaDataPersistService implements SchemaMetaDataPersistService<Map<String, ShardingSphereView>> {
    
    private final PersistRepository repository;
    
    @Override
    public void compareAndPersist(final String databaseName, final String schemaName, final Map<String, ShardingSphereView> loadedViews) {
        // TODO Add ShardingSphereSchemaFactory to support getToBeAddedViews and getToBeDeletedViews.
        Map<String, ShardingSphereView> currentViews = load(databaseName, schemaName);
        persist(databaseName, schemaName, getToBeAddedViews(loadedViews, currentViews));
        getToBeDeletedViews(loadedViews, currentViews).forEach((key, value) -> delete(databaseName, schemaName, key));
    }
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Map<String, ShardingSphereView> views) {
        views.forEach((key, value) -> repository.persist(DatabaseMetaDataNode.getViewMetaDataPath(databaseName, schemaName, key.toLowerCase()),
                YamlEngine.marshal(new YamlViewSwapper().swapToYamlConfiguration(value))));
    }
    
    @Override
    public Map<String, ShardingSphereView> load(final String databaseName, final String schemaName) {
        Collection<String> viewNames = repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataViewsPath(databaseName, schemaName));
        return viewNames.isEmpty() ? Collections.emptyMap() : getViewMetaDataByViewNames(databaseName, schemaName, viewNames);
    }
    
    @Override
    public void delete(final String databaseName, final String schemaName, final String viewName) {
        repository.delete(DatabaseMetaDataNode.getViewMetaDataPath(databaseName, schemaName, viewName.toLowerCase()));
    }
    
    private Map<String, ShardingSphereView> getToBeAddedViews(final Map<String, ShardingSphereView> loadedViews, final Map<String, ShardingSphereView> currentViews) {
        Map<String, ShardingSphereView> result = new LinkedHashMap<>(loadedViews.size(), 1);
        for (Map.Entry<String, ShardingSphereView> entry : loadedViews.entrySet()) {
            ShardingSphereView currentView = currentViews.get(entry.getKey());
            if (null != currentView && !entry.getValue().equals(currentView)) {
                result.put(entry.getKey(), entry.getValue());
            } else if (null == currentView) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, ShardingSphereView> getToBeDeletedViews(final Map<String, ShardingSphereView> loadedViews, final Map<String, ShardingSphereView> currentViews) {
        return currentViews.entrySet().stream().filter(entry -> !loadedViews.containsKey(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, ShardingSphereView> getViewMetaDataByViewNames(final String databaseName, final String schemaName, final Collection<String> viewNames) {
        Map<String, ShardingSphereView> result = new LinkedHashMap<>(viewNames.size(), 1);
        viewNames.forEach(each -> {
            String view = repository.get(DatabaseMetaDataNode.getViewMetaDataPath(databaseName, schemaName, each));
            if (!StringUtils.isEmpty(view)) {
                result.put(each.toLowerCase(), new YamlViewSwapper().swapToObject(YamlEngine.unmarshal(view, YamlShardingSphereView.class)));
            }
        });
        return result;
    }
}
