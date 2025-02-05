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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.mode.node.path.metadata.TableMetaDataNodePath;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Table meta data persist service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final YamlTableSwapper swapper = new YamlTableSwapper();
    
    /**
     * Load tables.
     *
     * @param databaseName to be loaded database name
     * @param schemaName to be loaded schema name
     * @return loaded tables
     */
    public Collection<ShardingSphereTable> load(final String databaseName, final String schemaName) {
        return repository.getChildrenKeys(TableMetaDataNodePath.getTableRootPath(databaseName, schemaName)).stream()
                .map(each -> load(databaseName, schemaName, each)).collect(Collectors.toList());
    }
    
    /**
     * Load table.
     *
     * @param databaseName to be loaded database name
     * @param schemaName to be loaded schema name
     * @param tableName to be loaded table name
     * @return loaded table
     */
    public ShardingSphereTable load(final String databaseName, final String schemaName, final String tableName) {
        Integer activeVersion = getActiveVersion(databaseName, schemaName, tableName);
        String tableContent = repository.query(
                TableMetaDataNodePath.getTableVersionPath(databaseName, schemaName, tableName, null == activeVersion ? MetaDataVersion.DEFAULT_VERSION : activeVersion));
        return swapper.swapToObject(YamlEngine.unmarshal(tableContent, YamlShardingSphereTable.class));
    }
    
    /**
     * Persist tables.
     *
     * @param databaseName to be persisted database name
     * @param schemaName to be persisted schema name
     * @param tables to be persisted tables
     */
    public void persist(final String databaseName, final String schemaName, final Collection<ShardingSphereTable> tables) {
        Collection<MetaDataVersion> metaDataVersions = new LinkedList<>();
        for (ShardingSphereTable each : tables) {
            String tableName = each.getName().toLowerCase();
            List<Integer> versions = metaDataVersionPersistService.getVersions(TableMetaDataNodePath.getTableVersionsPath(databaseName, schemaName, tableName));
            int nextActiveVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : versions.get(0) + 1;
            repository.persist(TableMetaDataNodePath.getTableVersionPath(databaseName, schemaName, tableName, nextActiveVersion), YamlEngine.marshal(swapper.swapToYamlConfiguration(each)));
            if (null == getActiveVersion(databaseName, schemaName, tableName)) {
                repository.persist(TableMetaDataNodePath.getTableActiveVersionPath(databaseName, schemaName, tableName), String.valueOf(MetaDataVersion.DEFAULT_VERSION));
            }
            metaDataVersions.add(
                    new MetaDataVersion(TableMetaDataNodePath.getTablePath(databaseName, schemaName, tableName), getActiveVersion(databaseName, schemaName, tableName), nextActiveVersion));
        }
        metaDataVersionPersistService.switchActiveVersion(metaDataVersions);
    }
    
    private Integer getActiveVersion(final String databaseName, final String schemaName, final String tableName) {
        String value = repository.query(TableMetaDataNodePath.getTableActiveVersionPath(databaseName, schemaName, tableName));
        return Strings.isNullOrEmpty(value) ? null : Integer.parseInt(value);
    }
    
    /**
     * Drop table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName to be dropped table name
     */
    public void drop(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(TableMetaDataNodePath.getTablePath(databaseName, schemaName, tableName.toLowerCase()));
    }
    
    /**
     * Drop tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tables to be dropped tables
     */
    public void drop(final String databaseName, final String schemaName, final Collection<ShardingSphereTable> tables) {
        tables.forEach(each -> drop(databaseName, schemaName, each.getName()));
    }
}
