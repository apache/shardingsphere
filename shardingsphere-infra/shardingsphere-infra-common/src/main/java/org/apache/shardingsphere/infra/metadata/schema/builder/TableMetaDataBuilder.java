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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
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
     * Build table meta data.
     *
     * @param tableName table name
     * @param materials schema builder materials
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> build(final String tableName, final SchemaBuilderMaterials materials) throws SQLException {
        TableMetaData tableMetaData = load(Collections.singletonList(tableName), materials).get(tableName);
        return Optional.ofNullable(tableMetaData).map(metaData -> decorate(tableName, metaData, materials.getRules()));
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
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, materials.getRules()).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                TableContainedRule rule = (TableContainedRule) entry.getKey();
                RuleBasedTableMetaDataBuilder<TableContainedRule> builder = entry.getValue();
                Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.getTables().contains(each)).filter(each -> !result.containsKey(each)).collect(Collectors.toList());
                if (!needLoadTables.isEmpty()) {
                    result.putAll(decorateTableMetaData(builder.load(tableNames, rule, materials)));
                }
            }
        }
        return result;
    }
    
    private static Map<String, TableMetaData> decorateTableMetaData(final Map<String, TableMetaData> tableMetaDataMap) {
        return tableMetaDataMap.entrySet().stream().map(entry -> new TableMetaData(entry.getKey(), entry.getValue().getColumns().values(), entry.getValue().getIndexes().values()))
                .collect(Collectors.toMap(TableMetaData::getName, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Decorate table meta data.
     *
     * @param tableName table name
     * @param tableMetaData table meta data
     * @param rules shardingSphere rules
     * @return table meta data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        TableMetaData result = null;
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                result = entry.getValue().decorate(tableName, null == result ? tableMetaData : result, (TableContainedRule) entry.getKey());
            }
        }
        return Optional.ofNullable(result).orElse(tableMetaData);
    }
}
