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

package org.apache.shardingsphere.singletable.metadata;

import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.metadata.schema.builder.util.TableMetaDataUtil;
import org.apache.shardingsphere.infra.metadata.schema.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.singletable.constant.SingleTableOrder;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table meta data builder for single.
 */
public final class SingleTableMetaDataBuilder implements RuleBasedTableMetaDataBuilder<SingleTableRule> {
    
    @Override
    public Map<String, TableMetaData> load(final Collection<String> tableNames, final SingleTableRule rule, final SchemaBuilderMaterials materials)
            throws SQLException {
        Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.getTables().contains(each)).collect(Collectors.toSet());
        if (needLoadTables.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<TableMetaDataLoaderMaterial> tableMetaDataLoaderMaterials = TableMetaDataUtil.getTableMetaDataLoadMaterial(needLoadTables, materials, false);
        if (tableMetaDataLoaderMaterials.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<TableMetaData> tableMetaDataList = TableMetaDataLoaderEngine.load(tableMetaDataLoaderMaterials, materials.getDatabaseType());
        return tableMetaDataList.stream().collect(Collectors.toMap(TableMetaData::getName, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Map<String, TableMetaData> decorate(final Map<String, TableMetaData> tableMetaDataMap, final SingleTableRule rule, final SchemaBuilderMaterials materials) {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (Entry<String, TableMetaData> entry : tableMetaDataMap.entrySet()) {
            result.put(entry.getKey(), decorate(entry.getKey(), entry.getValue(), rule));
        }
        return result;
    }
    
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final SingleTableRule rule) {
        return rule.getTables().contains(tableName) ? new TableMetaData(tableName, tableMetaData.getColumns().values(), getIndex(tableMetaData), getConstraint(tableMetaData)) : tableMetaData;
    }
    
    private Collection<IndexMetaData> getIndex(final TableMetaData tableMetaData) {
        return tableMetaData.getIndexes().values().stream().map(each -> new IndexMetaData(IndexMetaDataUtil.getLogicIndexName(each.getName(), tableMetaData.getName()))).collect(Collectors.toList());
    }
    
    private Collection<ConstraintMetaData> getConstraint(final TableMetaData tableMetaData) {
        return tableMetaData.getConstrains().values().stream().map(each 
            -> new ConstraintMetaData(IndexMetaDataUtil.getLogicIndexName(each.getName(), tableMetaData.getName()), each.getReferencedTableName())).collect(Collectors.toList());
    }
    
    @Override
    public int getOrder() {
        return SingleTableOrder.ORDER;
    }
    
    @Override
    public Class<SingleTableRule> getTypeClass() {
        return SingleTableRule.class;
    }
}
