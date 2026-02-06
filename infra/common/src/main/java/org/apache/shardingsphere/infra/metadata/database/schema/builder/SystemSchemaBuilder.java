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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
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
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        boolean isSystemSchemaMetaDataEnabled = isSystemSchemaMetaDataEnabled(props.getProps());
        return getSystemSchemas(databaseName, databaseType, systemDatabase).stream()
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
    
    private static ShardingSphereSchema createSchema(final String schemaName, final DatabaseType databaseType, final boolean isSystemSchemaMetadataEnabled) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        SystemTable systemTable = new SystemTable(databaseType);
        for (InputStream each : SystemSchemaManager.getAllInputStreams(databaseType.getType(), schemaName, isSystemSchemaMetadataEnabled)) {
            YamlShardingSphereTable metaData = new Yaml().loadAs(each, YamlShardingSphereTable.class);
            if (isSystemSchemaMetadataEnabled || systemTable.isSupportedSystemTable(schemaName, metaData.getName())) {
                tables.add(TABLE_SWAPPER.swapToObject(metaData));
            }
        }
        return new ShardingSphereSchema(schemaName, databaseType, tables, Collections.emptyList());
    }
}
