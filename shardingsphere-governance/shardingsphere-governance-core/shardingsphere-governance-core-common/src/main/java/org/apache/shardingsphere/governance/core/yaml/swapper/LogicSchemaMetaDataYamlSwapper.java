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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlColumnMetaData;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlIndexMetaData;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlLogicSchemaMetaData;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlSchemaMetaData;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlTableMetaData;
import org.apache.shardingsphere.infra.metadata.model.logic.LogicSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.infra.metadata.model.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Logic schema meta data configuration YAML swapper.
 */
public final class LogicSchemaMetaDataYamlSwapper implements YamlSwapper<YamlLogicSchemaMetaData, LogicSchemaMetaData> {

    @Override
    public YamlLogicSchemaMetaData swapToYamlConfiguration(final LogicSchemaMetaData metaData) {
        YamlLogicSchemaMetaData result = new YamlLogicSchemaMetaData();
        result.setConfiguredSchemaMetaData(convertYamlSchema(metaData.getConfiguredSchemaMetaData()));
        return result;
    }
    
    @Override
    public LogicSchemaMetaData swapToObject(final YamlLogicSchemaMetaData yamlConfig) {
        PhysicalSchemaMetaData configured = Optional.ofNullable(yamlConfig.getConfiguredSchemaMetaData()).map(this::convertSchema).orElse(new PhysicalSchemaMetaData());
        return new LogicSchemaMetaData(configured);
    }
    
    private PhysicalSchemaMetaData convertSchema(final YamlSchemaMetaData schema) {
        return new PhysicalSchemaMetaData(schema.getTables().entrySet().stream().collect(Collectors.toMap(Entry::getKey,
            entry -> convertTable(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
    
    private PhysicalTableMetaData convertTable(final YamlTableMetaData table) {
        return new PhysicalTableMetaData(convertColumns(table.getColumns()), convertIndexes(table.getIndexes()));
    }

    private Collection<PhysicalIndexMetaData> convertIndexes(final Map<String, YamlIndexMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::convertIndex).collect(Collectors.toList());
    }

    private PhysicalIndexMetaData convertIndex(final YamlIndexMetaData index) {
        return new PhysicalIndexMetaData(index.getName());
    }

    private Collection<PhysicalColumnMetaData> convertColumns(final Map<String, YamlColumnMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::convertColumn).collect(Collectors.toList());
    }

    private PhysicalColumnMetaData convertColumn(final YamlColumnMetaData column) {
        return new PhysicalColumnMetaData(column.getName(), column.getDataType(), column.getDataTypeName(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive());
    }
    
    private YamlSchemaMetaData convertYamlSchema(final PhysicalSchemaMetaData schema) {
        Map<String, YamlTableMetaData> tables = schema.getAllTableNames().stream()
                .collect(Collectors.toMap(each -> each, each -> convertYamlTable(schema.get(each)), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        YamlSchemaMetaData result = new YamlSchemaMetaData();
        result.setTables(tables);
        return result;
    }
    
    private YamlTableMetaData convertYamlTable(final PhysicalTableMetaData table) {
        YamlTableMetaData result = new YamlTableMetaData();
        result.setColumns(convertYamlColumns(table.getColumns()));
        result.setIndexes(convertYamlIndexes(table.getIndexes()));
        return result;
    }

    private Map<String, YamlIndexMetaData> convertYamlIndexes(final Map<String, PhysicalIndexMetaData> indexes) {
        return indexes.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> convertYamlIndex(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlIndexMetaData convertYamlIndex(final PhysicalIndexMetaData index) {
        YamlIndexMetaData result = new YamlIndexMetaData();
        result.setName(index.getName());
        return result;
    }
    
    private Map<String, YamlColumnMetaData> convertYamlColumns(final Map<String, PhysicalColumnMetaData> columns) {
        return columns.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> convertYamlColumn(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlColumnMetaData convertYamlColumn(final PhysicalColumnMetaData column) {
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
