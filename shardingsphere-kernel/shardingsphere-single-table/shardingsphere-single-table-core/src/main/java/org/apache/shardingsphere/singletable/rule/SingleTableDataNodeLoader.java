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

package org.apache.shardingsphere.singletable.rule;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.SchemaMetaDataLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
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
    public static Map<String, SingleTableDataNode> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                                        final Collection<String> excludedTables, final ConfigurationProperties props) {
        Map<String, SingleTableDataNode> result = new ConcurrentHashMap<>();
        boolean checkDuplicateTable = props.getValue(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, SingleTableDataNode> dataNodeMap = load(databaseType, entry.getKey(), entry.getValue(), excludedTables);
            for (String each : dataNodeMap.keySet()) {
                SingleTableDataNode existDataNode = result.putIfAbsent(each.toLowerCase(), dataNodeMap.get(each));
                if (checkDuplicateTable) {
                    Preconditions.checkState(null == existDataNode, "Single table conflict, there are multiple tables `%s` existed.", each);
                }
            }
        }
        return result;
    }
    
    private static Map<String, SingleTableDataNode> load(final DatabaseType databaseType, final String dataSourceName, final DataSource dataSource, final Collection<String> excludedTables) {
        Collection<String> tables = loadAllTableNames(databaseType, dataSource);
        Map<String, SingleTableDataNode> result = new HashMap<>(tables.size(), 1);
        for (String each : tables) {
            if (!excludedTables.contains(each)) {
                result.put(each, new SingleTableDataNode(each, dataSourceName));
            }
        }
        return result;
    }
    
    private static Collection<String> loadAllTableNames(final DatabaseType databaseType, final DataSource dataSource) {
        try {
            return SchemaMetaDataLoader.loadAllTableNames(databaseType, dataSource);
        } catch (final SQLException ex) {
            throw new ShardingSphereConfigurationException("Can not load table: %s", ex.getMessage());
        }
    }
}
