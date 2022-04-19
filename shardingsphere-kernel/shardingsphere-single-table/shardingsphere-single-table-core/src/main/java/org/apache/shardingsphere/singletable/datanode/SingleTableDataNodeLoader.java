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

package org.apache.shardingsphere.singletable.datanode;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.loader.common.SchemaMetaDataLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single table data node loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableDataNodeLoader {
    
    /**
     * Load single table data nodes.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param excludedTables excluded tables
     * @param props configuration properties
     * @return single table data node map
     */
    public static Map<String, Collection<DataNode>> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                         final Collection<String> excludedTables, final ConfigurationProperties props) {
        Map<String, Collection<DataNode>> result = new ConcurrentHashMap<>();
        boolean checkDuplicateTable = props.getValue(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, Collection<DataNode>> dataNodeMap = load(databaseType, entry.getKey(), entry.getValue(), excludedTables);
            for (String each : dataNodeMap.keySet()) {
                Collection<DataNode> addedDataNodes = dataNodeMap.get(each);
                Collection<DataNode> existDataNodes = result.getOrDefault(each.toLowerCase(), new LinkedHashSet<>(addedDataNodes.size(), 1));
                existDataNodes.addAll(addedDataNodes);
                result.putIfAbsent(each.toLowerCase(), existDataNodes);
                if (checkDuplicateTable) {
                    Preconditions.checkState(!containsDuplicateTable(existDataNodes), "Single table conflict, there are multiple tables `%s` existed.", each);
                }
            }
        }
        return result;
    }
    
    private static Map<String, Collection<DataNode>> load(final DatabaseType databaseType, final String dataSourceName, final DataSource dataSource, final Collection<String> excludedTables) {
        Map<String, Collection<String>> schemaTableNames = loadSchemaTableNames(databaseType, dataSource);
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : schemaTableNames.entrySet()) {
            for (String each : entry.getValue()) {
                if (excludedTables.contains(each)) {
                    continue;
                }
                Collection<DataNode> dataNodes = result.getOrDefault(each, new LinkedList<>());
                DataNode dataNode = new DataNode(dataSourceName, each);
                dataNode.setSchemaName(entry.getKey());
                dataNodes.add(dataNode);
                result.putIfAbsent(each, dataNodes);
            }
        }
        return result;
    }
    
    private static boolean containsDuplicateTable(final Collection<DataNode> dataNodes) {
        Collection<String> schemas = new HashSet<>(dataNodes.size(), 1);
        for (DataNode each : dataNodes) {
            if (!schemas.add(each.getSchemaName())) {
                return true;
            }
        }
        return false;
    }
    
    private static Map<String, Collection<String>> loadSchemaTableNames(final DatabaseType databaseType, final DataSource dataSource) {
        try {
            return SchemaMetaDataLoader.loadSchemaTableNames(databaseType, dataSource);
        } catch (final SQLException ex) {
            throw new ShardingSphereConfigurationException("Can not load table: %s", ex.getMessage());
        }
    }
}
