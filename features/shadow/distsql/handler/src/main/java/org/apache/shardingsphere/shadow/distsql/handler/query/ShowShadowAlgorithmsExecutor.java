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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Show shadow algorithms executor.
 */
@Setter
public final class ShowShadowAlgorithmsExecutor implements DistSQLQueryExecutor<ShowShadowAlgorithmsStatement>, DistSQLExecutorRuleAware<ShadowRule> {
    
    private ShadowRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowShadowAlgorithmsStatement sqlStatement) {
        return Arrays.asList("shadow_algorithm_name", "type", "props", "is_default");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShadowAlgorithmsStatement sqlStatement, final ContextManager contextManager) {
        String defaultAlgorithm = rule.getConfiguration().getDefaultShadowAlgorithmName();
        return rule.getConfiguration().getShadowAlgorithms().entrySet().stream()
                .map(entry -> new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), entry.getValue().getProps(), entry.getKey().equals(defaultAlgorithm)))
                .collect(Collectors.toList());
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<ShowShadowAlgorithmsStatement> getType() {
        return ShowShadowAlgorithmsStatement.class;
    }
}
