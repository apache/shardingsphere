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
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountDatabaseRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
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
 * Query result set for count database rules.
 */
public final class DatabaseRulesCountResultSet implements DatabaseDistSQLResultSet {
    
    private static final String SINGLE_TABLE = "single_table";
    
    private static final String SHARDING_TABLE = "sharding_table";
    
    private static final String SHARDING_BINDING_TABLE = "sharding_binding_table";
    
    private static final String SHARDING_BROADCAST_TABLE = "sharding_broadcast_table";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        // TODO Use LocalDataQueryResultRow to refactor Map<String, Collection<Object>> to Collection<LocalDataQueryResultRow>
        Map<String, Collection<Object>> dataMap = new LinkedHashMap<>();
        // TODO use ExportRule to export config
        addSingleTableData(database, dataMap);
        addShardingData(database, dataMap);
        addReadwriteSplittingData(database, dataMap);
        addDatabaseDiscoveryData(database, dataMap);
        addEncryptData(database, dataMap);
        addShadowData(database, dataMap);
        data = dataMap.values().iterator();
    }
    
    private void addSingleTableData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        SingleTableRule rule = database.getRuleMetaData().getSingleRule(SingleTableRule.class);
        dataMap.put(SINGLE_TABLE, Arrays.asList(SINGLE_TABLE, rule.getAllTables().size()));
    }
    
    private void addShardingData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        int shardingTableCount = 0;
        int shardingBindingTableCount = 0;
        int shardingBroadcastTableCount = 0;
        if (rule.isPresent()) {
            ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.get().getConfiguration();
            shardingTableCount = config.getTables().size() + config.getAutoTables().size();
            shardingBindingTableCount = config.getBindingTableGroups().size();
            shardingBroadcastTableCount = config.getBroadcastTables().size();
        }
        addData(dataMap, SHARDING_TABLE, shardingTableCount);
        addData(dataMap, SHARDING_BINDING_TABLE, shardingBindingTableCount);
        addData(dataMap, SHARDING_BROADCAST_TABLE, shardingBroadcastTableCount);
    }
    
    private void addReadwriteSplittingData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        int dataSourceCount = 0;
        if (rule.isPresent()) {
            dataSourceCount = ((ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration()).getDataSources().size();
        }
        addData(dataMap, READWRITE_SPLITTING, dataSourceCount);
    }
    
    private void addDatabaseDiscoveryData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<DatabaseDiscoveryRule> rule = database.getRuleMetaData().findSingleRule(DatabaseDiscoveryRule.class);
        int dataSourceCount = 0;
        if (rule.isPresent()) {
            dataSourceCount = ((DatabaseDiscoveryRuleConfiguration) rule.get().getConfiguration()).getDataSources().size();
        }
        addData(dataMap, DB_DISCOVERY, dataSourceCount);
    }
    
    private void addEncryptData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        int tableCount = 0;
        if (rule.isPresent()) {
            tableCount = ((EncryptRuleConfiguration) rule.get().getConfiguration()).getTables().size();
        }
        addData(dataMap, ENCRYPT, tableCount);
    }
    
    private void addShadowData(final ShardingSphereDatabase database, final Map<String, Collection<Object>> dataMap) {
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        int dataSourceCount = 0;
        if (rule.isPresent()) {
            dataSourceCount = ((ShadowRuleConfiguration) rule.get().getConfiguration()).getDataSources().size();
        }
        addData(dataMap, SHADOW, dataSourceCount);
    }
    
    private void addData(final Map<String, Collection<Object>> dataMap, final String dataKey, final int count) {
        dataMap.put(dataKey, Arrays.asList(dataKey, count));
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
