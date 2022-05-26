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

package org.apache.shardingsphere.infra.metadata.database.schema.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Generic schema builder.
 */
public final class GenericSchemaBuilder {
    
    /**
     * Build generic schema.
     *
     * @param databaseName database name
     * @param protocolType protocol type
     * @param storageType storage type
     * @param dataSourceMap data source map
     * @param rules rules
     * @param props configuration properties
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final String databaseName, final DatabaseType protocolType, final DatabaseType storageType, final Map<String, DataSource> dataSourceMap,
                                                          final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(protocolType, databaseName);
        Map<String, SchemaMetaData> schemaMetaDataMap = SchemaMetaDataLoader.load(
                getAllTableNames(rules), new GenericSchemaBuilderMaterials(protocolType, storageType, dataSourceMap, rules, props, defaultSchemaName));
        if (schemaMetaDataMap.isEmpty()) {
            return Collections.singletonMap(defaultSchemaName, new ShardingSphereSchema());
        }
        Map<String, ShardingSphereSchema> result = new ConcurrentHashMap<>();
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            result.put(entry.getKey().toLowerCase(), new ShardingSphereSchema(entry.getValue().getTables()));
        }
        return result;
    }
    
    private static Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(each -> each instanceof TableContainedRule).flatMap(each -> ((TableContainedRule) each).getTables().stream()).collect(Collectors.toSet());
    }
}
