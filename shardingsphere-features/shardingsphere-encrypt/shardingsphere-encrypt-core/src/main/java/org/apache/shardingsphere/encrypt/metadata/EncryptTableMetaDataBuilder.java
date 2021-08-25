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
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.util.TableMetaDataUtil;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table meta data builder for encrypt.
 */
public final class EncryptTableMetaDataBuilder implements RuleBasedTableMetaDataBuilder<EncryptRule> {
    
    @Override
    public Map<String, TableMetaData> load(final Collection<String> tableNames, final EncryptRule rule, final SchemaBuilderMaterials materials) throws SQLException {
        Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.findEncryptTable(each).isPresent()).collect(Collectors.toList());
        if (needLoadTables.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<TableMetaDataLoaderMaterial> tableMetaDataLoaderMaterials = TableMetaDataUtil.getTableMetaDataLoadMaterial(needLoadTables, materials, false);
        if (tableMetaDataLoaderMaterials.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<TableMetaData> tableMetaDatas = TableMetaDataLoaderEngine.load(tableMetaDataLoaderMaterials, materials.getDatabaseType());
        return tableMetaDatas.stream().collect(Collectors.toMap(TableMetaData::getName, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final EncryptRule encryptRule) {
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        return encryptTable.map(optional ->
                new TableMetaData(tableName, getEncryptColumnMetaDataList(optional, tableMetaData.getColumns().values()), tableMetaData.getIndexes().values())).orElse(tableMetaData);
    }
    
    private Collection<ColumnMetaData> getEncryptColumnMetaDataList(final EncryptTable encryptTable, final Collection<ColumnMetaData> originalColumnMetaDataList) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> plainColumns = encryptTable.getPlainColumns();
        Collection<String> assistedQueryColumns = encryptTable.getAssistedQueryColumns();
        for (ColumnMetaData each : originalColumnMetaDataList) {
            String columnName = each.getName();
            if (encryptTable.isCipherColumn(columnName)) {
                result.add(createColumnMetaData(encryptTable.getLogicColumn(columnName), each));
                continue;
            }
            if (!plainColumns.contains(columnName) && !assistedQueryColumns.contains(columnName)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private ColumnMetaData createColumnMetaData(final String columnName, final ColumnMetaData columnMetaData) {
        return new ColumnMetaData(columnName, columnMetaData.getDataType(), columnMetaData.isPrimaryKey(), columnMetaData.isGenerated(), columnMetaData.isCaseSensitive());
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
