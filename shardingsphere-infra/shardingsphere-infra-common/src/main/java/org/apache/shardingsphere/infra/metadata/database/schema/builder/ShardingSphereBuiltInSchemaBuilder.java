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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * System schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereBuiltInSchemaBuilder {
    
    private static final Set<String> BUILT_IN_TABLES = Collections.singleton("sharding_statistics_table");
    
    /**
     * Build sharding sphere built in schema.
     * 
     * @param databaseType database type
     * @return ShardingSphere system schema map
     */
    public static Map<String, ShardingSphereSchema> build(final DatabaseType databaseType) {
        Map<String, ShardingSphereSchema> result = new HashMap<>(1, 1);
        YamlTableSwapper swapper = new YamlTableSwapper();
        String schemaName = databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? "public" : "shardingsphere";
        result.put(schemaName, createSchema(getBuiltInTableStream(), swapper));
        return result;
    }
    
    private static Collection<InputStream> getBuiltInTableStream() {
        Collection<InputStream> result = new LinkedList<>();
        for (String each : BUILT_IN_TABLES) {
            result.add(ShardingSphereBuiltInSchemaBuilder.class.getClassLoader().getResourceAsStream("builtintables/" + each + ".yaml"));
        }
        return result;
    }
    
    private static ShardingSphereSchema createSchema(final Collection<InputStream> tableStreams, final YamlTableSwapper swapper) {
        Map<String, ShardingSphereTable> tables = new LinkedHashMap<>(tableStreams.size(), 1);
        tableStreams.forEach(each -> {
            YamlShardingSphereTable metaData = new Yaml().loadAs(each, YamlShardingSphereTable.class);
            tables.put(metaData.getName(), swapper.swapToObject(metaData));
        });
        return new ShardingSphereSchema(tables, Collections.emptyMap());
    }
}
