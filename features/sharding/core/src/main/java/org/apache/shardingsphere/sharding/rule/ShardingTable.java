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

package org.apache.shardingsphere.sharding.rule;

import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.datanode.DataNodeUtils;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.constant.ShardingTableConstants;
import org.apache.shardingsphere.sharding.exception.metadata.DataNodeGenerateException;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredDataNodesException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding table.
 */
@Getter
@ToString(exclude = {"dataNodeIndexMap", "actualTables", "actualDataSourceNames", "dataSourceDataNode", "tableDataNode"})
public final class ShardingTable {
    
    private final String logicTable;
    
    private final List<DataNode> actualDataNodes;
    
    @Getter(AccessLevel.NONE)
    private final Set<String> actualTables;
    
    @Getter(AccessLevel.NONE)
    private final Map<DataNode, Integer> dataNodeIndexMap;
    
    private final ShardingStrategyConfiguration databaseShardingStrategyConfig;
    
    private final ShardingStrategyConfiguration tableShardingStrategyConfig;
    
    private final ShardingAuditStrategyConfiguration auditStrategyConfig;
    
    @Getter(AccessLevel.NONE)
    private final String generateKeyColumn;
    
    private final String keyGeneratorName;
    
    private final Collection<String> actualDataSourceNames = new LinkedHashSet<>();
    
    private final Map<String, Collection<String>> dataSourceToTablesMap = new HashMap<>();
    
    private final DataNodeInfo dataSourceDataNode;
    
    private final DataNodeInfo tableDataNode;
    
    public ShardingTable(final Collection<String> dataSourceNames, final String logicTableName) {
        logicTable = logicTableName;
        dataNodeIndexMap = new HashMap<>(dataSourceNames.size(), 1F);
        actualDataNodes = generateDataNodes(logicTableName, dataSourceNames);
        actualTables = getActualTables();
        databaseShardingStrategyConfig = null;
        tableShardingStrategyConfig = null;
        auditStrategyConfig = null;
        generateKeyColumn = null;
        keyGeneratorName = null;
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
    }
    
