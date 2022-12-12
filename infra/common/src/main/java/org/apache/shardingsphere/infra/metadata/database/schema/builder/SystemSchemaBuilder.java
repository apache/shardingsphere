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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
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
     * Build system schema.
     * 
     * @param databaseName database name
     * @param databaseType database type
     * @return ShardingSphere system schema map
     */
    public static Map<String, ShardingSphereSchema> build(final String databaseName, final DatabaseType databaseType) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(databaseType.getSystemSchemas().size(), 1);
        YamlTableSwapper swapper = new YamlTableSwapper();
        for (String each : getSystemSchemas(databaseName, databaseType)) {
            result.put(each.toLowerCase(), createSchema(getSchemaStreams(each, databaseType), swapper));
        }
        return result;
    }
    
    private static Collection<String> getSystemSchemas(final String originalDatabaseName, final DatabaseType databaseType) {
        String databaseName = databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? "postgres" : originalDatabaseName;
        return databaseType.getSystemDatabaseSchemaMap().getOrDefault(databaseName, Collections.emptyList());
    }
    
    private static Collection<InputStream> getSchemaStreams(final String schemaName, final DatabaseType databaseType) {
        SystemSchemaBuilderRule builderRule = SystemSchemaBuilderRule.valueOf(databaseType.getType(), schemaName);
        Collection<InputStream> result = new LinkedList<>();
        for (String each : builderRule.getTables()) {
            result.add(SystemSchemaBuilder.class.getClassLoader().getResourceAsStream("schema/" + databaseType.getType().toLowerCase() + "/" + schemaName + "/" + each + ".yaml"));
        }
        return result;
    }
    
    private static ShardingSphereSchema createSchema(final Collection<InputStream> schemaStreams, final YamlTableSwapper swapper) {
        Map<String, ShardingSphereTable> tables = new LinkedHashMap<>(schemaStreams.size(), 1);
        for (InputStream each : schemaStreams) {
            YamlShardingSphereTable metaData = new Yaml().loadAs(each, YamlShardingSphereTable.class);
            tables.put(metaData.getName(), swapper.swapToObject(metaData));
        }
        return new ShardingSphereSchema(tables, Collections.emptyMap());
    }
}
