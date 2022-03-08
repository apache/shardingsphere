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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Count instance rules handler.
 */
public final class CountInstanceRulesHandler extends QueryableRALBackendHandler<CountInstanceRulesStatement, CountInstanceRulesHandler> {
    
    private static final int DEFAULT_COUNT = 0;
    
    private static final String SINGLE_TABLE = "single_table";
    
    private static final String SHARDING = "sharding";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    private static final String SHARDING_TABLE = "sharding_table";
    
    private static final String BINDING_TABLE = "binding_table";
    
    private static final String BROADCAST_TABLE = "broadcast_table";
    
    private static final String DATA_SOURCE = "data_source";
    
    private static final String TABLE = "table";
    
    private static final Map<String, Class<? extends RuleConfiguration>> FEATURE_MAP = new HashMap<>(5, 1);
    
    static {
        FEATURE_MAP.put(SHARDING, ShardingRuleConfiguration.class);
        FEATURE_MAP.put(READWRITE_SPLITTING, ReadwriteSplittingRuleConfiguration.class);
        FEATURE_MAP.put(DB_DISCOVERY, DatabaseDiscoveryRuleConfiguration.class);
        FEATURE_MAP.put(ENCRYPT, EncryptRuleConfiguration.class);
        FEATURE_MAP.put(SHADOW, ShadowRuleConfiguration.class);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("feature", "type", "count");
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) throws SQLException {
        Map<String, List<Object>> dataMap = new LinkedHashMap<>();
        ProxyContext.getInstance().getAllSchemaNames().forEach(each -> {
            addSchemaData(dataMap, ProxyContext.getInstance().getMetaData(each));
        });
        return dataMap.values();
    }
    
    private void addSchemaData(final Map<String, List<Object>> dataMap, final ShardingSphereMetaData metaData) {
        addSingleTableData(dataMap, metaData.getRuleMetaData().findRules(SingleTableRule.class));
        if (hasRuleConfiguration(metaData)) {
            addConfigurationData(dataMap, metaData.getRuleMetaData().getConfigurations());
        } else {
            addDefaultData(dataMap);
        }
    }
    
    private void addSingleTableData(final Map<String, List<Object>> dataMap, final Collection<SingleTableRule> rules) {
        Optional<Integer> count = rules.stream().map(each -> (Collection) each.export(ExportableConstants.EXPORTABLE_KEY_SINGLE_TABLES).orElse(Collections.emptyMap()))
                .map(Collection::size).reduce(Integer::sum);
        dataMap.compute(SINGLE_TABLE, (key, value) -> buildRow(value, SINGLE_TABLE, TABLE, count.orElse(DEFAULT_COUNT)));
    }
    
    private boolean hasRuleConfiguration(final ShardingSphereMetaData metaData) {
        Collection<RuleConfiguration> configurations = metaData.getRuleMetaData().getConfigurations();
        return null != configurations && !configurations.isEmpty();
    }
    
    private void addDefaultData(final Map<String, List<Object>> dataMap) {
        addShardingData(dataMap, null);
        addReadwriteSplittingData(dataMap, null);
        addDBDiscoveryData(dataMap, null);
        addEncryptData(dataMap, null);
        addShadowData(dataMap, null);
    }
    
    private void addConfigurationData(final Map<String, List<Object>> dataMap, final Collection<RuleConfiguration> configurations) {
        configurations.forEach(each -> {
            addShardingData(dataMap, each);
            addReadwriteSplittingData(dataMap, each);
            addDBDiscoveryData(dataMap, each);
            addEncryptData(dataMap, each);
            addShadowData(dataMap, each);
        });
    }
    
    private void addShardingData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, String.join("_", SHARDING, SHARDING_TABLE), SHARDING, SHARDING_TABLE, ruleConfiguration,
            config -> ((ShardingRuleConfiguration) config).getTables().size() + ((ShardingRuleConfiguration) config).getAutoTables().size());
        addData(dataMap, String.join("_", SHARDING, BINDING_TABLE), SHARDING, BINDING_TABLE, ruleConfiguration, config -> ((ShardingRuleConfiguration) config).getBindingTableGroups().size());
        addData(dataMap, String.join("_", SHARDING, BROADCAST_TABLE), SHARDING, BROADCAST_TABLE, ruleConfiguration, config -> ((ShardingRuleConfiguration) config).getBroadcastTables().size());
    }
    
    private void addReadwriteSplittingData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, READWRITE_SPLITTING, DATA_SOURCE, ruleConfiguration, config -> ((ReadwriteSplittingRuleConfiguration) config).getDataSources().size());
    }
    
    private void addDBDiscoveryData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, DB_DISCOVERY, DATA_SOURCE, ruleConfiguration, config -> ((DatabaseDiscoveryRuleConfiguration) config).getDataSources().size());
    }
    
    private void addEncryptData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, ENCRYPT, TABLE, ruleConfiguration, config -> ((EncryptRuleConfiguration) config).getTables().size());
    }
    
    private void addShadowData(final Map<String, List<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, SHADOW, DATA_SOURCE, ruleConfiguration, config -> ((ShadowRuleConfiguration) config).getDataSources().size());
    }
    
    private void addData(final Map<String, List<Object>> dataMap, final String feature, final String type,
                         final RuleConfiguration ruleConfiguration, final Function<RuleConfiguration, Integer> apply) {
        addData(dataMap, feature, feature, type, ruleConfiguration, apply);
    }
    
    private void addData(final Map<String, List<Object>> dataMap, final String dataKey, final String feature, final String type,
                         final RuleConfiguration ruleConfiguration, final Function<RuleConfiguration, Integer> apply) {
        if (null == ruleConfiguration) {
            dataMap.putIfAbsent(dataKey, buildRow(feature, type, DEFAULT_COUNT));
            return;
        }
        Class<? extends RuleConfiguration> clz = FEATURE_MAP.get(feature);
        if (!(ruleConfiguration.getClass().getCanonicalName().equals(clz.getCanonicalName()))) {
            dataMap.putIfAbsent(dataKey, buildRow(feature, type, DEFAULT_COUNT));
            return;
        }
        dataMap.compute(dataKey, (key, value) -> buildRow(value, feature, type, apply.apply(ruleConfiguration)));
    }
    
    private List<Object> buildRow(final Collection<Object> value, final String type, final String name, final Integer count) {
        if (value == null) {
            return Arrays.asList(type, name, count);
        } else {
            Integer oldCount = (Integer) new LinkedList<>(value).getLast();
            return Arrays.asList(type, name, Integer.sum(oldCount, count));
        }
    }
    
    private List<Object> buildRow(final String type, final String name, final Integer count) {
        return Arrays.asList(type, name, count);
    }
}
