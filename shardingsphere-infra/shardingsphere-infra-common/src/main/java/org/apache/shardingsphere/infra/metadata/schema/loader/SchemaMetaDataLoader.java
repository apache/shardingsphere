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
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Schema meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataLoader {
    
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
    public static PhysicalSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                              final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        PhysicalSchemaMetaData result = new PhysicalSchemaMetaData();
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
}
