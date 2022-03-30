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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * System schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemSchemaBuilder {
    
    /**
     * Build.
     * 
     * @param databaseName database name
     * @param databaseType database type
     * @return ShardingSphere schema map
     */
    public static Map<String, ShardingSphereSchema> build(final String databaseName, final DatabaseType databaseType) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(databaseType.getSystemSchemas().size(), 1);
        TableMetaDataYamlSwapper swapper = new TableMetaDataYamlSwapper();
        for (String each : getSystemSchemas(databaseName, databaseType)) {
            Collection<InputStream> schemaStreams = getSchemaStreams(each, databaseType);
            if (schemaStreams.isEmpty()) {
                continue;
            }
            result.put(each, createSchema(schemaStreams, swapper));
        }
        return result;
    }
    
    private static Collection<String> getSystemSchemas(final String originalDatabaseName, final DatabaseType databaseType) {
        String databaseName = databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? "postgres" : originalDatabaseName;
        return databaseType.getSystemDatabaseSchemaMap().getOrDefault(databaseName, Collections.emptyList());
    }
    
    private static Collection<InputStream> getSchemaStreams(final String schemaName, final DatabaseType databaseType) {
        String databaseTypeName = databaseType.getName().toLowerCase();
        SystemSchemaBuilderRule builderRule = SystemSchemaBuilderRule.valueOf(databaseTypeName, schemaName);
        Collection<InputStream> result = new LinkedList<>();
        for (String each : builderRule.getTables()) {
            result.add(SystemSchemaBuilder.class.getClassLoader().getResourceAsStream("schema/" + databaseTypeName + "/" + schemaName + "/" + each + ".yaml"));
        }
        return result;
    }
    
    private static ShardingSphereSchema createSchema(final Collection<InputStream> schemaStreams, final TableMetaDataYamlSwapper swapper) {
        Map<String, TableMetaData> tables = new LinkedHashMap<>(schemaStreams.size(), 1);
        for (InputStream each : schemaStreams) {
            YamlTableMetaData metaData = new Yaml().loadAs(each, YamlTableMetaData.class);
            tables.put(metaData.getName(), swapper.swapToObject(metaData));
        }
        return new ShardingSphereSchema(tables);
    }
}
