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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.CountInstanceRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Count instance rules handler.
 */
public final class CountInstanceRulesHandler extends QueryableRALBackendHandler<CountInstanceRulesStatement> {
    
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
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) throws SQLException {
        Map<String, List<Object>> dataMap = new LinkedHashMap<>();
        ProxyContext.getInstance().getAllDatabaseNames().forEach(each -> addDatabaseData(dataMap, ProxyContext.getInstance().getDatabase(each)));
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (List<Object> each : dataMap.values()) {
            result.add(new LocalDataQueryResultRow(each));
        }
        return result;
    }
    
    private void addDatabaseData(final Map<String, List<Object>> dataMap, final ShardingSphereDatabase database) {
        initData(dataMap);
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof SingleTableRule) {
                addSingleTableData(dataMap, (SingleTableRule) each);
            } else if (each instanceof ShardingRule) {
                Optional<ShardingRuleConfiguration> shardingRuleConfig = database.getRuleMetaData().findSingleRuleConfiguration(ShardingRuleConfiguration.class);
                Preconditions.checkState(shardingRuleConfig.isPresent());
                addShardingData(dataMap, (ShardingRule) each, shardingRuleConfig.get());
            } else if (each instanceof ReadwriteSplittingRule) {
                addReadwriteSplittingData(dataMap, (ReadwriteSplittingRule) each);
            } else if (each instanceof DatabaseDiscoveryRule) {
                addDBDiscoveryData(dataMap, (DatabaseDiscoveryRule) each);
            } else if (each instanceof EncryptRule) {
                addEncryptData(dataMap, (EncryptRule) each);
            } else if (each instanceof ShadowRule) {
                addShadowData(dataMap, (ShadowRule) each);
            }
        }
    }
    
    private void initData(final Map<String, List<Object>> dataMap) {
        for (String each : Arrays.asList(SINGLE_TABLE, SHARDING_TABLE, SHARDING_BINDING_TABLE, SHARDING_BROADCAST_TABLE, SHARDING_SCALING, READWRITE_SPLITTING, DB_DISCOVERY, ENCRYPT, SHADOW)) {
            dataMap.putIfAbsent(each, buildRow(each, DEFAULT_COUNT));
        }
    }
    
    private void addSingleTableData(final Map<String, List<Object>> dataMap, final SingleTableRule rule) {
        dataMap.compute(SINGLE_TABLE, (key, value) -> buildRow(value, SINGLE_TABLE, rule.getAllTables().size()));
    }
    
    private void addShardingData(final Map<String, List<Object>> dataMap, final ShardingRule rule, final ShardingRuleConfiguration ruleConfig) {
        addData(dataMap, SHARDING_TABLE, () -> rule.getTables().size());
        addData(dataMap, SHARDING_BINDING_TABLE, () -> rule.getBindingTableRules().size());
        addData(dataMap, SHARDING_BROADCAST_TABLE, () -> rule.getBroadcastTables().size());
        addData(dataMap, SHARDING_SCALING, () -> ruleConfig.getScaling().size());
    }
    
    private void addReadwriteSplittingData(final Map<String, List<Object>> dataMap, final ReadwriteSplittingRule rule) {
        addData(dataMap, READWRITE_SPLITTING, () -> rule.getDataSourceMapper().size());
    }
    
    private void addDBDiscoveryData(final Map<String, List<Object>> dataMap, final DatabaseDiscoveryRule rule) {
        addData(dataMap, DB_DISCOVERY, () -> rule.getDataSourceMapper().size());
    }
    
    private void addEncryptData(final Map<String, List<Object>> dataMap, final EncryptRule rule) {
        addData(dataMap, ENCRYPT, () -> rule.getTables().size());
    }
    
    private void addShadowData(final Map<String, List<Object>> dataMap, final ShadowRule rule) {
        addData(dataMap, SHADOW, () -> rule.getDataSourceMapper().size());
    }
    
    private void addData(final Map<String, List<Object>> dataMap, final String dataKey, final Supplier<Integer> apply) {
        dataMap.compute(dataKey, (key, value) -> buildRow(value, dataKey, apply.get()));
    }
    
    private List<Object> buildRow(final List<Object> value, final String ruleName, final int count) {
        if (null == value) {
            return Arrays.asList(ruleName, count);
        }
        Integer oldCount = (Integer) new LinkedList<>(value).getLast();
        return Arrays.asList(ruleName, Integer.sum(oldCount, count));
    }
    
    private List<Object> buildRow(final String ruleName, final int count) {
        return Arrays.asList(ruleName, count);
    }
}
