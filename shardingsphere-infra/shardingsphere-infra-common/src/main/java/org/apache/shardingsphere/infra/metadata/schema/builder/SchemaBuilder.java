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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    static {
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }
    
    /**
     * build actual and logic table meta data.
     *
     * @param materials schema builder materials
     * @return actual and logic table meta data
     * @throws SQLException SQL exception
     */
    public static Map<TableMetaData, TableMetaData> build(final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, TableMetaData> actualTableMetaMap = buildActualTableMetaDataMap(materials);
        Map<String, TableMetaData> logicTableMetaMap = buildLogicTableMetaDataMap(materials, actualTableMetaMap);
        Map<TableMetaData, TableMetaData> tableMetaDataMap = new HashMap<>(actualTableMetaMap.size(), 1);
        for (Entry<String, TableMetaData> entry : actualTableMetaMap.entrySet()) {
            tableMetaDataMap.put(entry.getValue(), logicTableMetaMap.getOrDefault(entry.getKey(), entry.getValue()));
        }
        return tableMetaDataMap;
    }
    
    private static Map<String, TableMetaData> buildActualTableMetaDataMap(final SchemaBuilderMaterials materials) throws SQLException {
        Collection<String> allTableNames = materials.getRules().stream().filter(each -> each instanceof TableContainedRule)
                .flatMap(shardingSphereRule -> ((TableContainedRule) shardingSphereRule).getTables().stream()).collect(Collectors.toSet());
        return TableMetaDataBuilder.load(allTableNames, materials);
    }
    
    private static Map<String, TableMetaData> buildLogicTableMetaDataMap(final SchemaBuilderMaterials materials, final Map<String, TableMetaData> tables) {
        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);
        for (ShardingSphereRule rule : materials.getRules()) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (tables.containsKey(table)) {
                        TableMetaData metaData = TableMetaDataBuilder.decorate(table, tables.get(table), materials.getRules());
                        result.put(table, metaData);
                    }
                }
            }
        }
        return result;
    }
}
