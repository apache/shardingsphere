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

package org.apache.shardingsphere.metadata.persist.service.metadata.table;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.metadata.persist.node.metadata.TableMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Table meta data persist service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    /**
     * Load tables.
     *
     * @param databaseName to be loaded database name
     * @param schemaName to be loaded schema name
     * @return loaded tables
     */
    public Map<String, ShardingSphereTable> load(final String databaseName, final String schemaName) {
        List<String> tableNames = repository.getChildrenKeys(TableMetaDataNode.getMetaDataTablesNode(databaseName, schemaName));
        return tableNames.stream().collect(Collectors.toMap(String::toLowerCase, each -> load(databaseName, schemaName, each), (a, b) -> b, () -> new LinkedHashMap<>(tableNames.size(), 1F)));
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
        String tableContent = repository.query(TableMetaDataNode.getTableVersionNode(databaseName, schemaName, tableName,
                repository.query(TableMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName))));
        return new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(tableContent, YamlShardingSphereTable.class));
    }
    
    /**
     * Persist tables.
     *
     * @param databaseName to be persisted database name
     * @param schemaName to be persisted schema name
     * @param tables to be persisted tables
     */
    public void persist(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        Collection<MetaDataVersion> metaDataVersions = new LinkedList<>();
        for (Entry<String, ShardingSphereTable> entry : tables.entrySet()) {
            String tableName = entry.getKey().toLowerCase();
            List<String> versions = repository.getChildrenKeys(TableMetaDataNode.getTableVersionsNode(databaseName, schemaName, tableName));
            String nextActiveVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            if (null != entry.getValue()) {
                repository.persist(TableMetaDataNode.getTableVersionNode(databaseName, schemaName, tableName, nextActiveVersion),
                        YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(entry.getValue())));
            }
            if (Strings.isNullOrEmpty(getActiveVersion(databaseName, schemaName, tableName))) {
                repository.persist(TableMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName), MetaDataVersion.DEFAULT_VERSION);
            }
            metaDataVersions.add(new MetaDataVersion(TableMetaDataNode.getTableNode(databaseName, schemaName, tableName), getActiveVersion(databaseName, schemaName, tableName), nextActiveVersion));
        }
        metaDataVersionPersistService.switchActiveVersion(metaDataVersions);
    }
    
    private String getActiveVersion(final String databaseName, final String schemaName, final String tableName) {
        return repository.query(TableMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName));
    }
    
    /**
     * Drop table.
     *
     * @param databaseName to be dropped database name
     * @param schemaName to be dropped schema name
     * @param tableName to be dropped table name
     */
    public void drop(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(TableMetaDataNode.getTableNode(databaseName, schemaName, tableName.toLowerCase()));
    }
    
    /**
     * Drop tables.
     *
     * @param databaseName to be dropped database name
     * @param schemaName to be dropped schema name
     * @param tables to be dropped tables
     */
    public void drop(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        tables.forEach((key, value) -> drop(databaseName, schemaName, key));
    }
}
