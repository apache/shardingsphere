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

import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlColumnMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlIndexMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
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
        return Optional.ofNullable(yamlConfig).map(this::swapSchema).orElse(new ShardingSphereSchema());
    }
    
    private ShardingSphereSchema swapSchema(final YamlSchema schema) {
        return new ShardingSphereSchema(MapUtils.isEmpty(schema.getTables()) ? Maps.newLinkedHashMap() : schema.getTables().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> swapTable(entry.getKey(), entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
    
    private TableMetaData swapTable(final String tableName, final YamlTableMetaData table) {
        return new TableMetaData(tableName, swapColumns(table.getColumns()), swapIndexes(table.getIndexes()));
    }
    
    private Collection<IndexMetaData> swapIndexes(final Map<String, YamlIndexMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::swapIndex).collect(Collectors.toList());
    }
    
    private IndexMetaData swapIndex(final YamlIndexMetaData index) {
        return new IndexMetaData(index.getName());
    }
    
    private Collection<ColumnMetaData> swapColumns(final Map<String, YamlColumnMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::swapColumn).collect(Collectors.toList());
    }
    
    private ColumnMetaData swapColumn(final YamlColumnMetaData column) {
        return new ColumnMetaData(column.getName(), column.getDataType(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive());
    }
    
    private YamlTableMetaData swapYamlTable(final TableMetaData table) {
        YamlTableMetaData result = new YamlTableMetaData();
        result.setColumns(swapYamlColumns(table.getColumns()));
        result.setIndexes(swapYamlIndexes(table.getIndexes()));
        return result;
    }
    
    private Map<String, YamlIndexMetaData> swapYamlIndexes(final Map<String, IndexMetaData> indexes) {
        return indexes.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapYamlIndex(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlIndexMetaData swapYamlIndex(final IndexMetaData index) {
        YamlIndexMetaData result = new YamlIndexMetaData();
        result.setName(index.getName());
        return result;
    }
    
    private Map<String, YamlColumnMetaData> swapYamlColumns(final Map<String, ColumnMetaData> columns) {
        return columns.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapYamlColumn(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlColumnMetaData swapYamlColumn(final ColumnMetaData column) {
        YamlColumnMetaData result = new YamlColumnMetaData();
        result.setName(column.getName());
        result.setCaseSensitive(column.isCaseSensitive());
        result.setGenerated(column.isGenerated());
        result.setPrimaryKey(column.isPrimaryKey());
        result.setDataType(result.getDataType());
        result.setDataTypeName(result.getDataTypeName());
        return result;
    }
}
