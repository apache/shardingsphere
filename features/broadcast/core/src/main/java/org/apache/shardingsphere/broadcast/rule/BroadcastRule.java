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

package org.apache.shardingsphere.broadcast.rule;

import lombok.Getter;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datasource.DataSourceMapperContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datanode.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datanode.DataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.table.TableMapperContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.table.TableMapperRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Broadcast rule.
 */
@Getter
public final class BroadcastRule implements DatabaseRule, DataNodeContainedRule, TableMapperContainedRule {
    
    private final BroadcastRuleConfiguration configuration;
    
    private final String databaseName;
    
    private final Collection<String> tables;
    
    private final Collection<String> dataSourceNames;
    
    private final DataNodeRule dataNodeRule;
    
    private final TableMapperRule tableMapperRule;
    
    public BroadcastRule(final BroadcastRuleConfiguration config, final String databaseName, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules) {
        configuration = config;
        this.databaseName = databaseName;
        dataSourceNames = getAggregatedDataSourceNames(dataSources, builtRules);
        tables = createBroadcastTables(config.getTables());
        dataNodeRule = new BroadcastDataNodeRule(dataSourceNames, tables);
        tableMapperRule = new BroadcastTableMapperRule(tables);
    }
    
    private Collection<String> getAggregatedDataSourceNames(final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules) {
        Collection<String> result = new LinkedList<>(dataSources.keySet());
        for (ShardingSphereRule each : builtRules) {
            if (each instanceof DataSourceMapperContainedRule) {
                result = getAggregatedDataSourceNames(result, (DataSourceMapperContainedRule) each);
            }
        }
        return result;
    }
    
    private Collection<String> getAggregatedDataSourceNames(final Collection<String> dataSourceNames, final DataSourceMapperContainedRule builtRule) {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, Collection<String>> entry : builtRule.getDataSourceMapperRule().getDataSourceMapper().entrySet()) {
            for (String each : entry.getValue()) {
                if (dataSourceNames.contains(each)) {
                    dataSourceNames.remove(each);
                    if (!result.contains(entry.getKey())) {
                        result.add(entry.getKey());
                    }
                }
            }
        }
        result.addAll(dataSourceNames);
        return result;
    }
    
    private Collection<String> createBroadcastTables(final Collection<String> broadcastTables) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(broadcastTables);
        return result;
    }
    
    /**
     * Get broadcast rule table names.
     * 
     * @param logicTableNames logic table names
     * @return broadcast rule table names.
     */
    public Collection<String> getBroadcastRuleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(tables::contains).collect(Collectors.toSet());
    }
    
    /**
     * Judge whether logic table is all broadcast tables or not.
     *
     * @param logicTableNames logic table names
     * @return whether logic table is all broadcast tables or not
     */
    public boolean isAllBroadcastTables(final Collection<String> logicTableNames) {
        return !logicTableNames.isEmpty() && tables.containsAll(logicTableNames);
    }
}
