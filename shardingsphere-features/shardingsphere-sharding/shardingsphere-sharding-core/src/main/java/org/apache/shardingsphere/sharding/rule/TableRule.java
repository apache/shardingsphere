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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.datanode.DataNodeUtil;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Table rule.
 */
@Getter
@ToString(exclude = {"dataNodeIndexMap", "actualTables", "actualTablePrefix", "actualDataSourceNames", "dataSourceDataNode", "tableDataNode"})
public final class TableRule {
    
    private static final Pattern DATA_NODE_SUFFIX_PATTERN = Pattern.compile("(\\d+[\\-_]){0,}(\\d+$)");
    
    private static final char DEFAULT_PADDING_CHAR = '0';
    
    private final String logicTable;
    
    private final List<DataNode> actualDataNodes;
    
    private final String actualTablePrefix;
    
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
    
    public TableRule(final Collection<String> dataSourceNames, final String logicTableName) {
        logicTable = logicTableName;
        dataNodeIndexMap = new HashMap<>(dataSourceNames.size(), 1);
        actualTablePrefix = null;
        actualDataNodes = generateDataNodes(logicTableName, dataSourceNames, null);
        actualTables = getActualTables();
        databaseShardingStrategyConfig = null;
        tableShardingStrategyConfig = null;
        auditStrategyConfig = null;
        generateKeyColumn = null;
        keyGeneratorName = null;
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
    }
    
