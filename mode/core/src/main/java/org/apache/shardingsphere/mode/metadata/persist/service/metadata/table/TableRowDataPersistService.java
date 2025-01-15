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
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.node.path.metadata.ShardingSphereDataNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Table row data persist service.
 */
@RequiredArgsConstructor
public final class TableRowDataPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist table row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    public void persist(final String databaseName, final String schemaName, final String tableName, final Collection<YamlShardingSphereRowData> rows) {
        if (rows.isEmpty()) {
            repository.persist(ShardingSphereDataNodePath.getTablePath(databaseName, schemaName, tableName.toLowerCase()), "");
        } else {
            rows.forEach(each -> repository.persist(ShardingSphereDataNodePath.getTableRowPath(databaseName, schemaName, tableName.toLowerCase(), each.getUniqueKey()), YamlEngine.marshal(each)));
        }
    }
    
    /**
     * Delete table row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    public void delete(final String databaseName, final String schemaName, final String tableName, final Collection<YamlShardingSphereRowData> rows) {
        rows.forEach(each -> repository.delete(ShardingSphereDataNodePath.getTableRowPath(databaseName, schemaName, tableName.toLowerCase(), each.getUniqueKey())));
    }
    
    /**
     * Load table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table
     * @return table data
     */
    public ShardingSphereTableData load(final String databaseName, final String schemaName, final ShardingSphereTable table) {
        ShardingSphereTableData result = new ShardingSphereTableData(table.getName());
        YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(table.getAllColumns()));
        for (String each : repository.getChildrenKeys(ShardingSphereDataNodePath.getTablePath(databaseName, schemaName, table.getName()))) {
            String yamlRow = repository.query(ShardingSphereDataNodePath.getTableRowPath(databaseName, schemaName, table.getName(), each));
            if (!Strings.isNullOrEmpty(yamlRow)) {
                result.getRows().add(swapper.swapToObject(YamlEngine.unmarshal(yamlRow, YamlShardingSphereRowData.class)));
            }
        }
        return result;
    }
}
