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

package org.apache.shardingsphere.encrypt.metadata;

import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Schema meta data decorator for encrypt.
 */
public final class EncryptSchemaMetaDataDecorator implements RuleBasedSchemaMetaDataDecorator<EncryptRule> {
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final EncryptRule rule, final GenericSchemaBuilderMaterials materials) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap.size(), 1);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            Collection<TableMetaData> tables = new LinkedList<>();
            for (TableMetaData each : entry.getValue().getTables()) {
                tables.add(decorate(each.getName(), each, rule));
            }
            result.put(entry.getKey(), new SchemaMetaData(entry.getKey(), tables, entry.getValue().getViews()));
        }
        return result;
    }
    
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final EncryptRule encryptRule) {
        return encryptRule.findEncryptTable(tableName).map(optional -> new TableMetaData(tableName,
                getEncryptColumnMetaDataList(optional, tableMetaData.getColumns()), tableMetaData.getIndexes(), tableMetaData.getConstrains())).orElse(tableMetaData);
    }
    
    private Collection<ColumnMetaData> getEncryptColumnMetaDataList(final EncryptTable encryptTable, final Collection<ColumnMetaData> originalColumnMetaDataList) {
        Collection<ColumnMetaData> result = new LinkedHashSet<>();
        Collection<String> plainColumns = encryptTable.getPlainColumns();
        Collection<String> assistedQueryColumns = encryptTable.getAssistedQueryColumns();
        for (ColumnMetaData each : originalColumnMetaDataList) {
            String columnName = each.getName();
            if (plainColumns.contains(columnName)) {
                result.add(createColumnMetaData(encryptTable.getLogicColumnByPlainColumn(columnName), each));
                continue;
            }
            if (encryptTable.isCipherColumn(columnName)) {
                result.add(createColumnMetaData(encryptTable.getLogicColumnByCipherColumn(columnName), each));
                continue;
            }
            if (!assistedQueryColumns.contains(columnName)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private ColumnMetaData createColumnMetaData(final String columnName, final ColumnMetaData columnMetaData) {
        return new ColumnMetaData(columnName, columnMetaData.getDataType(), columnMetaData.isPrimaryKey(), columnMetaData.isGenerated(), columnMetaData.isCaseSensitive(), columnMetaData.isVisible());
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
}
