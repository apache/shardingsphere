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

package org.apache.shardingsphere.infra.metadata.schema.model.addressing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.schema.PhysicalSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table addressing meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableAddressingMetaDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(TableAddressingMetaDataDecorator.class);
    }
    
    /**
     * Load table addressing meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param rules ShardingSphere rules
     * @return table addressing meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TableAddressingMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) throws SQLException {
        TableAddressingMetaData result = initializeMetaData(databaseType, dataSourceMap);
        for (Entry<ShardingSphereRule, TableAddressingMetaDataDecorator> entry : OrderedSPIRegistry.getRegisteredServices(rules, TableAddressingMetaDataDecorator.class).entrySet()) {
            entry.getValue().decorate(entry.getKey(), result);
        }
        return result;
    }
    
    private static TableAddressingMetaData initializeMetaData(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws SQLException {
        TableAddressingMetaData result = new TableAddressingMetaData();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            appendMetaData(result, databaseType, entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private static void appendMetaData(final TableAddressingMetaData metaData, final DatabaseType databaseType, final String dataSourceName, final DataSource dataSource) throws SQLException {
        for (String each : PhysicalSchemaMetaDataLoader.loadTableNames(dataSource, databaseType, Collections.emptyList())) {
            if (!metaData.getTableDataSourceNamesMapper().containsKey(each)) {
                metaData.getTableDataSourceNamesMapper().put(each, new LinkedHashSet<>());
            }
            metaData.getTableDataSourceNamesMapper().get(each).add(dataSourceName);
        }
    }
}
