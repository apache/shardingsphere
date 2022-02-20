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
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedResourceStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Result set for show rules used resource.
 */
public final class RulesUsedResourceQueryResultSet implements DistSQLResultSet {
    
    private static final String TYPE = ShowRulesUsedResourceStatement.class.getName();
    
    private static final String SHARDING = "sharding";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
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
        List<Collection<Object>> result = new ArrayList<>();
        ShowRulesUsedResourceStatement statement = (ShowRulesUsedResourceStatement) sqlStatement;
        String resourceName = statement.getResourceName().get();
        if (hasRulesConfig(metaData) && metaData.getResource().getDataSources().keySet().contains(resourceName)) {
            getRulesConfig(metaData.getRuleMetaData().getConfigurations(), resourceName, result);
        }
        data = result.iterator();
    }
    
    private void getRulesConfig(final Collection<RuleConfiguration> ruleConfigurations, final String resourceName, final List<Collection<Object>> result) {
        ruleConfigurations.stream().forEach(each -> {
            getRulesConfigForSharding(each, result);
            getRulesConfigForReadwriteSplitting(each, resourceName, result);
            getRulesConfigForDBDiscovery(each, resourceName, result);
            getRulesConfigForEncrypt(each, result);
            getRulesConfigForShadow(each, resourceName, result);
        });
    }
    
    private void getRulesConfigForSharding(final RuleConfiguration ruleConfig, final List<Collection<Object>> result) {
        if (!matchFeature(ruleConfig, SHARDING)) {
            return;
        }
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) ruleConfig;
        config.getAutoTables().forEach(each -> {
            result.add(buildRow(SHARDING, each.getLogicTable()));
        });
        config.getTables().forEach(each -> {
            result.add(buildRow(SHARDING, each.getLogicTable()));
        });
    }
    
    private void getRulesConfigForReadwriteSplitting(final RuleConfiguration ruleConfig, final String resourceName, final List<Collection<Object>> result) {
        if (!matchFeature(ruleConfig, READWRITE_SPLITTING)) {
            return;
        }
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) ruleConfig;
        config.getDataSources().forEach(each -> {
            if (each.getWriteDataSourceName().isPresent() && each.getWriteDataSourceName().get().equalsIgnoreCase(resourceName)) {
                result.add(buildRow(READWRITE_SPLITTING, each.getName()));
            }
            if (each.getReadDataSourceNames().isPresent() && Arrays.asList(each.getReadDataSourceNames().get().split(",")).contains(resourceName)) {
                result.add(buildRow(READWRITE_SPLITTING, each.getName()));
            }
        });
    }
    
    private void getRulesConfigForDBDiscovery(final RuleConfiguration ruleConfig, final String resourceName, final List<Collection<Object>> result) {
        if (!matchFeature(ruleConfig, DB_DISCOVERY)) {
            return;
        }
        DatabaseDiscoveryRuleConfiguration config = (DatabaseDiscoveryRuleConfiguration) ruleConfig;
        config.getDataSources().forEach(each -> {
            if (each.getDataSourceNames().contains(resourceName)) {
                result.add(buildRow(DB_DISCOVERY, each.getGroupName()));
            }
        });
    }
    
    private void getRulesConfigForEncrypt(final RuleConfiguration ruleConfig, final List<Collection<Object>> result) {
        if (!matchFeature(ruleConfig, ENCRYPT)) {
            return;
        }
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) ruleConfig;
        config.getTables().forEach(each -> {
            result.add(buildRow(ENCRYPT, each.getName()));
        });
    }
    
    private void getRulesConfigForShadow(final RuleConfiguration ruleConfig, final String resourceName, final List<Collection<Object>> result) {
        if (!matchFeature(ruleConfig, SHADOW)) {
            return;
        }
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) ruleConfig;
        for (Map.Entry<String, ShadowDataSourceConfiguration> each : config.getDataSources().entrySet()) {
            if (each.getValue().getShadowDataSourceName().equalsIgnoreCase(resourceName) || each.getValue().getSourceDataSourceName().equalsIgnoreCase(resourceName)) {
                result.add(buildRow(SHADOW, each.getKey()));
            }
        }
    }
    
    private boolean matchFeature(final RuleConfiguration ruleConfig, final String feature) {
        if (null != ruleConfig && ruleConfig.getClass().getName().equals(FEATURE_MAP.get(feature).getName())) {
            return true;
        }
        return false;
    }
    
    private Collection<Object> buildRow(final String type, final String name) {
        return Arrays.asList(type, name);
    }
    
    private boolean hasRulesConfig(final ShardingSphereMetaData metaData) {
        Collection<RuleConfiguration> configurations = metaData.getRuleMetaData().getConfigurations();
        return null != configurations && !configurations.isEmpty();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "name");
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
        return TYPE;
    }
}
