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

package org.apache.shardingsphere.mask.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Show mask rule executor.
 */
@Setter
public final class ShowMaskRuleExecutor implements DistSQLQueryExecutor<ShowMaskRulesStatement>, DistSQLExecutorRuleAware<MaskRule> {
    
    private MaskRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowMaskRulesStatement sqlStatement) {
        return Arrays.asList("table", "column", "algorithm_type", "algorithm_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMaskRulesStatement sqlStatement, final ContextManager contextManager) {
        return rule.getConfiguration().getTables().stream().filter(each -> null == sqlStatement.getTableName() || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, rule.getConfiguration().getMaskAlgorithms())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> buildColumnData(final MaskTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithmConfigs) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        tableRuleConfig.getColumns().forEach(each -> {
            AlgorithmConfiguration maskAlgorithmConfig = maskAlgorithmConfigs.get(each.getMaskAlgorithm());
            result.add(new LocalDataQueryResultRow(tableRuleConfig.getName(), each.getLogicColumn(), maskAlgorithmConfig.getType(), maskAlgorithmConfig.getProps()));
        });
        return result;
    }
    
    @Override
    public Class<MaskRule> getRuleClass() {
        return MaskRule.class;
    }
    
    @Override
    public Class<ShowMaskRulesStatement> getType() {
        return ShowMaskRulesStatement.class;
    }
}
