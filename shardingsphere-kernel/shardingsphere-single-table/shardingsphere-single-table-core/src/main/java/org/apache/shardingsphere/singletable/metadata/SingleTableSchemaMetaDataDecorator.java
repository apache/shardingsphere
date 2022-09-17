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

import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.singletable.constant.SingleTableOrder;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema meta data decorator for single table.
 */
public final class SingleTableSchemaMetaDataDecorator implements RuleBasedSchemaMetaDataDecorator<SingleTableRule> {
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final SingleTableRule rule, final GenericSchemaBuilderMaterials materials) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            Collection<TableMetaData> tables = new LinkedList<>();
            for (TableMetaData each : entry.getValue().getTables()) {
                tables.add(decorate(each.getName(), each));
            }
            result.put(entry.getKey(), new SchemaMetaData(entry.getKey(), tables));
        }
        return result;
    }
    
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData) {
        return new TableMetaData(tableName, tableMetaData.getColumns(), getIndex(tableMetaData), getConstraint(tableMetaData));
    }
    
    private Collection<IndexMetaData> getIndex(final TableMetaData tableMetaData) {
        return tableMetaData.getIndexes().stream().map(each -> new IndexMetaData(IndexMetaDataUtil.getLogicIndexName(each.getName(), tableMetaData.getName()))).collect(Collectors.toList());
    }
    
    private Collection<ConstraintMetaData> getConstraint(final TableMetaData tableMetaData) {
        return tableMetaData.getConstrains().stream().map(each -> new ConstraintMetaData(
                IndexMetaDataUtil.getLogicIndexName(each.getName(), tableMetaData.getName()), each.getReferencedTableName())).collect(Collectors.toList());
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
