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

package org.apache.shardingsphere.governance.core.metadata.yaml;

import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Rule schema meta data configuration YAML swapper.
 */
public final class RuleSchemaMetaDataYamlSwapper implements YamlSwapper<YamlRuleSchemaMetaData, RuleSchemaMetaData> {

    @Override
    public YamlRuleSchemaMetaData swapToYamlConfiguration(final RuleSchemaMetaData metaData) {
        YamlRuleSchemaMetaData result = new YamlRuleSchemaMetaData();
        result.setConfiguredSchemaMetaData(convertYamlSchema(metaData.getConfiguredSchemaMetaData()));
        Map<String, YamlSchemaMetaData> unconfigured = metaData.getUnconfiguredSchemaMetaDataMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> convertYamlSchema(entry.getValue())));
        result.setUnconfiguredSchemaMetaDataMap(unconfigured);
        return result;
    }

    @Override
    public RuleSchemaMetaData swapToObject(final YamlRuleSchemaMetaData yamlConfig) {
        SchemaMetaData configured = Optional.ofNullable(yamlConfig.getConfiguredSchemaMetaData()).map(this::convertSchema).orElse(new SchemaMetaData());
        Map<String, SchemaMetaData> unconfigured = Optional.ofNullable(yamlConfig.getUnconfiguredSchemaMetaDataMap()).map(e -> e.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> convertSchema(entry.getValue())))).orElse(new HashMap<>());
        return new RuleSchemaMetaData(configured, unconfigured);
    }

    private SchemaMetaData convertSchema(final YamlSchemaMetaData schema) {
        return new SchemaMetaData(schema.getTables().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> convertTable(entry.getValue()))));
    }

    private TableMetaData convertTable(final YamlTableMetaData table) {
        return new TableMetaData(convertColumns(table.getColumns()), convertIndexes(table.getIndexes()));
    }

    private Collection<IndexMetaData> convertIndexes(final Map<String, YamlIndexMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::convertIndex).collect(Collectors.toList());
    }

    private IndexMetaData convertIndex(final YamlIndexMetaData index) {
        return new IndexMetaData(index.getName());
    }

    private Collection<ColumnMetaData> convertColumns(final Map<String, YamlColumnMetaData> indexes) {
        return indexes.values().stream().map(this::convertColumn).collect(Collectors.toList());
    }

    private ColumnMetaData convertColumn(final YamlColumnMetaData column) {
        return new ColumnMetaData(column.getName(), column.getDataType(), column.getDataTypeName(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive());
    }
    
    private YamlSchemaMetaData convertYamlSchema(final SchemaMetaData schema) {
        Map<String, YamlTableMetaData> tables = schema.getAllTableNames().stream().collect(Collectors.toMap(each -> each, each -> convertYamlTable(schema.get(each))));
        YamlSchemaMetaData result = new YamlSchemaMetaData();
        result.setTables(tables);
        return result;
    }

    private YamlTableMetaData convertYamlTable(final TableMetaData table) {
        YamlTableMetaData result = new YamlTableMetaData();
        result.setColumns(convertYamlColumns(table.getColumns()));
        result.setIndexes(convertYamlIndexes(table.getIndexes()));
        return result;
    }

    private Map<String, YamlIndexMetaData> convertYamlIndexes(final Map<String, IndexMetaData> indexes) {
        return indexes.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> convertYamlIndex(entry.getValue())));
    }

    private YamlIndexMetaData convertYamlIndex(final IndexMetaData index) {
        YamlIndexMetaData result = new YamlIndexMetaData();
        result.setName(index.getName());
        return result;
    }

    private Map<String, YamlColumnMetaData> convertYamlColumns(final Map<String, ColumnMetaData> columns) {
        return columns.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> convertYamlColumn(entry.getValue())));
    }

    private YamlColumnMetaData convertYamlColumn(final ColumnMetaData column) {
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
