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

package org.apache.shardingsphere.infra.metadata.model.addressing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
        TableAddressingMetaData result = initialize(databaseType, dataSourceMap);
        for (Entry<ShardingSphereRule, TableAddressingMetaDataDecorator> entry : OrderedSPIRegistry.getRegisteredServices(rules, TableAddressingMetaDataDecorator.class).entrySet()) {
            entry.getValue().decorate(entry.getKey(), result);
        }
        return result;
    }
    
    private static TableAddressingMetaData initialize(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws SQLException {
        TableAddressingMetaData result = new TableAddressingMetaData();
        for (String each : getAllTableNames(databaseType, dataSourceMap)) {
            result.getTableDataSourceNamesMapper().put(each, new LinkedList<>());
        }
        return result;
    }
    
    private static Collection<String> getAllTableNames(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        for (DataSource each : dataSourceMap.values()) {
            result.addAll(PhysicalSchemaMetaDataLoader.loadTableNames(each, databaseType, Collections.emptyList()));
        }
        return result;
    }
}
