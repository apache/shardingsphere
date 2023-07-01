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
import java.util.Map;

/**
 * Table meta data persist service.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistService implements SchemaMetaDataPersistService<Map<String, ShardingSphereTable>> {
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        tables.forEach((key, value) -> repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, key.toLowerCase()),
                YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(value))));
    }
    
    // TODO Remove this when metadata structure adjustment completed. #25485
    @Override
    public Collection<MetaDataVersion> persistSchemaMetaData(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> schema) {
        return Collections.emptyList();
    }
    
    @Override
    public Map<String, ShardingSphereTable> load(final String databaseName, final String schemaName) {
        Collection<String> tableNames = repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName));
        return tableNames.isEmpty() ? Collections.emptyMap() : getTableMetaDataByTableNames(databaseName, schemaName, tableNames);
    }
    
    // TODO Remove this when metadata structure adjustment completed. #25485
    @Override
    public Map<String, ShardingSphereTable> load(final String databaseName, final String schemaName, final String tableName) {
        return Collections.emptyMap();
    }
    
    @Override
    public void delete(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, tableName.toLowerCase()));
    }
    
    private Map<String, ShardingSphereTable> getTableMetaDataByTableNames(final String databaseName, final String schemaName, final Collection<String> tableNames) {
        Map<String, ShardingSphereTable> result = new LinkedHashMap<>(tableNames.size(), 1F);
        tableNames.forEach(each -> {
            String table = repository.getDirectly(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, each));
            if (!Strings.isNullOrEmpty(table)) {
                result.put(each.toLowerCase(), new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(table, YamlShardingSphereTable.class)));
            }
        });
        return result;
    }
}