    public ShardingTable(final ShardingTableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames, final String defaultGenerateKeyColumn) {
        logicTable = tableRuleConfig.getLogicTable();
        Collection<String> dataNodes = InlineExpressionParserFactory.newInstance(tableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1F);
        actualDataNodes = isEmptyDataNodes(dataNodes) ? generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames) : generateDataNodes(dataNodes, dataSourceNames);
        actualTables = getActualTables();
        databaseShardingStrategyConfig = tableRuleConfig.getDatabaseShardingStrategy();
        tableShardingStrategyConfig = tableRuleConfig.getTableShardingStrategy();
        auditStrategyConfig = tableRuleConfig.getAuditStrategy();
        KeyGenerateStrategyConfiguration keyGeneratorConfig = tableRuleConfig.getKeyGenerateStrategy();
        generateKeyColumn = null == keyGeneratorConfig || Strings.isNullOrEmpty(keyGeneratorConfig.getColumn()) ? defaultGenerateKeyColumn : keyGeneratorConfig.getColumn();
        keyGeneratorName = null == keyGeneratorConfig ? null : keyGeneratorConfig.getKeyGeneratorName();
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
        checkRule(dataNodes);
    }
    
    public ShardingTable(final ShardingAutoTableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames,
                         final ShardingAutoTableAlgorithm shardingAutoTableAlgorithm, final String defaultGenerateKeyColumn) {
        logicTable = tableRuleConfig.getLogicTable();
        databaseShardingStrategyConfig = new NoneShardingStrategyConfiguration();
        tableShardingStrategyConfig = tableRuleConfig.getShardingStrategy();
        auditStrategyConfig = tableRuleConfig.getAuditStrategy();
        List<String> dataNodes = getDataNodes(tableRuleConfig, shardingAutoTableAlgorithm, dataSourceNames);
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1F);
        actualDataNodes = isEmptyDataNodes(dataNodes) ? generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames) : generateDataNodes(dataNodes, dataSourceNames);
        actualTables = getActualTables();
        KeyGenerateStrategyConfiguration keyGeneratorConfig = tableRuleConfig.getKeyGenerateStrategy();
        generateKeyColumn = null == keyGeneratorConfig || Strings.isNullOrEmpty(keyGeneratorConfig.getColumn()) ? defaultGenerateKeyColumn : keyGeneratorConfig.getColumn();
        keyGeneratorName = null == keyGeneratorConfig ? null : keyGeneratorConfig.getKeyGeneratorName();
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
        checkRule(dataNodes);
    }
    
    private DataNodeInfo createDataSourceDataNode(final Collection<DataNode> actualDataNodes) {
        String prefix = ShardingTableConstants.DATA_NODE_SUFFIX_PATTERN.matcher(actualDataNodes.iterator().next().getDataSourceName()).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getDataSourceName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, ShardingTableConstants.DEFAULT_PADDING_CHAR);
    }
    
    private DataNodeInfo createTableDataNode(final Collection<DataNode> actualDataNodes) {
        String tableName = actualDataNodes.iterator().next().getTableName();
        String prefix = tableName.startsWith(logicTable) ? logicTable + ShardingTableConstants.DATA_NODE_SUFFIX_PATTERN.matcher(tableName.substring(logicTable.length())).replaceAll("")
                : ShardingTableConstants.DATA_NODE_SUFFIX_PATTERN.matcher(tableName).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getTableName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, ShardingTableConstants.DEFAULT_PADDING_CHAR);
    }
    
    private List<String> getDataNodes(final ShardingAutoTableRuleConfiguration tableRuleConfig, final ShardingAutoTableAlgorithm shardingAlgorithm, final Collection<String> dataSourceNames) {
        if (null == tableShardingStrategyConfig) {
            return new ArrayList<>();
        }
        List<String> dataSources = Strings.isNullOrEmpty(tableRuleConfig.getActualDataSources()) ? new ArrayList<>(dataSourceNames)
                : InlineExpressionParserFactory.newInstance(tableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return DataNodeUtils.getFormattedDataNodes(shardingAlgorithm.getAutoTablesAmount(), logicTable, dataSources);
    }
    
    private Set<String> getActualTables() {
        return actualDataNodes.stream().map(DataNode::getTableName).collect(Collectors.toCollection(CaseInsensitiveSet::new));
    }
    
    private void addActualTable(final String datasourceName, final String tableName) {
        dataSourceToTablesMap.computeIfAbsent(datasourceName, key -> new LinkedHashSet<>()).add(tableName);
    }
    
    private boolean isEmptyDataNodes(final Collection<String> dataNodes) {
        return null == dataNodes || dataNodes.isEmpty();
    }
    
    private List<DataNode> generateDataNodes(final String logicTable, final Collection<String> dataSourceNames) {
        List<DataNode> result = new ArrayList<>(dataSourceNames.size());
        int index = 0;
        for (String each : dataSourceNames) {
            DataNode dataNode = new DataNode(each, (String) null, logicTable);
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDataSourceNames.add(each);
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    private List<DataNode> generateDataNodes(final Collection<String> actualDataNodes, final Collection<String> dataSourceNames) {
        List<DataNode> result = new ArrayList<>(actualDataNodes.size());
        int index = 0;
        for (String each : actualDataNodes) {
            DataNode dataNode = new DataNode(each);
            if (!dataSourceNames.contains(dataNode.getDataSourceName())) {
                throw new DataNodeGenerateException(each);
            }
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDataSourceNames.add(dataNode.getDataSourceName());
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    /**
     * Get data node groups.
     *
     * @return data node groups, key is data source name, values are data nodes belong to this data source
     */
    public Map<String, List<DataNode>> getDataNodeGroups() {
        return DataNodeUtils.getDataNodeGroups(actualDataNodes);
    }
    
    /**
     * Get actual table names via target data source name.
     *
     * @param targetDataSource target data source name
     * @return names of actual tables
     */
    public Collection<String> getActualTableNames(final String targetDataSource) {
        return dataSourceToTablesMap.getOrDefault(targetDataSource, Collections.emptySet());
    }
    
    /**
     * Find actual table index.
     *
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return actual table index
     */
    public int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        return dataNodeIndexMap.getOrDefault(new DataNode(dataSourceName, (String) null, actualTableName), -1);
    }
    
    /**
     * Is existed.
     *
     * @param actualTableName actual table name
     * @return is existed or not
     */
    public boolean isExisted(final String actualTableName) {
        return actualTables.contains(actualTableName);
    }
    
    private void checkRule(final Collection<String> dataNodes) {
        ShardingSpherePreconditions.checkState(!isEmptyDataNodes(dataNodes) || null == tableShardingStrategyConfig || tableShardingStrategyConfig instanceof NoneShardingStrategyConfiguration,
                () -> new MissingRequiredDataNodesException(logicTable));
    }
    
    /**
     * Get generate key column.
     *
     * @return generate key column
     */
    public Optional<String> getGenerateKeyColumn() {
        return Optional.ofNullable(generateKeyColumn);
    }
}
