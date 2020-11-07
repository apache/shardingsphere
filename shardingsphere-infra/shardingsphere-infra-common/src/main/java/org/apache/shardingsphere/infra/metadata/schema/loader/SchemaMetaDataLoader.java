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
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.ShardingSphereMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Schema meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereMetaDataLoader.class);
    }
    
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
    @SuppressWarnings("rawtypes")
    public static PhysicalSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                              final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        PhysicalSchemaMetaData result = new PhysicalSchemaMetaData();
        for (Entry<ShardingSphereRule, ShardingSphereMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, ShardingSphereMetaDataLoader.class).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                for (String each : ((TableContainedRule) entry.getKey()).getTables()) {
                    if (!result.containsTable(each)) {
                        TableMetaDataLoader.load(each, databaseType, dataSourceMap, rules, props).ifPresent(optional -> result.put(each, optional));
                    }
                }
            }
        }
        return result;
    }
}
