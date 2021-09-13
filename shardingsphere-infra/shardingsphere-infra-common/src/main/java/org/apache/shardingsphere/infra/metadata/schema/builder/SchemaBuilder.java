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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    /**
     * Build sharding sphere schema.
     *
     * @param materials schema builder materials
     * @return sharding sphere schema
     * @throws SQLException SQL exception
     */
    public static ShardingSphereSchema build(final SchemaBuilderMaterials materials) throws SQLException {
        Collection<String> allTableNames = materials.getRules().stream().filter(each -> each instanceof TableContainedRule)
                .flatMap(shardingSphereRule -> ((TableContainedRule) shardingSphereRule).getTables().stream()).collect(Collectors.toSet());
        return new ShardingSphereSchema(TableMetaDataBuilder.load(allTableNames, materials));
    }
    
    /**
     * Decorate sharding sphere schema.
     *
     * @param schema sharding sphere schema
     * @param materials schema builder materials
     * @return sharding sphere schema
     */
    public static ShardingSphereSchema decorateSchema(final ShardingSphereSchema schema, final SchemaBuilderMaterials materials) {
        Map<String, TableMetaData> tableMetaDataMap = new LinkedHashMap<>(schema.getTables());
        for (ShardingSphereRule rule : materials.getRules()) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (tableMetaDataMap.containsKey(table.toLowerCase())) {
                        TableMetaData metaData = TableMetaDataBuilder.decorate(table.toLowerCase(), tableMetaDataMap.get(table), materials.getRules());
                        tableMetaDataMap.put(table, metaData);
                    }
                }
            }
        }
        return new ShardingSphereSchema(tableMetaDataMap);
    }
}
