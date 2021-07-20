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

package org.apache.shardingsphere.infra.rule.single;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.SchemaMetaDataLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Single table data node loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableDataNodeLoader {
    
    /**
     * Load single table data node.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param excludedTables excluded tables
     * @return single table data node map
     */
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public static Map<String, SingleTableDataNode> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<String> excludedTables) {
        Map<String, SingleTableDataNode> result = new HashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, SingleTableDataNode> singleTableDataNodes = load(databaseType, entry.getKey(), entry.getValue(), excludedTables);
            // TODO recover check single table must be unique. Current situation cannot recognize replica query rule or databaseDiscovery rule for single table duplicate. 
//            singleTableDataNodes.keySet().forEach(each -> Preconditions.checkState(!result.containsKey(each), "Single table conflict, there are multiple tables `%s` existed.", each));
            result.putAll(singleTableDataNodes);
        }
        return result;
    }
    
    private static Map<String, SingleTableDataNode> load(final DatabaseType databaseType, final String dataSourceName, 
                                                         final DataSource dataSource, final Collection<String> excludedTables) {
        Collection<String> tables;
        try {
            tables = SchemaMetaDataLoader.loadAllTableNames(dataSource, databaseType);
        } catch (final SQLException ex) {
            throw new ShardingSphereConfigurationException("Can not load table: %s", ex.getMessage());
        }
        Map<String, SingleTableDataNode> result = new HashMap<>(tables.size(), 1);
        for (String each : tables) {
            if (!excludedTables.contains(each)) {
                result.put(each, new SingleTableDataNode(each, dataSourceName));
            }
        }
        return result;
    }
}
