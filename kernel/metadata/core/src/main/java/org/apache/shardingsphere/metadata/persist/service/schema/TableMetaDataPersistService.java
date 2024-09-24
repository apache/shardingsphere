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

import com.google.common.base.Preconditions;
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
import java.util.Optional;

/**
 * Table meta data persist service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    /**
     * Persist tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tables tables
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
     * Load tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return loaded tables
     */
    public Map<String, ShardingSphereTable> load(final String databaseName, final String schemaName) {
        List<String> tableNames = repository.getChildrenKeys(TableMetaDataNode.getMetaDataTablesNode(databaseName, schemaName));
        Map<String, ShardingSphereTable> result = new LinkedHashMap<>(tableNames.size(), 1F);
        for (String each : tableNames) {
            getTableMetaData(databaseName, schemaName, each).ifPresent(optional -> result.put(each.toLowerCase(), optional));
        }
        return result;
    }
    
    /**
     * Load table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return loaded table
     */
    public ShardingSphereTable load(final String databaseName, final String schemaName, final String tableName) {
        Optional<ShardingSphereTable> result = getTableMetaData(databaseName, schemaName, tableName);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
    
    private Optional<ShardingSphereTable> getTableMetaData(final String databaseName, final String schemaName, final String tableName) {
        String tableContent = repository.query(TableMetaDataNode.getTableVersionNode(databaseName, schemaName, tableName,
                repository.query(TableMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName))));
        return Strings.isNullOrEmpty(tableContent) ? Optional.empty() : Optional.of(new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(tableContent, YamlShardingSphereTable.class)));
    }
    
    /**
     * Delete table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public void delete(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(TableMetaDataNode.getTableNode(databaseName, schemaName, tableName.toLowerCase()));
    }
}
