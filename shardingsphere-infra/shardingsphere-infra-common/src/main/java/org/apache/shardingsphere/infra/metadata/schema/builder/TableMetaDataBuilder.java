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
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, materials.getRules()).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                TableContainedRule rule = (TableContainedRule) entry.getKey();
                RuleBasedTableMetaDataBuilder<TableContainedRule> builder = entry.getValue();
                Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.getTables().contains(each)).filter(each -> !result.containsKey(each)).collect(Collectors.toList());
                if (!needLoadTables.isEmpty()) {
                    result.putAll(builder.load(needLoadTables, rule, materials));
                }
            }
        }
        return result;
    }
    
    /**
     * Decorate federate table meta data.
     *
     * @param tableMetaData table meta data
     * @param rules shardingSphere rules
     * @return table meta data
     */
    public static TableMetaData decorateFederateTableMetaData(final TableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        return decorateTableName(tableMetaData, rules);
    }
    
    /**
     * Decorate kernel table meta data.
     *
     * @param tableMetaData table meta data
     * @param rules shardingSphere rules
     * @return table meta data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TableMetaData decorateKernelTableMetaData(final TableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        TableMetaData result = decorateTableName(tableMetaData, rules);
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                result = entry.getValue().decorate(result.getName(), result, (TableContainedRule) entry.getKey());
            }
        }
        return result;
    }
    
    private static TableMetaData decorateTableName(final TableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        for (ShardingSphereRule each : rules) {
            if (each instanceof DataNodeContainedRule) {
                Optional<String> logicTable = ((DataNodeContainedRule) each).findLogicTableByActualTable(tableMetaData.getName());
                if (logicTable.isPresent()) {
                    return new TableMetaData(logicTable.get(), tableMetaData.getColumns().values(), tableMetaData.getIndexes().values());
                }
            }
        }
        return new TableMetaData(tableMetaData.getName(), tableMetaData.getColumns().values(), tableMetaData.getIndexes().values());
    }
    
}