    public TableRule(final ShardingTableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames, final String defaultGenerateKeyColumn) {
        logicTable = tableRuleConfig.getLogicTable();
        List<String> dataNodes = new InlineExpressionParser(tableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1);
        actualTablePrefix = tableRuleConfig.getActualTablePrefix();
        actualDataNodes = isEmptyDataNodes(dataNodes) ? generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames, actualTablePrefix)
                : generateDataNodes(dataNodes, dataSourceNames, actualTablePrefix);
        actualTables = getActualTables();
        databaseShardingStrategyConfig = tableRuleConfig.getDatabaseShardingStrategy();
        tableShardingStrategyConfig = tableRuleConfig.getTableShardingStrategy();
        auditStrategyConfig = tableRuleConfig.getAuditStrategy();
        KeyGenerateStrategyConfiguration keyGeneratorConfig = tableRuleConfig.getKeyGenerateStrategy();
        generateKeyColumn = null != keyGeneratorConfig && !Strings.isNullOrEmpty(keyGeneratorConfig.getColumn()) ? keyGeneratorConfig.getColumn() : defaultGenerateKeyColumn;
        keyGeneratorName = null == keyGeneratorConfig ? null : keyGeneratorConfig.getKeyGeneratorName();
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
        checkRule(dataNodes);
    }
    
    public TableRule(final ShardingAutoTableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames,
                     final ShardingAutoTableAlgorithm shardingAutoTableAlgorithm, final String defaultGenerateKeyColumn) {
        logicTable = tableRuleConfig.getLogicTable();
        databaseShardingStrategyConfig = new NoneShardingStrategyConfiguration();
        tableShardingStrategyConfig = tableRuleConfig.getShardingStrategy();
        auditStrategyConfig = tableRuleConfig.getAuditStrategy();
        List<String> dataNodes = getDataNodes(tableRuleConfig, shardingAutoTableAlgorithm, dataSourceNames);
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1);
        actualTablePrefix = tableRuleConfig.getActualTablePrefix();
        actualDataNodes = isEmptyDataNodes(dataNodes) ? generateDataNodes(tableRuleConfig.getLogicTable(), dataSourceNames, actualTablePrefix)
                : generateDataNodes(dataNodes, dataSourceNames, actualTablePrefix);
        actualTables = getActualTables();
        KeyGenerateStrategyConfiguration keyGeneratorConfig = tableRuleConfig.getKeyGenerateStrategy();
        generateKeyColumn = null != keyGeneratorConfig && !Strings.isNullOrEmpty(keyGeneratorConfig.getColumn()) ? keyGeneratorConfig.getColumn() : defaultGenerateKeyColumn;
        keyGeneratorName = null == keyGeneratorConfig ? null : keyGeneratorConfig.getKeyGeneratorName();
        dataSourceDataNode = actualDataNodes.isEmpty() ? null : createDataSourceDataNode(actualDataNodes);
        tableDataNode = actualDataNodes.isEmpty() ? null : createTableDataNode(actualDataNodes);
        checkRule(dataNodes);
    }
    
    private DataNodeInfo createDataSourceDataNode(final Collection<DataNode> actualDataNodes) {
        String prefix = DATA_NODE_SUFFIX_PATTERN.matcher(actualDataNodes.iterator().next().getDataSourceName()).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getDataSourceName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, DEFAULT_PADDING_CHAR);
    }
    
    private DataNodeInfo createTableDataNode(final Collection<DataNode> actualDataNodes) {
        String prefix = DATA_NODE_SUFFIX_PATTERN.matcher(actualDataNodes.iterator().next().getTableName()).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getTableName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, DEFAULT_PADDING_CHAR);
    }
    
    private List<String> getDataNodes(final ShardingAutoTableRuleConfiguration tableRuleConfig, final ShardingAutoTableAlgorithm shardingAlgorithm, final Collection<String> dataSourceNames) {
        if (null == tableShardingStrategyConfig) {
            return new LinkedList<>();
        }
        List<String> dataSources = Strings.isNullOrEmpty(tableRuleConfig.getActualDataSources()) ? new LinkedList<>(dataSourceNames)
                : new InlineExpressionParser(tableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return DataNodeUtil.getFormatDataNodes(shardingAlgorithm.getAutoTablesAmount(), logicTable, dataSources);
    }
    
    private Set<String> getActualTables() {
        return actualDataNodes.stream().map(DataNode::getTableName).collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
    }
    
    private void addActualTable(final String datasourceName, final String tableName) {
        dataSourceToTablesMap.computeIfAbsent(datasourceName, key -> new LinkedHashSet<>()).add(tableName);
    }
    
    private boolean isEmptyDataNodes(final List<String> dataNodes) {
        return null == dataNodes || dataNodes.isEmpty();
    }
    
    private List<DataNode> generateDataNodes(final String logicTable, final Collection<String> dataSourceNames, final String actualTablePrefix) {
        List<DataNode> result = new LinkedList<>();
        int index = 0;
        for (String each : dataSourceNames) {
            String actualTable = Strings.isNullOrEmpty(actualTablePrefix) ? logicTable : actualTablePrefix + logicTable;
            DataNode dataNode = new DataNode(each, actualTable);
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDataSourceNames.add(each);
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    private List<DataNode> generateDataNodes(final List<String> actualDataNodes, final Collection<String> dataSourceNames, final String actualTablePrefix) {
        List<DataNode> result = new LinkedList<>();
        int index = 0;
        for (String each : actualDataNodes) {
            DataNode dataNode = generateDataNode(each, actualTablePrefix);
            if (!dataSourceNames.contains(dataNode.getDataSourceName())) {
                throw new ShardingSphereException("Cannot find data source in sharding rule, invalid actual data node is: '%s'", each);
            }
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDataSourceNames.add(dataNode.getDataSourceName());
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    private DataNode generateDataNode(final String dataNode, final String actualTablePrefix) {
        DataNode result = new DataNode(dataNode);
        if (Strings.isNullOrEmpty(actualTablePrefix)) {
            return result;
        }
        return new DataNode(result.getDataSourceName(), actualTablePrefix + result.getTableName());
    }
    
    /**
     * Get data node groups.
     *
     * @return data node groups, key is data source name, values are data nodes belong to this data source
     */
    public Map<String, List<DataNode>> getDataNodeGroups() {
        return DataNodeUtil.getDataNodeGroups(actualDataNodes);
    }
    
    /**
     * Get actual data source names.
     *
     * @return actual data source names
     */
    public Collection<String> getActualDataSourceNames() {
        return actualDataSourceNames;
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
    
    int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        return dataNodeIndexMap.getOrDefault(new DataNode(dataSourceName, actualTableName), -1);
    }
    
    boolean isExisted(final String actualTableName) {
        return actualTables.contains(actualTableName);
    }
    
    private void checkRule(final List<String> dataNodes) {
        if (isEmptyDataNodes(dataNodes) && null != tableShardingStrategyConfig && !(tableShardingStrategyConfig instanceof NoneShardingStrategyConfiguration)) {
            throw new ShardingSphereConfigurationException("ActualDataNodes must be configured if want to shard tables for logicTable [%s]", logicTable);
        }
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
