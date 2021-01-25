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
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

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
        Optional<TableMetaData> tableMetaData = load(tableName, materials);
        return tableMetaData.map(optional -> decorate(tableName, optional, materials.getRules()));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Optional<TableMetaData> load(final String tableName, final SchemaBuilderMaterials materials) throws SQLException {
        DataNodes dataNodes = new DataNodes(materials.getRules());
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(materials.getRules(), RuleBasedTableMetaDataBuilder.class).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                TableContainedRule rule = (TableContainedRule) entry.getKey();
                RuleBasedTableMetaDataBuilder loader = entry.getValue();
                Optional<TableMetaData> result = loader.load(tableName, materials.getDatabaseType(), materials.getDataSourceMap(), dataNodes, rule, materials.getProps());
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        TableMetaData result = null;
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(rules, RuleBasedTableMetaDataBuilder.class).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                result = entry.getValue().decorate(tableName, null == result ? tableMetaData : result, (TableContainedRule) entry.getKey());
            }
        }
        return Optional.ofNullable(result).orElse(tableMetaData);
    }
}
