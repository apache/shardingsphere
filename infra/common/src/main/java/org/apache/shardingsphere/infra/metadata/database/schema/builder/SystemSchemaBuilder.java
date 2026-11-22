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

import com.google.common.io.ByteStreams;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * System schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemSchemaBuilder {
    
    private static final YamlTableSwapper TABLE_SWAPPER = new YamlTableSwapper();
    
    /**
     * Build system schema.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param props configuration properties
     * @return ShardingSphere system schema map
     */
    public static Map<String, ShardingSphereSchema> build(final String databaseName, final DatabaseType databaseType, final ConfigurationProperties props) {
        return build(databaseName, databaseType, props, Collections.emptyList());
    }
    
    /**
     * Build system schemas which are absent from loaded schemas or miss system tables.
     *
     * <p>A loaded system schema which already contains every expected system table is not built again, so its table definitions are not read.</p>
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param props configuration properties
     * @param loadedSchemas loaded schemas
     * @return ShardingSphere system schema map
     */
    public static Map<String, ShardingSphereSchema> build(final String databaseName, final DatabaseType databaseType, final ConfigurationProperties props,
                                                          final Collection<ShardingSphereSchema> loadedSchemas) {
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        boolean isSystemSchemaMetaDataEnabled = isSystemSchemaMetaDataEnabled(props.getProps());
        SystemTable systemTable = new SystemTable(databaseType);
        Map<String, ShardingSphereSchema> loadedSchemaMap = loadedSchemas.stream()
                .collect(Collectors.toMap(ShardingSphereSchema::getName, each -> each, (oldValue, currentValue) -> currentValue, () -> new HashMap<>(loadedSchemas.size(), 1F)));
        return getSystemSchemas(databaseName, databaseType, systemDatabase).stream()
                .filter(each -> !isComplete(loadedSchemaMap.get(each), each, databaseType, systemTable, isSystemSchemaMetaDataEnabled))
                .collect(Collectors.toMap(String::toLowerCase, each -> createSchema(each, databaseType, isSystemSchemaMetaDataEnabled), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private static boolean isSystemSchemaMetaDataEnabled(final Properties props) {
        TemporaryConfigurationPropertyKey configKey = TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED;
        return Boolean.parseBoolean(props.getOrDefault(configKey.getKey(), configKey.getDefaultValue()).toString());
    }
    
    private static Collection<String> getSystemSchemas(final String originalDatabaseName, final DatabaseType databaseType, final SystemDatabase systemDatabase) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        return systemDatabase.getSystemSchemas(dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() ? "postgres" : originalDatabaseName);
    }
    
    private static boolean isComplete(final ShardingSphereSchema loadedSchema, final String schemaName, final DatabaseType databaseType, final SystemTable systemTable,
                                      final boolean isSystemSchemaMetadataEnabled) {
        if (null == loadedSchema) {
            return false;
        }
        for (String each : SystemSchemaManager.getTables(databaseType.getType(), schemaName)) {
            if ((isSystemSchemaMetadataEnabled || systemTable.isSupportedSystemTable(schemaName, each)) && !loadedSchema.containsTable(each)) {
                return false;
            }
        }
        return true;
    }
    
    @SneakyThrows(IOException.class)
    private static ShardingSphereSchema createSchema(final String schemaName, final DatabaseType databaseType, final boolean isSystemSchemaMetadataEnabled) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        SystemTable systemTable = new SystemTable(databaseType);
        for (InputStream each : SystemSchemaManager.getAllInputStreams(databaseType.getType(), schemaName)) {
            try (InputStream inputStream = each) {
                YamlShardingSphereTable metaData = YamlEngine.unmarshal(ByteStreams.toByteArray(inputStream), YamlShardingSphereTable.class);
                if (isSystemSchemaMetadataEnabled || systemTable.isSupportedSystemTable(schemaName, metaData.getName())) {
                    tables.add(TABLE_SWAPPER.swapToObject(metaData));
                }
            }
        }
        return new ShardingSphereSchema(schemaName, databaseType, tables, Collections.emptyList());
    }
}
