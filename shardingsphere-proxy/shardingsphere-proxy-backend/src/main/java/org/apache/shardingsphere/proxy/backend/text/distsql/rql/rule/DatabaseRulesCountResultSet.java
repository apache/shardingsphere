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
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountDatabaseRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Result set for count database rules.
 */
public final class DatabaseRulesCountResultSet implements DistSQLResultSet {
    
    private static final String SINGLE_TABLE = "single_table";
    
    private static final String SHARDING_TABLE = "sharding_table";
    
    private static final String SHARDING_BINDING_TABLE = "sharding_binding_table";
    
    private static final String SHARDING_BROADCAST_TABLE = "sharding_broadcast_table";
    
    private static final String SHARDING_SCALING = "sharding_scaling";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Map<String, Collection<Object>> dataMap = new LinkedHashMap<>();
        initData(dataMap);
        addSingleTableData(dataMap, database.getRuleMetaData().getSingleRule(SingleTableRule.class));
        addShardingData(database, dataMap);
        addReadwriteSplittingData(database, dataMap);
        addDatabaseDiscoveryData(database, dataMap);
        addEncryptData(database, dataMap);
        addShadowData(database, dataMap);
        data = dataMap.values().iterator();
    }
    
    private void initData(final Map<String, Collection<Object>> dataMap) {
        addDefaultData(dataMap, SINGLE_TABLE, SHARDING_TABLE, SHARDING_BINDING_TABLE, SHARDING_BROADCAST_TABLE, SHARDING_SCALING, READWRITE_SPLITTING, DB_DISCOVERY, ENCRYPT, SHADOW);
    }
    
    private void addSingleTableData(final Map<String, Collection<Object>> dataMap, final SingleTableRule rule) {
        dataMap.put(SINGLE_TABLE, Arrays.asList(SINGLE_TABLE, rule.getAllTables().size()));
    }
    
    private void addShardingData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (rule.isPresent()) {
            addShardingData(dataMap, (ShardingRuleConfiguration) rule.get().getConfiguration());
        } else {
            addDefaultShardingData(dataMap);
        }
    }
    
    private void addShardingData(final Map<String, Collection<Object>> dataMap, final ShardingRuleConfiguration config) {
        addData(dataMap, SHARDING_TABLE, config.getTables().size() + config.getAutoTables().size());
        addData(dataMap, SHARDING_BINDING_TABLE, config.getBindingTableGroups().size());
        addData(dataMap, SHARDING_BROADCAST_TABLE, config.getBroadcastTables().size());
        addData(dataMap, SHARDING_SCALING, config.getScaling().size());
    }
    
    private void addDefaultShardingData(final Map<String, Collection<Object>> dataMap) {
        addData(dataMap, SHARDING_TABLE, 0);
        addData(dataMap, SHARDING_BINDING_TABLE, 0);
        addData(dataMap, SHARDING_BROADCAST_TABLE, 0);
        addData(dataMap, SHARDING_SCALING, 0);
    }
    
    private void addReadwriteSplittingData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (rule.isPresent()) {
            addData(dataMap, READWRITE_SPLITTING, ((ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration()).getDataSources().size());
        } else {
            addData(dataMap, READWRITE_SPLITTING, 0);
        }
    }
    
    private void addDatabaseDiscoveryData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<DatabaseDiscoveryRule> rule = database.getRuleMetaData().findSingleRule(DatabaseDiscoveryRule.class);
        if (rule.isPresent()) {
            addData(dataMap, DB_DISCOVERY, ((DatabaseDiscoveryRuleConfiguration) rule.get().getConfiguration()).getDataSources().size());
        } else {
            addData(dataMap, DB_DISCOVERY, 0);
        }
    }
    
    private void addEncryptData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        if (rule.isPresent()) {
            addData(dataMap, ENCRYPT, ((EncryptRuleConfiguration) rule.get().getConfiguration()).getTables().size());
        } else {
            addData(dataMap, ENCRYPT, 0);
        }
    }
    
    private void addShadowData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        if (rule.isPresent()) {
            addData(dataMap, SHADOW, ((ShadowRuleConfiguration) rule.get().getConfiguration()).getDataSources().size());
        } else {
            addData(dataMap, SHADOW, 0);
        }
    }
    
    private void addData(final Map<String, Collection<Object>> dataMap, final String dataKey, final int count) {
        dataMap.put(dataKey, Arrays.asList(dataKey, count));
    }
    
    private void addDefaultData(final Map<String, Collection<Object>> dataMap, final String... dataKey) {
        for (String each : dataKey) {
            dataMap.putIfAbsent(each, Arrays.asList(each, 0));
        }
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "count");
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
        return CountDatabaseRulesStatement.class.getName();
    }
}
