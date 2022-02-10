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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSchemaRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Result set for count schema rules.
 */
public final class SchemaRulesCountResultSet implements DistSQLResultSet {
    
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
    
    private Iterator<Collection<Object>> data;
    
    static {
        FEATURE_MAP.put(SHARDING, ShardingRuleConfiguration.class);
        FEATURE_MAP.put(READWRITE_SPLITTING, ReadwriteSplittingRuleConfiguration.class);
        FEATURE_MAP.put(DB_DISCOVERY, DatabaseDiscoveryRuleConfiguration.class);
        FEATURE_MAP.put(ENCRYPT, EncryptRuleConfiguration.class);
        FEATURE_MAP.put(SHADOW, ShadowRuleConfiguration.class);
    }
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Map<String, Collection<Object>> dataMap = new LinkedHashMap<>();
        addSingleTableData(dataMap, metaData.getRuleMetaData().findRules(SingleTableRule.class));
        if (hasRuleConfiguration(metaData)) {
            addConfigurationData(dataMap, metaData.getRuleMetaData().getConfigurations());
        } else {
            addDefaultData(dataMap);
        }
        data = dataMap.values().iterator();
    }
    
    private void addSingleTableData(final Map<String, Collection<Object>> dataMap, final Collection<SingleTableRule> rules) {
        Optional<Integer> count = rules.stream().map(each -> (Collection) each.export(ExportableConstants.EXPORTABLE_KEY_SINGLE_TABLES).orElse(Collections.emptyMap()))
                .map(Collection::size).reduce(Integer::sum);
        dataMap.putIfAbsent(SINGLE_TABLE, buildRow(SINGLE_TABLE, TABLE, count.orElse(DEFAULT_COUNT)));
    }
    
    private boolean hasRuleConfiguration(final ShardingSphereMetaData metaData) {
        Collection<RuleConfiguration> configurations = metaData.getRuleMetaData().getConfigurations();
        return null != configurations && !configurations.isEmpty();
    }
    
    private void addDefaultData(final Map<String, Collection<Object>> dataMap) {
        addShardingData(dataMap, null);
        addReadwriteSplittingData(dataMap, null);
        addDBDiscoveryData(dataMap, null);
        addEncryptData(dataMap, null);
        addShadowData(dataMap, null);
    }
    
    private void addConfigurationData(final Map<String, Collection<Object>> dataMap, final Collection<RuleConfiguration> configurations) {
        configurations.forEach(each -> {
            addShardingData(dataMap, each);
            addReadwriteSplittingData(dataMap, each);
            addDBDiscoveryData(dataMap, each);
            addEncryptData(dataMap, each);
            addShadowData(dataMap, each);
        });
    }
    
    private void addShardingData(final Map<String, Collection<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, String.join("_", SHARDING, SHARDING_TABLE), SHARDING, SHARDING_TABLE, ruleConfiguration,
            config -> ((ShardingRuleConfiguration) config).getTables().size() + ((ShardingRuleConfiguration) config).getAutoTables().size());
        addData(dataMap, String.join("_", SHARDING, BINDING_TABLE), SHARDING, BINDING_TABLE, ruleConfiguration, config -> ((ShardingRuleConfiguration) config).getBindingTableGroups().size());
        addData(dataMap, String.join("_", SHARDING, BROADCAST_TABLE), SHARDING, BROADCAST_TABLE, ruleConfiguration, config -> ((ShardingRuleConfiguration) config).getBroadcastTables().size());
    }
    
    private void addReadwriteSplittingData(final Map<String, Collection<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, READWRITE_SPLITTING, DATA_SOURCE, ruleConfiguration, config -> ((ReadwriteSplittingRuleConfiguration) config).getDataSources().size());
    }
    
    private void addDBDiscoveryData(final Map<String, Collection<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, DB_DISCOVERY, DATA_SOURCE, ruleConfiguration, config -> ((DatabaseDiscoveryRuleConfiguration) config).getDataSources().size());
    }
    
    private void addEncryptData(final Map<String, Collection<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, ENCRYPT, TABLE, ruleConfiguration, config -> ((EncryptRuleConfiguration) config).getTables().size());
    }
    
    private void addShadowData(final Map<String, Collection<Object>> dataMap, final RuleConfiguration ruleConfiguration) {
        addData(dataMap, SHADOW, DATA_SOURCE, ruleConfiguration, config -> ((ShadowRuleConfiguration) config).getDataSources().size());
    }
    
    private void addData(final Map<String, Collection<Object>> dataMap, final String feature, final String type,
                         final RuleConfiguration ruleConfiguration, final Function<RuleConfiguration, Integer> apply) {
        addData(dataMap, feature, feature, type, ruleConfiguration, apply);
    }
    
    private void addData(final Map<String, Collection<Object>> dataMap, final String dataKey, final String feature, final String type,
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
        dataMap.put(dataKey, buildRow(feature, type, apply.apply(ruleConfiguration)));
    }
    
    private Collection<Object> buildRow(final String type, final String name, final Integer count) {
        return Arrays.asList(type, name, count);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("feature", "type", "count");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return CountSchemaRulesStatement.class.getName();
    }
}
