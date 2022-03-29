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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    @SneakyThrows
    public static Map<String, ShardingSphereSchema> build(final String databaseName, final DatabaseType databaseType) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(databaseType.getSystemSchemas().size(), 1);
        TableMetaDataYamlSwapper swapper = new TableMetaDataYamlSwapper();
        for (String each : getSystemSchemas(databaseName, databaseType)) {
            Collection<File> schemaFiles = getSchemaFiles(each, databaseType);
            if (schemaFiles.isEmpty()) {
                continue;
            }
            result.put(each, createSchema(schemaFiles, swapper));
        }
        return result;
    }
    
    private static Collection<String> getSystemSchemas(final String originalDatabaseName, final DatabaseType databaseType) {
        String databaseName = databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? "postgres" : originalDatabaseName;
        return databaseType.getSystemDatabaseSchemaMap().getOrDefault(databaseName, Collections.emptyList());
    }
    
    private static Collection<File> getSchemaFiles(final String schemaName, final DatabaseType databaseType) {
        URL url = SystemSchemaBuilder.class.getClassLoader().getResource("schema/" + databaseType.getName().toLowerCase() + "/" + schemaName);
        if (null == url) {
            return Collections.emptyList();
        }
        File[] files = new File(url.getFile()).listFiles();
        return null == files ? Collections.emptyList() : Arrays.asList(files);
    }
    
    @SneakyThrows
    private static ShardingSphereSchema createSchema(final Collection<File> schemaFiles, final TableMetaDataYamlSwapper swapper) {
        Map<String, TableMetaData> tables = new LinkedHashMap<>(schemaFiles.size(), 1);
        for (File file : schemaFiles) {
            YamlTableMetaData metaData = YamlEngine.unmarshal(file, YamlTableMetaData.class);
            tables.put(metaData.getName(), swapper.swapToObject(metaData));
        }
        return new ShardingSphereSchema(tables);
    }
}
