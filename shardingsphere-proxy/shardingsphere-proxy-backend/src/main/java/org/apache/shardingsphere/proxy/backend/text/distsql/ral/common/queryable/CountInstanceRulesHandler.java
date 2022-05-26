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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.CountInstanceRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Count instance rules handler.
 */
public final class CountInstanceRulesHandler extends QueryableRALBackendHandler<CountInstanceRulesStatement, CountInstanceRulesHandler> {
    
    private static final int DEFAULT_COUNT = 0;
    
    private static final String SINGLE_TABLE = "single_table";
    
    private static final String SHARDING_TABLE = "sharding_table";
    
    private static final String SHARDING_BINDING_TABLE = "sharding_binding_table";
    
    private static final String SHARDING_BROADCAST_TABLE = "sharding_broadcast_table";
    
    private static final String SHARDING_SCALING = "sharding_scaling";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "count");
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) throws SQLException {
        Map<String, List<Object>> dataMap = new LinkedHashMap<>();
        ProxyContext.getInstance().getAllDatabaseNames().forEach(each -> addSchemaData(dataMap, ProxyContext.getInstance().getDatabase(each)));
        return dataMap.values();
    }
    
    private void addSchemaData(final Map<String, List<Object>> dataMap, final ShardingSphereDatabase database) {
        initData(dataMap);
        Collection<SingleTableRule> singleTableRules = database.getRuleMetaData().findRules(SingleTableRule.class);
        if (!singleTableRules.isEmpty()) {
            addSingleTableData(dataMap, singleTableRules);
        }
        if (hasRuleConfiguration(database)) {
            addConfigurationData(dataMap, database.getRuleMetaData().getConfigurations());
        }
    }
    
    private void addSingleTableData(final Map<String, List<Object>> dataMap, final Collection<SingleTableRule> rules) {
        Optional<Integer> count = rules.stream().map(each -> (Collection) each.export(ExportableConstants.EXPORT_SINGLE_TABLES).orElse(Collections.emptyMap()))
                .map(Collection::size).reduce(Integer::sum);
        dataMap.compute(SINGLE_TABLE, (key, value) -> buildRow(value, SINGLE_TABLE, count.orElse(DEFAULT_COUNT)));
    }
    
    private boolean hasRuleConfiguration(final ShardingSphereDatabase database) {
        Collection<RuleConfiguration> configs = database.getRuleMetaData().getConfigurations();
        return null != configs && !configs.isEmpty();
    }
    
    private void initData(final Map<String, List<Object>> dataMap) {
        addDefaultData(dataMap, SINGLE_TABLE, SHARDING_TABLE, SHARDING_BINDING_TABLE, SHARDING_BROADCAST_TABLE, SHARDING_SCALING, READWRITE_SPLITTING, DB_DISCOVERY, ENCRYPT, SHADOW);
    }
    
    private void addConfigurationData(final Map<String, List<Object>> dataMap, final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigs.forEach(each -> {
            addShardingData(dataMap, each);
            addReadwriteSplittingData(dataMap, each);
            addDBDiscoveryData(dataMap, each);
            addEncryptData(dataMap, each);
            addShadowData(dataMap, each);
        });
    }
    
    private void addShardingData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfig) {
        if (!(ruleConfig instanceof ShardingRuleConfiguration)) {
            return;
        }
        addData(dataMap, SHARDING_TABLE, () -> ((ShardingRuleConfiguration) ruleConfig).getTables().size() + ((ShardingRuleConfiguration) ruleConfig).getAutoTables().size());
        addData(dataMap, SHARDING_BINDING_TABLE, () -> ((ShardingRuleConfiguration) ruleConfig).getBindingTableGroups().size());
        addData(dataMap, SHARDING_BROADCAST_TABLE, () -> ((ShardingRuleConfiguration) ruleConfig).getBroadcastTables().size());
        addData(dataMap, SHARDING_SCALING, () -> ((ShardingRuleConfiguration) ruleConfig).getScaling().size());
    }
    
    private void addReadwriteSplittingData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfig) {
        if (!(ruleConfig instanceof ReadwriteSplittingRuleConfiguration)) {
            return;
        }
        addData(dataMap, READWRITE_SPLITTING, () -> ((ReadwriteSplittingRuleConfiguration) ruleConfig).getDataSources().size());
    }
    
    private void addDBDiscoveryData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfig) {
        if (!(ruleConfig instanceof DatabaseDiscoveryRuleConfiguration)) {
            return;
        }
        addData(dataMap, DB_DISCOVERY, () -> ((DatabaseDiscoveryRuleConfiguration) ruleConfig).getDataSources().size());
    }
    
    private void addEncryptData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfig) {
        if (!(ruleConfig instanceof EncryptRuleConfiguration)) {
            return;
        }
        addData(dataMap, ENCRYPT, () -> ((EncryptRuleConfiguration) ruleConfig).getTables().size());
    }
    
    private void addShadowData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfig) {
        if (!(ruleConfig instanceof ShadowRuleConfiguration)) {
            return;
        }
        addData(dataMap, SHADOW, () -> ((ShadowRuleConfiguration) ruleConfig).getDataSources().size());
    }
    
    private void addData(final Map<String, List<Object>> dataMap, final String dataKey, final Supplier<Integer> apply) {
        dataMap.compute(dataKey, (key, value) -> buildRow(value, dataKey, apply.get()));
    }
    
    private void addDefaultData(final Map<String, List<Object>> dataMap, final String... dataKey) {
        for (String each : dataKey) {
            dataMap.putIfAbsent(each, buildRow(each, DEFAULT_COUNT));
        }
    }
    
    private List<Object> buildRow(final Collection<Object> value, final String ruleName, final Integer count) {
        if (value == null) {
            return Arrays.asList(ruleName, count);
        } else {
            Integer oldCount = (Integer) new LinkedList<>(value).getLast();
            return Arrays.asList(ruleName, Integer.sum(oldCount, count));
        }
    }
    
    private List<Object> buildRow(final String ruleName, final Integer count) {
        return Arrays.asList(ruleName, count);
    }
}
