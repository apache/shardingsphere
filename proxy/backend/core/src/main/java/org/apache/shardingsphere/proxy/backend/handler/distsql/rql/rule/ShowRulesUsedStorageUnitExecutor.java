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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show rules used storage unit executor.
 */
public final class ShowRulesUsedStorageUnitExecutor implements RQLExecutor<ShowRulesUsedStorageUnitStatement> {
    
    private static final String SHARDING = "sharding";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    private static final String MASK = "mask";
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowRulesUsedStorageUnitStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        String resourceName = sqlStatement.getStorageUnitName().orElse(null);
        if (database.getResourceMetaData().getDataSources().containsKey(resourceName)) {
            result.addAll(getShardingData(database));
            result.addAll(getReadwriteSplittingData(database, resourceName));
            result.addAll(getEncryptData(database));
            result.addAll(getShadowData(database, resourceName));
            result.addAll(getMaskData(database));
        }
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> getShardingData(final ShardingSphereDatabase database) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.get().getConfiguration();
        for (ShardingAutoTableRuleConfiguration each : config.getAutoTables()) {
            result.add(buildRow(SHARDING, each.getLogicTable()));
        }
        for (ShardingTableRuleConfiguration each : config.getTables()) {
            result.add(buildRow(SHARDING, each.getLogicTable()));
        }
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> getReadwriteSplittingData(final ShardingSphereDatabase database, final String resourceName) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : config.getDataSources()) {
            if (each.getWriteDataSourceName().equalsIgnoreCase(resourceName)) {
                result.add(buildRow(READWRITE_SPLITTING, each.getName()));
            }
            if (each.getReadDataSourceNames().contains(resourceName)) {
                result.add(buildRow(READWRITE_SPLITTING, each.getName()));
            }
        }
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> getEncryptData(final ShardingSphereDatabase database) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        EncryptRuleConfiguration ruleConfig =
                rule.get().getConfiguration() instanceof CompatibleEncryptRuleConfiguration ? ((CompatibleEncryptRuleConfiguration) rule.get().getConfiguration()).convertToEncryptRuleConfiguration()
                        : (EncryptRuleConfiguration) rule.get().getConfiguration();
        return ruleConfig.getTables().stream().map(each -> buildRow(ENCRYPT, each.getName())).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> getShadowData(final ShardingSphereDatabase database, final String resourceName) {
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) rule.get().getConfiguration();
        return config.getDataSources().stream()
                .filter(each -> each.getShadowDataSourceName().equalsIgnoreCase(resourceName) || each.getProductionDataSourceName().equalsIgnoreCase(resourceName))
                .map(each -> buildRow(SHADOW, each.getName())).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> getMaskData(final ShardingSphereDatabase database) {
        Optional<MaskRule> rule = database.getRuleMetaData().findSingleRule(MaskRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        MaskRuleConfiguration config = (MaskRuleConfiguration) rule.get().getConfiguration();
        return config.getTables().stream().map(each -> buildRow(MASK, each.getName())).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow buildRow(final String type, final String name) {
        return new LocalDataQueryResultRow(type, name);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public Class<ShowRulesUsedStorageUnitStatement> getType() {
        return ShowRulesUsedStorageUnitStatement.class;
    }
}
