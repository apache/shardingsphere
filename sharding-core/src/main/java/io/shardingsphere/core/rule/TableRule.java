/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.keygen.KeyGenerator;
import io.shardingsphere.core.routing.strategy.ShardingStrategy;
import io.shardingsphere.core.routing.strategy.ShardingStrategyFactory;
import io.shardingsphere.core.util.InlineExpressionParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

/**
 * Table rule configuration.
 *
 * @author zhangliang
 */
@Getter
@ToString(exclude = "dataNodeIndexMap")
public final class TableRule {
    
    private final String logicTable;
    
    private final List<DataNode> actualDataNodes;
    
    @Getter(AccessLevel.NONE)
    private final Map<DataNode, Integer> dataNodeIndexMap;
    
    private final ShardingStrategy databaseShardingStrategy;
    
    private final ShardingStrategy tableShardingStrategy;
    
    private final String generateKeyColumn;
    
    private final KeyGenerator keyGenerator;
    
    private final String logicIndex;
    
    private final Collection<String> actualDatasourceNames = new LinkedHashSet<>();
    
    private final Map<String, Collection<String>> datasourceActualTables = new HashMap<>();
    
    public TableRule(final String defaultDataSourceName, final String logicTableName) {
        logicTable = logicTableName.toLowerCase();
        actualDataNodes = Collections.singletonList(new DataNode(defaultDataSourceName, logicTableName));
        fillActualDatasourceNames();
        dataNodeIndexMap = Collections.emptyMap();
        databaseShardingStrategy = null;
        tableShardingStrategy = null;
        generateKeyColumn = null;
        keyGenerator = null;
        logicIndex = null;
    }
    
    public TableRule(final Collection<String> dataSourceNames, final String logicTableName) {
        logicTable = logicTableName.toLowerCase();
        dataNodeIndexMap = new HashMap<>(dataSourceNames.size(), 1);
        actualDataNodes = generateDataNodes(logicTableName, dataSourceNames);
        databaseShardingStrategy = null;
        tableShardingStrategy = null;
        generateKeyColumn = null;
        keyGenerator = null;
        logicIndex = null;
    }
    
    public TableRule(final TableRuleConfiguration tableRuleConfig, final ShardingDataSourceNames shardingDataSourceNames) {
        Preconditions.checkNotNull(tableRuleConfig.getLogicTable(), "Logic table cannot be null.");
        logicTable = tableRuleConfig.getLogicTable().toLowerCase();
        List<String> dataNodes = new InlineExpressionParser(tableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1);
        actualDataNodes = isEmptyDataNodes(dataNodes)
                ? generateDataNodes(tableRuleConfig.getLogicTable(), shardingDataSourceNames.getDataSourceNames()) : generateDataNodes(dataNodes, shardingDataSourceNames.getDataSourceNames());
        databaseShardingStrategy = null == tableRuleConfig.getDatabaseShardingStrategyConfig() ? null : ShardingStrategyFactory.newInstance(tableRuleConfig.getDatabaseShardingStrategyConfig());
        tableShardingStrategy = null == tableRuleConfig.getTableShardingStrategyConfig() ? null : ShardingStrategyFactory.newInstance(tableRuleConfig.getTableShardingStrategyConfig());
        generateKeyColumn = tableRuleConfig.getKeyGeneratorColumnName();
        keyGenerator = tableRuleConfig.getKeyGenerator();
        logicIndex = null == tableRuleConfig.getLogicIndex() ? null : tableRuleConfig.getLogicIndex().toLowerCase();
    }
    
    private boolean isEmptyDataNodes(final List<String> dataNodes) {
        return null == dataNodes || dataNodes.isEmpty();
    }
    
    private List<DataNode> generateDataNodes(final String logicTable, final Collection<String> dataSourceNames) {
        List<DataNode> result = new LinkedList<>();
        int index = 0;
        for (String each : dataSourceNames) {
            DataNode dataNode = new DataNode(each, logicTable);
            result.add(dataNode);
            dataNodeIndexMap.put(dataNode, index);
            actualDatasourceNames.add(each);
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            index++;
        }
        return result;
    }
    
    private List<DataNode> generateDataNodes(final List<String> actualDataNodes, final Collection<String> dataSourceNames) {
        List<DataNode> result = new LinkedList<>();
        int index = 0;
        for (String each : actualDataNodes) {
            DataNode dataNode = new DataNode(each);
            if (!dataSourceNames.contains(dataNode.getDataSourceName())) {
                throw new ShardingException("Cannot find data source in sharding rule, invalid actual data node is: '%s'", each);
            }
            result.add(dataNode);
            actualDatasourceNames.add(dataNode.getDataSourceName());
            addActualTable(dataNode.getDataSourceName(), dataNode.getTableName());
            dataNodeIndexMap.put(dataNode, index);
            index++;
        }
        return result;
    }
    
    /**
     * Get data node groups.
     *
     * @return data node groups, key is data source name, value is tables belong to this data source
     */
    public Map<String, List<DataNode>> getDataNodeGroups() {
        Map<String, List<DataNode>> result = new LinkedHashMap<>(actualDataNodes.size(), 1);
        for (DataNode each : actualDataNodes) {
            String dataSourceName = each.getDataSourceName();
            if (!result.containsKey(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<DataNode>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
    
    /**
     * Get actual data source names.
     *
     * @return actual data source names
     */
    public Collection<String> getActualDatasourceNames() {
        return actualDatasourceNames;
    }
    
    private void fillActualDatasourceNames() {
        for (DataNode each : actualDataNodes) {
            actualDatasourceNames.add(each.getDataSourceName());
            addActualTable(each.getDataSourceName(), each.getTableName());
        }
    }
    
    private void addActualTable(final String datasourceName, final String tableName) {
        Collection<String> actualTables = datasourceActualTables.get(datasourceName);
        if (null == actualTables) {
            actualTables = new LinkedHashSet<>();
            datasourceActualTables.put(datasourceName, actualTables);
        }
        actualTables.add(tableName);
    }
    
    /**
     * Get actual table names via target data source name.
     *
     * @param targetDataSource target data source name
     * @return names of actual tables
     */
    public Collection<String> getActualTableNames(final String targetDataSource) {
        Collection<String> result = datasourceActualTables.get(targetDataSource);
        if (null == result) {
            result = Collections.emptySet();
        }
        return result;
    }
    
    int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        DataNode dataNode = new DataNode(dataSourceName, actualTableName);
        return dataNodeIndexMap.containsKey(dataNode) ? dataNodeIndexMap.get(dataNode) : -1;
    }
    
    boolean isExisted(final String actualTableName) {
        for (DataNode each : actualDataNodes) {
            if (each.getTableName().equalsIgnoreCase(actualTableName)) {
                return true;
            }
        }
        return false;
    }
}
