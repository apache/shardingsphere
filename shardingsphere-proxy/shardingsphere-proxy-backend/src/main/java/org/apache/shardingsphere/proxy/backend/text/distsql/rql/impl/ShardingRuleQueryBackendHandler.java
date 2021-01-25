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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Backend handler for show sharding rules.
 */
public final class ShardingRuleQueryBackendHandler extends SchemaRequiredBackendHandler<ShowRuleStatement> {
    
    private Iterator<Map<String, Object>> data;
    
    private final String schema;
    
    public ShardingRuleQueryBackendHandler(final ShowRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
        if (sqlStatement.getSchema().isPresent()) {
            schema = sqlStatement.getSchema().get().getIdentifier().getValue();
        } else {
            schema = backendConnection.getSchemaName();
        }
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowRuleStatement sqlStatement) {
        List<QueryHeader> queryHeader = getQueryHeader();
        data = loadRuleConfiguration();
        return new QueryResponseHeader(queryHeader);
    }
    
    private List<QueryHeader> getQueryHeader() {
        List<QueryHeader> result = new LinkedList();
        result.add(new QueryHeader(schema, "", "name", "name", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "actualDataNodes", "actualDataNodes", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "tableStrategy", "tableStrategy", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "databaseStrategy", "databaseStrategy", Types.BIGINT, "BIGINT", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "keyGenerateStrategy", "keyGenerateStrategy", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "bindingTable", "bindingTable", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
    }
    
    private Iterator<Map<String, Object>> loadRuleConfiguration() {
        List<Map<String, Object>> result = new LinkedList<>();
        Optional<ShardingRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schema).getRuleMetaData().getConfigurations()
                .stream().map(each -> (ShardingRuleConfiguration) each).findFirst();
        if (ruleConfig.isPresent()) {
            List<List<String>> bindingTables = ruleConfig.get().getBindingTableGroups().stream().filter(each -> null != each && !each.isEmpty()).map(each -> Arrays.asList(each.split(",")))
                    .collect(Collectors.toList());
            for (ShardingTableRuleConfiguration each : ruleConfig.get().getTables()) {
                Map<String, Object> table = new HashMap<>();
                table.put("name", each.getLogicTable());
                table.put("actualDataNodes", each.getActualDataNodes());
                table.put("tableStrategy", generateShardingStrategy(ruleConfig.get(), each.getTableShardingStrategy()));
                table.put("databaseStrategy", generateShardingStrategy(ruleConfig.get(), each.getDatabaseShardingStrategy()));
                table.put("keyGenerateStrategy", generateKeyGenerateStrategy(ruleConfig.get(), each.getKeyGenerateStrategy()));
                table.put("bindingTable", generateBindingTable(bindingTables, each.getLogicTable()));
                result.add(table);
            }
        }
        return result.iterator();
    }
    
    private String generateBindingTable(final List<List<String>> bindingTableGroups, final String tableName) {
        Set<String> bindingTable = new HashSet<>();
        for (List<String> each : bindingTableGroups) {
            if (each.contains(tableName)) {
                for (String table : each) {
                    if (!table.equals(tableName)) {
                        bindingTable.add(table);
                    }
                }
            }
        }
        return (new Gson()).toJson(bindingTable);
    }
    
    private String generateShardingStrategy(final ShardingRuleConfiguration ruleConfig, final ShardingStrategyConfiguration shardingStrategy) {
        StringBuilder result = new StringBuilder();
        if (shardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            result.append("shardingColumns:");
            result.append(((ComplexShardingStrategyConfiguration) shardingStrategy).getShardingColumns());
            result.append(" ");
        } else if (shardingStrategy instanceof StandardShardingStrategyConfiguration) {
            result.append("shardingColumn:");
            result.append(((StandardShardingStrategyConfiguration) shardingStrategy).getShardingColumn());
            result.append(" ");
        }
        result.append((new Gson()).toJson(ruleConfig.getShardingAlgorithms().get(shardingStrategy.getShardingAlgorithmName())));
        return result.toString();
    }
    
    private String generateKeyGenerateStrategy(final ShardingRuleConfiguration ruleConfig, final KeyGenerateStrategyConfiguration keyGenerateStrategy) {
        StringBuilder result = new StringBuilder();
        result.append("column:");
        result.append(keyGenerateStrategy.getColumn());
        result.append(" ");
        result.append((new Gson()).toJson(ruleConfig.getKeyGenerators().get(keyGenerateStrategy.getKeyGeneratorName())));
        return result.toString();
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Map<String, Object> table = data.next();
        return Arrays.asList(table.get("name"), table.get("actualDataNodes"), table.get("tableStrategy"), table.get("databaseStrategy"), table.get("keyGenerateStrategy"), table.get("bindingTable"));
    }
}
