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
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Show unused sharding algorithms executor.
 */
public final class ShowUnusedShardingAlgorithmsExecutor implements RQLExecutor<ShowUnusedShardingAlgorithmsStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowUnusedShardingAlgorithmsStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) rule.get().getConfiguration();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Collection<String> inUsedAlgorithms = getUsedShardingAlgorithms(shardingRuleConfig);
        for (Entry<String, AlgorithmConfiguration> entry : shardingRuleConfig.getShardingAlgorithms().entrySet()) {
            if (!inUsedAlgorithms.contains(entry.getKey())) {
                result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), buildProps(entry.getValue().getProps())));
            }
        }
        return result;
    }
    
    private Collection<String> getUsedShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().forEach(each -> {
            if (null != each.getDatabaseShardingStrategy()) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (null != each.getTableShardingStrategy()) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        shardingRuleConfig.getAutoTables().stream().filter(each -> null != each.getShardingStrategy()).forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
        ShardingStrategyConfiguration tableShardingStrategy = shardingRuleConfig.getDefaultTableShardingStrategy();
        if (null != tableShardingStrategy && !Strings.isNullOrEmpty(tableShardingStrategy.getShardingAlgorithmName())) {
            result.add(tableShardingStrategy.getShardingAlgorithmName());
        }
        ShardingStrategyConfiguration databaseShardingStrategy = shardingRuleConfig.getDefaultDatabaseShardingStrategy();
        if (null != databaseShardingStrategy && !Strings.isNullOrEmpty(databaseShardingStrategy.getShardingAlgorithmName())) {
            result.add(databaseShardingStrategy.getShardingAlgorithmName());
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    private String buildProps(final Properties props) {
        return null == props ? "" : PropertiesConverter.convert(props);
    }
    
    @Override
    public Class<ShowUnusedShardingAlgorithmsStatement> getType() {
        return ShowUnusedShardingAlgorithmsStatement.class;
    }
}
