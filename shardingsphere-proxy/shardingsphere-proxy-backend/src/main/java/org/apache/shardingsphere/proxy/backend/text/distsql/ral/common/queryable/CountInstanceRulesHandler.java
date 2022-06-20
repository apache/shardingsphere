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
import java.util.Map;
import java.util.function.Supplier;

/**
 * Count instance rules handler.
 */
public final class CountInstanceRulesHandler extends QueryableRALBackendHandler<CountInstanceRulesStatement> {
    
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
        Map<String, LocalDataQueryResultRow> result = initRows();
        ProxyContext.getInstance().getAllDatabaseNames().forEach(each -> addDatabaseData(result, ProxyContext.getInstance().getDatabase(each)));
        return result.values();
    }
    
    private Map<String, LocalDataQueryResultRow> initRows() {
        Map<String, LocalDataQueryResultRow> result = new LinkedHashMap<>();
        for (String each : Arrays.asList(SINGLE_TABLE, SHARDING_TABLE, SHARDING_BINDING_TABLE, SHARDING_BROADCAST_TABLE, SHARDING_SCALING, READWRITE_SPLITTING, DB_DISCOVERY, ENCRYPT, SHADOW)) {
            result.put(each, new LocalDataQueryResultRow(each, 0));
        }
        return result;
    }
    
    private void addDatabaseData(final Map<String, LocalDataQueryResultRow> rowMap, final ShardingSphereDatabase database) {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof SingleTableRule) {
                addSingleTableData(rowMap, (SingleTableRule) each);
            } else if (each instanceof ShardingRule) {
                addShardingData(rowMap, (ShardingRule) each);
            } else if (each instanceof ReadwriteSplittingRule) {
                addReadwriteSplittingData(rowMap, (ReadwriteSplittingRule) each);
            } else if (each instanceof DatabaseDiscoveryRule) {
                addDBDiscoveryData(rowMap, (DatabaseDiscoveryRule) each);
            } else if (each instanceof EncryptRule) {
                addEncryptData(rowMap, (EncryptRule) each);
            } else if (each instanceof ShadowRule) {
                addShadowData(rowMap, (ShadowRule) each);
            }
        }
    }
    
    private void addSingleTableData(final Map<String, LocalDataQueryResultRow> rowMap, final SingleTableRule rule) {
        rowMap.compute(SINGLE_TABLE, (key, value) -> buildRow(value, SINGLE_TABLE, rule.getAllTables().size()));
    }
    
    private void addShardingData(final Map<String, LocalDataQueryResultRow> rowMap, final ShardingRule rule) {
        addData(rowMap, SHARDING_TABLE, () -> rule.getTables().size());
        addData(rowMap, SHARDING_BINDING_TABLE, () -> rule.getBindingTableRules().size());
        addData(rowMap, SHARDING_BROADCAST_TABLE, () -> rule.getBroadcastTables().size());
        addData(rowMap, SHARDING_SCALING, () -> ((ShardingRuleConfiguration) rule.getConfiguration()).getScaling().size());
    }
    
    private void addReadwriteSplittingData(final Map<String, LocalDataQueryResultRow> rowMap, final ReadwriteSplittingRule rule) {
        addData(rowMap, READWRITE_SPLITTING, () -> rule.getDataSourceMapper().size());
    }
    
    private void addDBDiscoveryData(final Map<String, LocalDataQueryResultRow> rowMap, final DatabaseDiscoveryRule rule) {
        addData(rowMap, DB_DISCOVERY, () -> rule.getDataSourceMapper().size());
    }
    
    private void addEncryptData(final Map<String, LocalDataQueryResultRow> rowMap, final EncryptRule rule) {
        addData(rowMap, ENCRYPT, () -> rule.getTables().size());
    }
    
    private void addShadowData(final Map<String, LocalDataQueryResultRow> rowMap, final ShadowRule rule) {
        addData(rowMap, SHADOW, () -> rule.getDataSourceMapper().size());
    }
    
    private void addData(final Map<String, LocalDataQueryResultRow> rowMap, final String dataKey, final Supplier<Integer> apply) {
        rowMap.compute(dataKey, (key, value) -> buildRow(value, dataKey, apply.get()));
    }
    
    private LocalDataQueryResultRow buildRow(final LocalDataQueryResultRow value, final String ruleName, final int count) {
        return null == value ? new LocalDataQueryResultRow(ruleName, count) : new LocalDataQueryResultRow(ruleName, Integer.sum((Integer) value.getCell(2), count));
    }
}
