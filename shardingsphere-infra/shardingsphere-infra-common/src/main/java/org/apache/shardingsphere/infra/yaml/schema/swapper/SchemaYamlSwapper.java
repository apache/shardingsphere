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

package org.apache.shardingsphere.infra.yaml.schema.swapper;

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere schema YAML swapper.
 */
public final class SchemaYamlSwapper implements YamlConfigurationSwapper<YamlSchema, ShardingSphereSchema> {
    
    @Override
    public YamlSchema swapToYamlConfiguration(final ShardingSphereSchema schema) {
        Map<String, YamlTableMetaData> tables = schema.getAllTableNames().stream()
                .collect(Collectors.toMap(each -> each, each -> swapYamlTable(schema.get(each)), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        YamlSchema result = new YamlSchema();
        result.setTables(tables);
        return result;
    }
    
    @Override
    public ShardingSphereSchema swapToObject(final YamlSchema yamlConfig) {
        return Optional.ofNullable(yamlConfig).map(this::swapSchema).orElseGet(ShardingSphereSchema::new);
    }
    
    private ShardingSphereSchema swapSchema(final YamlSchema schema) {
        return new ShardingSphereSchema(null == schema.getTables() || schema.getTables().isEmpty() ? new LinkedHashMap<>()
                : schema.getTables().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapTable(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
    
    private ShardingSphereTable swapTable(final YamlTableMetaData table) {
        return new TableMetaDataYamlSwapper().swapToObject(table);
    }
    
    private YamlTableMetaData swapYamlTable(final ShardingSphereTable table) {
        return new TableMetaDataYamlSwapper().swapToYamlConfiguration(table);
    }
}
