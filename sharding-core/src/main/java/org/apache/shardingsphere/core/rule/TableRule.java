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

package org.apache.shardingsphere.core.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.core.encrypt.ShardingEncryptorFactory;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.keygen.ShardingKeyGenerator;
import org.apache.shardingsphere.core.keygen.ShardingKeyGeneratorFactory;
import org.apache.shardingsphere.core.routing.strategy.ShardingEncryptorStrategy;
import org.apache.shardingsphere.core.routing.strategy.ShardingStrategy;
import org.apache.shardingsphere.core.routing.strategy.ShardingStrategyFactory;
import org.apache.shardingsphere.core.util.InlineExpressionParser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Table rule.
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
    
    private final ShardingKeyGenerator shardingKeyGenerator;
    
    private final ShardingEncryptorStrategy shardingEncryptorStrategy;
    
    private final String logicIndex;
    
    public TableRule(final String defaultDataSourceName, final String logicTableName) {
        logicTable = logicTableName.toLowerCase();
        actualDataNodes = Collections.singletonList(new DataNode(defaultDataSourceName, logicTableName));
        dataNodeIndexMap = Collections.emptyMap();
        databaseShardingStrategy = null;
        tableShardingStrategy = null;
        generateKeyColumn = null;
        shardingKeyGenerator = null;
        shardingEncryptorStrategy = null;
        logicIndex = null;
    }
    
    public TableRule(final Collection<String> dataSourceNames, final String logicTableName) {
        logicTable = logicTableName.toLowerCase();
        dataNodeIndexMap = new HashMap<>(dataSourceNames.size(), 1);
        actualDataNodes = generateDataNodes(logicTableName, dataSourceNames);
        databaseShardingStrategy = null;
        tableShardingStrategy = null;
        generateKeyColumn = null;
        shardingKeyGenerator = null;
        shardingEncryptorStrategy = null;
        logicIndex = null;
    }
    
    public TableRule(final TableRuleConfiguration tableRuleConfig, final ShardingDataSourceNames shardingDataSourceNames, final String defaultGenerateKeyColumn) {
        Preconditions.checkNotNull(tableRuleConfig.getLogicTable(), "Logic table cannot be null.");
        logicTable = tableRuleConfig.getLogicTable().toLowerCase();
        List<String> dataNodes = new InlineExpressionParser(tableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        dataNodeIndexMap = new HashMap<>(dataNodes.size(), 1);
        actualDataNodes = isEmptyDataNodes(dataNodes)
            ? generateDataNodes(tableRuleConfig.getLogicTable(), shardingDataSourceNames.getDataSourceNames()) : generateDataNodes(dataNodes, shardingDataSourceNames.getDataSourceNames());
        databaseShardingStrategy = null == tableRuleConfig.getDatabaseShardingStrategyConfig() ? null : ShardingStrategyFactory.newInstance(tableRuleConfig.getDatabaseShardingStrategyConfig());
        tableShardingStrategy = null == tableRuleConfig.getTableShardingStrategyConfig() ? null : ShardingStrategyFactory.newInstance(tableRuleConfig.getTableShardingStrategyConfig());
        generateKeyColumn = getGenerateKeyColumn(tableRuleConfig.getKeyGeneratorConfig(), defaultGenerateKeyColumn);
        shardingKeyGenerator = containsKeyGeneratorConfiguration(tableRuleConfig)
                ? ShardingKeyGeneratorFactory.getInstance().newAlgorithm(tableRuleConfig.getKeyGeneratorConfig().getType(), tableRuleConfig.getKeyGeneratorConfig().getProps()) : null;
        shardingEncryptorStrategy = null == tableRuleConfig.getEncryptorConfig() ? null : getShardingEncryptorStrategy(tableRuleConfig.getEncryptorConfig());
        logicIndex = null == tableRuleConfig.getLogicIndex() ? null : tableRuleConfig.getLogicIndex().toLowerCase();
    }
    
    private boolean containsKeyGeneratorConfiguration(final TableRuleConfiguration tableRuleConfiguration) {
        return null != tableRuleConfiguration.getKeyGeneratorConfig() && !Strings.isNullOrEmpty(tableRuleConfiguration.getKeyGeneratorConfig().getType());
    }
    
    private ShardingEncryptorStrategy getShardingEncryptorStrategy(final EncryptorConfiguration encryptorConfiguration) {
        ShardingEncryptor shardingEncryptor = ShardingEncryptorFactory.getInstance().newAlgorithm(encryptorConfiguration.getType(), encryptorConfiguration.getProps());
        List<String> columns = Splitter.on(",").trimResults().splitToList(encryptorConfiguration.getColumns());
        List<String> assistedQueryColumns = Strings.isNullOrEmpty(encryptorConfiguration.getAssistedQueryColumns())
                ? Collections.<String>emptyList() : Splitter.on(",").trimResults().splitToList(encryptorConfiguration.getAssistedQueryColumns());
        return new ShardingEncryptorStrategy(columns, assistedQueryColumns, shardingEncryptor); 
    }
    
    private String getGenerateKeyColumn(final KeyGeneratorConfiguration keyGeneratorConfiguration, final String defaultGenerateKeyColumn) {
        if (null != keyGeneratorConfiguration && !Strings.isNullOrEmpty(keyGeneratorConfiguration.getColumn())) {
            return keyGeneratorConfiguration.getColumn();
        }
        return defaultGenerateKeyColumn;
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
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size());
        for (DataNode each : actualDataNodes) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * Get actual table names via target data source name.
     *
     * @param targetDataSource target data source name
     * @return names of actual tables
     */
    public Collection<String> getActualTableNames(final String targetDataSource) {
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size());
        for (DataNode each : actualDataNodes) {
            if (targetDataSource.equals(each.getDataSourceName())) {
                result.add(each.getTableName());
            }
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
