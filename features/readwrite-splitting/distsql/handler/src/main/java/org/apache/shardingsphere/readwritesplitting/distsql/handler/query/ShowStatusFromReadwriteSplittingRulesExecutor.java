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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show status from readwrite-splitting rules executor.
 */
@Setter
public final class ShowStatusFromReadwriteSplittingRulesExecutor implements DistSQLQueryExecutor<ShowStatusFromReadwriteSplittingRulesStatement>, DistSQLExecutorRuleAware<ReadwriteSplittingRule> {
    
    private ReadwriteSplittingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowStatusFromReadwriteSplittingRulesStatement sqlStatement) {
        return Arrays.asList("name", "storage_unit", "status");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStatusFromReadwriteSplittingRulesStatement sqlStatement, final ContextManager contextManager) {
        Collection<ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules = getDataSourceGroupRules(sqlStatement);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        dataSourceGroupRules.forEach(each -> result.addAll(buildRows(each)));
        return result;
    }
    
    private Collection<ReadwriteSplittingDataSourceGroupRule> getDataSourceGroupRules(final ShowStatusFromReadwriteSplittingRulesStatement sqlStatement) {
        if (sqlStatement.getRuleName().isPresent()) {
            return rule.getDataSourceRuleGroups().values().stream().filter(each -> each.getName().equalsIgnoreCase(sqlStatement.getRuleName().get())).collect(Collectors.toList());
        }
        return rule.getDataSourceRuleGroups().values();
    }
    
    private Collection<LocalDataQueryResultRow> buildRows(final ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule) {
        return dataSourceGroupRule.getReadwriteSplittingGroup().getReadDataSources().stream()
                .map(each -> buildRow(dataSourceGroupRule.getName(), each, dataSourceGroupRule.getDisabledDataSourceNames().contains(each))).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow buildRow(final String ruleName, final String dataSourceName, final boolean disabled) {
        return new LocalDataQueryResultRow(ruleName, dataSourceName, disabled ? DataSourceState.DISABLED : DataSourceState.ENABLED);
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public Class<ShowStatusFromReadwriteSplittingRulesStatement> getType() {
        return ShowStatusFromReadwriteSplittingRulesStatement.class;
    }
}
