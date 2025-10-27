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

package org.apache.shardingsphere.single.rule.attribute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Single mutable data node rule attribute.
 */
@RequiredArgsConstructor
public final class SingleMutableDataNodeRuleAttribute implements MutableDataNodeRuleAttribute {
    
    private final SingleRuleConfiguration configuration;
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, Collection<DataNode>> singleTableDataNodes;
    
    private final DatabaseType protocolType;
    
    private final SingleTableMapperRuleAttribute tableMapperRuleAttribute;
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    @Override
    public void put(final String dataSourceName, final String schemaName, final String tableName) {
        if (dataSourceNames.contains(dataSourceName)) {
            Collection<DataNode> dataNodes = singleTableDataNodes.computeIfAbsent(tableName.toLowerCase(), key -> new LinkedHashSet<>());
            dataNodes.add(new DataNode(dataSourceName, schemaName, tableName));
            tableMapperRuleAttribute.getLogicTableNames().add(tableName);
            addTableConfiguration(dataSourceName, schemaName, tableName);
        }
    }
    
    private void addTableConfiguration(final String dataSourceName, final String schemaName, final String tableName) {
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(configuration.getTables());
        if (splitTables.contains(SingleTableLoadUtils.getAllTablesNodeStr(protocolType))
                || splitTables.contains(SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(protocolType, dataSourceName, schemaName))) {
            return;
        }
        String dataNodeString = SingleTableLoadUtils.getDataNodeString(protocolType, dataSourceName, schemaName, tableName);
        if (!configuration.getTables().contains(dataNodeString)) {
            configuration.getTables().add(dataNodeString);
        }
    }
    
    @Override
    public void remove(final String schemaName, final String tableName) {
        remove(Collections.singleton(schemaName.toLowerCase()), tableName);
    }
    
    @Override
    public void remove(final Collection<String> schemaNames, final String tableName) {
        if (!singleTableDataNodes.containsKey(tableName.toLowerCase())) {
            return;
        }
        Collection<DataNode> dataNodes = singleTableDataNodes.get(tableName.toLowerCase());
        Iterator<DataNode> iterator = dataNodes.iterator();
        while (iterator.hasNext()) {
            DataNode each = iterator.next();
            if (schemaNames.contains(each.getSchemaName().toLowerCase())) {
                iterator.remove();
                configuration.getTables().remove(SingleTableLoadUtils.getDataNodeString(protocolType, each.getDataSourceName(), each.getSchemaName(), tableName));
            }
        }
        if (dataNodes.isEmpty()) {
            singleTableDataNodes.remove(tableName.toLowerCase());
            tableMapperRuleAttribute.getLogicTableNames().remove(tableName);
        }
    }
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    @Override
    public Optional<DataNode> findTableDataNode(final String schemaName, final String tableName) {
        Collection<DataNode> dataNodes = singleTableDataNodes.getOrDefault(tableName.toLowerCase(), new LinkedHashSet<>());
        for (DataNode each : dataNodes) {
            if (schemaName.equalsIgnoreCase(each.getSchemaName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public ShardingSphereRule reloadRule(final RuleConfiguration ruleConfig, final String databaseName, final Map<String, DataSource> dataSourceMap,
                                         final Collection<ShardingSphereRule> builtRules) {
        return new SingleRule((SingleRuleConfiguration) ruleConfig, databaseName, protocolType, dataSourceMap, builtRules);
    }
}
