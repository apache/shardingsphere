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

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedResourceStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Query result set for show rules used resource.
 */
public final class RulesUsedResourceQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final String SHARDING = "sharding";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        List<Collection<Object>> data = new LinkedList<>();
        ShowRulesUsedResourceStatement statement = (ShowRulesUsedResourceStatement) sqlStatement;
        String resourceName = statement.getResourceName().orElse(null);
        if (database.getResources().getDataSources().containsKey(resourceName)) {
            data.addAll(getShardingData(database));
            data.addAll(getReadwriteSplittingData(database, resourceName));
            data.addAll(getDatabaseDiscoveryData(database, resourceName));
            data.addAll(getEncryptData(database));
            data.addAll(getShadowData(database, resourceName));
        }
        this.data = data.iterator();
    }
    
    private Collection<Collection<Object>> getShardingData(final ShardingSphereDatabase database) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<Collection<Object>> result = new LinkedList<>();
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.get().getConfiguration();
        for (ShardingAutoTableRuleConfiguration each : config.getAutoTables()) {
            result.add(buildRow(SHARDING, each.getLogicTable()));
        }
        for (ShardingTableRuleConfiguration each : config.getTables()) {
            result.add(buildRow(SHARDING, each.getLogicTable()));
        }
        return result;
    }
    
    private Collection<Collection<Object>> getReadwriteSplittingData(final ShardingSphereDatabase database, final String resourceName) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<Collection<Object>> result = new LinkedList<>();
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : config.getDataSources()) {
            if (null != each.getStaticStrategy()) {
                if (each.getStaticStrategy().getWriteDataSourceName().equalsIgnoreCase(resourceName)) {
                    result.add(buildRow(READWRITE_SPLITTING, each.getName()));
                }
                if (each.getStaticStrategy().getReadDataSourceNames().contains(resourceName)) {
                    result.add(buildRow(READWRITE_SPLITTING, each.getName()));
                }
            }
        }
        return result;
    }
    
    private Collection<Collection<Object>> getDatabaseDiscoveryData(final ShardingSphereDatabase database, final String resourceName) {
        Optional<DatabaseDiscoveryRule> rule = database.getRuleMetaData().findSingleRule(DatabaseDiscoveryRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        DatabaseDiscoveryRuleConfiguration config = (DatabaseDiscoveryRuleConfiguration) rule.get().getConfiguration();
        return config.getDataSources().stream().filter(each -> each.getDataSourceNames().contains(resourceName)).map(each -> buildRow(DB_DISCOVERY, each.getGroupName())).collect(Collectors.toList());
    }
    
    private Collection<Collection<Object>> getEncryptData(final ShardingSphereDatabase database) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) rule.get().getConfiguration();
        return config.getTables().stream().map(each -> buildRow(ENCRYPT, each.getName())).collect(Collectors.toList());
    }
    
    private Collection<Collection<Object>> getShadowData(final ShardingSphereDatabase database, final String resourceName) {
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) rule.get().getConfiguration();
        return config.getDataSources().entrySet().stream()
                .filter(entry -> entry.getValue().getShadowDataSourceName().equalsIgnoreCase(resourceName) || entry.getValue().getProductionDataSourceName().equalsIgnoreCase(resourceName))
                .map(entry -> buildRow(SHADOW, entry.getKey())).collect(Collectors.toList());
    }
    
    private Collection<Object> buildRow(final String type, final String name) {
        return Arrays.asList(type, name);
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
        return ShowRulesUsedResourceStatement.class.getName();
    }
}
