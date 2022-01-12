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
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Table meta data builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataBuilder {
    
    static {
        ShardingSphereServiceLoader.register(RuleBasedTableMetaDataBuilder.class);
    }
    
    /**
     * Load table metadata.
     *
     * @param tableNames table name collection
     * @param materials schema builder materials
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, TableMetaData> load(final Collection<String> tableNames, final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, TableMetaData> tableMetaDataMap = new LinkedHashMap<>();
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, materials.getRules()).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                TableContainedRule rule = (TableContainedRule) entry.getKey();
                RuleBasedTableMetaDataBuilder<TableContainedRule> builder = entry.getValue();
                Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.getTables().contains(each))
                        .filter(each -> !tableMetaDataMap.containsKey(each)).collect(Collectors.toList());
                if (!needLoadTables.isEmpty()) {
                    tableMetaDataMap.putAll(builder.load(needLoadTables, rule, materials));
                }
            }
        }
        return decorate(tableMetaDataMap, materials.getRules());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, TableMetaData> decorate(final Map<String, TableMetaData> tableMetaDataMap, final Collection<ShardingSphereRule> rules) {
        for (Entry<String, TableMetaData> entry : tableMetaDataMap.entrySet()) {
            for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> builderEntry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).entrySet()) {
                if (builderEntry.getKey() instanceof TableContainedRule) {
                    entry.setValue(builderEntry.getValue().decorate(entry.getKey(), entry.getValue(), (TableContainedRule) builderEntry.getKey()));
                }
            }
        }
        return tableMetaDataMap;
    }
}
