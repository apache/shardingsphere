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

package org.apache.shardingsphere.infra.metadata.schema.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.loader.addressing.TableAddressingMapperDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Schema loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaLoader {
    
    /**
     * Load schema meta data.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param rules ShardingSphere rules
     * @param props configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereSchema load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                            final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        ShardingSphereSchema result = loadSchema(databaseType, dataSourceMap, rules, props);
        setTableAddressingMapper(databaseType, dataSourceMap, rules, result);
        return result;
    }
    
    private static ShardingSphereSchema loadSchema(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                   final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        ShardingSphereSchema result = new ShardingSphereSchema();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (!result.containsTable(table)) {
                        TableMetaDataLoader.load(table, databaseType, dataSourceMap, rules, props).ifPresent(optional -> result.put(table, optional));
                    }
                }
            }
        }
        return result;
    }
    
    private static void setTableAddressingMapper(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                 final Collection<ShardingSphereRule> rules, final ShardingSphereSchema schema) throws SQLException {
        for (Entry<String, Collection<String>> entry : TableAddressingMapperDataLoader.load(databaseType, dataSourceMap, rules).entrySet()) {
            String tableName = entry.getKey();
            if (!schema.containsTable(tableName)) {
                schema.put(tableName, new TableMetaData());
            }
            schema.get(tableName).getAddressingDataSources().addAll(entry.getValue());
        }
    }
}
