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
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereView;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * YAML schema swapper.
 */
public final class YamlSchemaSwapper implements YamlConfigurationSwapper<YamlShardingSphereSchema, ShardingSphereSchema> {
    
    @Override
    public YamlShardingSphereSchema swapToYamlConfiguration(final ShardingSphereSchema schema) {
        Map<String, YamlShardingSphereTable> tables = schema.getAllTableNames().stream()
                .collect(Collectors.toMap(each -> each, each -> swapYamlTable(schema.getTable(each)), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, YamlShardingSphereView> views = schema.getAllViewNames().stream()
                .collect(Collectors.toMap(each -> each, each -> swapYamlView(schema.getView(each)), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        YamlShardingSphereSchema result = new YamlShardingSphereSchema();
        result.setTables(tables);
        result.setViews(views);
        return result;
    }
    
    @Override
    public ShardingSphereSchema swapToObject(final YamlShardingSphereSchema yamlConfig) {
        return Optional.ofNullable(yamlConfig).map(this::swapSchema).orElseGet(ShardingSphereSchema::new);
    }
    
    private ShardingSphereSchema swapSchema(final YamlShardingSphereSchema schema) {
        Map<String, ShardingSphereTable> tables = null == schema.getTables() || schema.getTables().isEmpty() ? new LinkedHashMap<>()
                : schema.getTables().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapTable(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, ShardingSphereView> views = null == schema.getViews() || schema.getViews().isEmpty() ? new LinkedHashMap<>()
                : schema.getViews().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapView(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new ShardingSphereSchema(tables, views);
    }
    
    private ShardingSphereTable swapTable(final YamlShardingSphereTable table) {
        return new YamlTableSwapper().swapToObject(table);
    }
    
    private YamlShardingSphereTable swapYamlTable(final ShardingSphereTable table) {
        return new YamlTableSwapper().swapToYamlConfiguration(table);
    }
    
    private ShardingSphereView swapView(final YamlShardingSphereView view) {
        return new YamlViewSwapper().swapToObject(view);
    }
    
    private YamlShardingSphereView swapYamlView(final ShardingSphereView view) {
        return new YamlViewSwapper().swapToYamlConfiguration(view);
    }
}
