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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema.SchemaMetaDataReviseEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data revise engine.
 */
@RequiredArgsConstructor
public final class MetaDataReviseEngine {
    
    private final Collection<ShardingSphereRule> rules;
    
    private final DatabaseType protocolType;
    
    /**
     * Revise meta data.
     *
     * @param schemaMetaDataMap schema meta data map
     * @param material generic schema builder material
     * @return ShardingSphere schema map
     */
    public Map<String, ShardingSphereSchema> revise(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material) {
        if (schemaMetaDataMap.isEmpty()) {
            return Collections.singletonMap(material.getDefaultSchemaName(), new ShardingSphereSchema(material.getDefaultSchemaName(), protocolType));
        }
        Map<String, ShardingSphereSchema> result = new HashMap<>(schemaMetaDataMap.size(), 1F);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            SchemaMetaData schemaMetaData = new SchemaMetaDataReviseEngine(rules, material.getProps()).revise(entry.getValue());
            result.put(entry.getKey(), new ShardingSphereSchema(entry.getKey(), convertToTables(schemaMetaData.getTables()), new LinkedList<>(), protocolType));
        }
        return result;
    }
    
    private Collection<ShardingSphereTable> convertToTables(final Collection<TableMetaData> tableMetaDataList) {
        return tableMetaDataList.stream().map(each -> new ShardingSphereTable(
                each.getName(), convertToColumns(each.getColumns()), convertToIndexes(each.getIndexes()), convertToConstraints(each.getConstraints()), each.getType())).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereColumn> convertToColumns(final Collection<ColumnMetaData> columnMetaDataList) {
        return columnMetaDataList.stream().map(ShardingSphereColumn::new).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereIndex> convertToIndexes(final Collection<IndexMetaData> indexMetaDataList) {
        return indexMetaDataList.stream().map(ShardingSphereIndex::new).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereConstraint> convertToConstraints(final Collection<ConstraintMetaData> constraintMetaDataList) {
        return constraintMetaDataList.stream().map(ShardingSphereConstraint::new).collect(Collectors.toList());
    }
}
