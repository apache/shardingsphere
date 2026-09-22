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

package org.apache.shardingsphere.sharding.checker.config;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sharding rule configuration checker.
 */
public final class ShardingRuleConfigurationChecker implements DatabaseRuleConfigurationChecker<ShardingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ShardingRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        ruleConfig.getTables().forEach(this::checkDataNodeSchemas);
    }
    
    private void checkDataNodeSchemas(final ShardingTableRuleConfiguration tableRuleConfig) {
        Collection<String> actualDataNodes = InlineExpressionParserFactory.newInstance(tableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        Map<String, String> schemaNamesByDataSource = new HashMap<>(actualDataNodes.size(), 1F);
        for (String each : actualDataNodes) {
            DataNode dataNode = new DataNode(each);
            if (null == dataNode.getSchemaName()) {
                continue;
            }
            String configuredSchemaName = schemaNamesByDataSource.putIfAbsent(dataNode.getDataSourceName(), dataNode.getSchemaName());
            ShardingSpherePreconditions.checkState(null == configuredSchemaName || configuredSchemaName.equalsIgnoreCase(dataNode.getSchemaName()),
                    () -> new InvalidRuleConfigurationException("sharding table", Collections.singleton(tableRuleConfig.getLogicTable()),
                            Collections.singleton(String.format("Multiple schemas are configured for storage unit '%s'.", dataNode.getDataSourceName()))));
        }
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        ruleConfig.getTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        ruleConfig.getAutoTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<String> actualDataNodes = InlineExpressionParserFactory.newInstance(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        return new HashSet<>(InlineExpressionParserFactory.newInstance(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate());
    }
    
    @Override
    public Collection<String> getTableNames(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        result.addAll(ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
}
