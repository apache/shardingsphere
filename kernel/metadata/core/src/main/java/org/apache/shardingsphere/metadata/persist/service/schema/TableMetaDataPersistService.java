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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

/**
 * Table meta data persist service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistService implements SchemaMetaDataPersistService<Map<String, ShardingSphereTable>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        for (Entry<String, ShardingSphereTable> entry : tables.entrySet()) {
            String tableName = entry.getKey().toLowerCase();
            List<String> versions = repository.getChildrenKeys(DatabaseMetaDataNode.getTableVersionsNode(databaseName, schemaName, tableName));
            repository.persist(DatabaseMetaDataNode.getTableVersionNode(databaseName, schemaName, tableName, versions.isEmpty()
                    ? DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(entry.getValue())));
            if (Strings.isNullOrEmpty(repository.getDirectly(DatabaseMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName)))) {
                repository.persist(DatabaseMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName), DEFAULT_VERSION);
            }
        }
    }
    
    @Override
    public Collection<MetaDataVersion> persistSchemaMetaData(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (Entry<String, ShardingSphereTable> entry : tables.entrySet()) {
            String tableName = entry.getKey().toLowerCase();
            List<String> versions = repository.getChildrenKeys(DatabaseMetaDataNode.getTableVersionsNode(databaseName, schemaName, tableName));
            String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(DatabaseMetaDataNode.getTableVersionNode(databaseName, schemaName, tableName, nextActiveVersion),
                    YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(entry.getValue())));
            if (Strings.isNullOrEmpty(getActiveVersion(databaseName, schemaName, tableName))) {
                repository.persist(DatabaseMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName), DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(DatabaseMetaDataNode.getTableNode(databaseName, schemaName, tableName), getActiveVersion(databaseName, schemaName, tableName), nextActiveVersion));
        }
        return result;
    }
    
    private String getActiveVersion(final String databaseName, final String schemaName, final String tableName) {
        return repository.getDirectly(DatabaseMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, tableName));
    }
    
    @Override
    public Map<String, ShardingSphereTable> load(final String databaseName, final String schemaName) {
        Collection<String> tableNames = repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName));
        return tableNames.isEmpty() ? Collections.emptyMap() : getTableMetaDataByTableNames(databaseName, schemaName, tableNames);
    }
    
    @Override
    public Map<String, ShardingSphereTable> load(final String databaseName, final String schemaName, final String tableName) {
        return getTableMetaDataByTableNames(databaseName, schemaName, Collections.singletonList(tableName));
    }
    
    private Map<String, ShardingSphereTable> getTableMetaDataByTableNames(final String databaseName, final String schemaName, final Collection<String> tableNames) {
        Map<String, ShardingSphereTable> result = new LinkedHashMap<>(tableNames.size(), 1F);
        tableNames.forEach(each -> {
            String table = repository.getDirectly(DatabaseMetaDataNode.getTableVersionNode(databaseName, schemaName, each,
                    repository.getDirectly(DatabaseMetaDataNode.getTableActiveVersionNode(databaseName, schemaName, each))));
            if (!Strings.isNullOrEmpty(table)) {
                result.put(each.toLowerCase(), new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(table, YamlShardingSphereTable.class)));
            }
        });
        return result;
    }
    
    @Override
    public void delete(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(DatabaseMetaDataNode.getTableNode(databaseName, schemaName, tableName.toLowerCase()));
    }
}
