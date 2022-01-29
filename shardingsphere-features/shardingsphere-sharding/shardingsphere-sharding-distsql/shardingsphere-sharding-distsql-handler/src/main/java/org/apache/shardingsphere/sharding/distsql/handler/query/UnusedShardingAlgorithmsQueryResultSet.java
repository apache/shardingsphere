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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingAlgorithmsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Result set for show unused sharding algorithms.
 */
public final class UnusedShardingAlgorithmsQueryResultSet implements DistSQLResultSet {
    
    private static final String TYPE = ShowUnusedShardingAlgorithmsStatement.class.getName();
    
    private static final String NAME = "name";
    
    private static final String COLUMN_TYPE = "type";
    
    private static final String PROPS = "props";
    
    private Iterator<Entry<String, ShardingSphereAlgorithmConfiguration>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findAny();
        ruleConfig.ifPresent(this::getUnusedShardingAlgorithms);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(NAME, COLUMN_TYPE, PROPS);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildTableRowData(data.next());
    }
    
    private Collection<Object> buildTableRowData(final Entry<String, ShardingSphereAlgorithmConfiguration> data) {
        Collection<Object> result = new LinkedList<>();
        result.add(data.getKey());
        result.add(data.getValue().getType());
        result.add(buildProps(data.getValue().getProps()));
        return result;
    }
    
    private Object buildProps(final Properties props) {
        return Objects.nonNull(props) ? PropertiesConverter.convert(props) : "";
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    private void getUnusedShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> inUsedSet = getUsedShardingAlgorithms(shardingRuleConfig);
        Map<String, ShardingSphereAlgorithmConfiguration> map = new HashMap<>();
        for (Entry<String, ShardingSphereAlgorithmConfiguration> each : shardingRuleConfig.getShardingAlgorithms().entrySet()) {
            if (!inUsedSet.contains(each.getKey())) {
                map.put(each.getKey(), each.getValue());
            }
        }
        data = map.entrySet().iterator();
    }
    
    private Collection<String> getUsedShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().forEach(each -> {
            if (Objects.nonNull(each.getDatabaseShardingStrategy())) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (Objects.nonNull(each.getTableShardingStrategy())) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        shardingRuleConfig.getAutoTables().stream().filter(each -> Objects.nonNull(each.getShardingStrategy())).forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
        ShardingStrategyConfiguration tableShardingStrategy = shardingRuleConfig.getDefaultTableShardingStrategy();
        if (Objects.nonNull(tableShardingStrategy) && !Strings.isNullOrEmpty(tableShardingStrategy.getShardingAlgorithmName())) {
            result.add(tableShardingStrategy.getShardingAlgorithmName());
        }
        ShardingStrategyConfiguration databaseShardingStrategy = shardingRuleConfig.getDefaultDatabaseShardingStrategy();
        if (Objects.nonNull(databaseShardingStrategy) && !Strings.isNullOrEmpty(databaseShardingStrategy.getShardingAlgorithmName())) {
            result.add(databaseShardingStrategy.getShardingAlgorithmName());
        }
        return result;
    }
}
