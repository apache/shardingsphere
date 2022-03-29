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

package org.apache.shardingsphere.infra.metadata.schema.builder.dialect;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectSystemSchemaBuilder;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MySQL system schema builder.
 */
public final class MySQLSystemSchemaBuilder implements DialectSystemSchemaBuilder {
    
    @SneakyThrows
    @Override
    public Map<String, ShardingSphereSchema> build(final String schemaName) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>();
        DatabaseType databaseType = DatabaseTypeRegistry.getTrunkDatabaseType(getDatabaseType());
        TableMetaDataYamlSwapper swapper = new TableMetaDataYamlSwapper();
        for (String each : databaseType.getSystemDatabases()) {
            Collection<File> schemaFiles = buildSystemSchema(each);
            if (schemaFiles.isEmpty()) {
                continue;
            }
            YamlSchema yamlSchema = new YamlSchema();
            Map<String, YamlTableMetaData> tables = new LinkedHashMap<>();
            for (File file : schemaFiles) {
                YamlTableMetaData metaData = YamlEngine.unmarshal(file, YamlTableMetaData.class);
                tables.put(file.getName().substring(0, file.getName().lastIndexOf(".")), metaData);
                yamlSchema.setTables(tables);
            }
            result.put(each, new ShardingSphereSchema());
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
