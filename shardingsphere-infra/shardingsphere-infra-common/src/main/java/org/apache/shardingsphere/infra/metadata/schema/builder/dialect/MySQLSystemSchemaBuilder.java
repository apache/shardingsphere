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
import org.apache.shardingsphere.infra.yaml.schema.swapper.SchemaYamlSwapper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL system schema builder.
 */
public final class MySQLSystemSchemaBuilder implements DialectSystemSchemaBuilder {
    
    @SneakyThrows
    @Override
    public Map<String, ShardingSphereSchema> build(final String schemaName) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>();
        DatabaseType databaseType = DatabaseTypeRegistry.getTrunkDatabaseType(getDatabaseType());
        SchemaYamlSwapper swapper = new SchemaYamlSwapper();
        for (String each : databaseType.getSystemDatabases()) {
            Optional<String> schemaPath = getSystemSchemaPath(each);
            if (!schemaPath.isPresent()) {
                continue;
            }
            YamlSchema yamlSchema = YamlEngine.unmarshal(new File(schemaPath.get()), YamlSchema.class);
            result.put(each, swapper.swapToObject(yamlSchema));
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
