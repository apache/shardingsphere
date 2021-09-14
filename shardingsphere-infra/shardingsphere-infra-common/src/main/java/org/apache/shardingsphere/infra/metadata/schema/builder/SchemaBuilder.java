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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
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
    public static ShardingSphereSchema decorate(final ShardingSphereSchema schema, final SchemaBuilderMaterials materials) {
        Map<String, TableMetaData> tableMetaDataMap = schema.getTables().values().stream().collect(Collectors
            .toMap(TableMetaData::getName, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        for (ShardingSphereRule each : materials.getRules()) {
            if (each instanceof TableContainedRule) {
                decorateByRule(tableMetaDataMap, each);
            }
        }
        return new ShardingSphereSchema(tableMetaDataMap);
    }
    
    private static void decorateByRule(final Map<String, TableMetaData> tableMetaDataMap, final ShardingSphereRule rule) {
        for (String each : ((TableContainedRule) rule).getTables()) {
            if (tableMetaDataMap.containsKey(each)) {
                TableMetaData metaData = TableMetaDataBuilder.decorate(each, tableMetaDataMap.get(each), Collections.singletonList(rule));
                tableMetaDataMap.put(each, metaData);
            }
        }
    }
}
