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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.shadow.distsql.statement.ShowDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Show default shadow algorithm executor.
 */
@Setter
public final class ShowDefaultShadowAlgorithmExecutor implements DistSQLQueryExecutor<ShowDefaultShadowAlgorithmStatement>, DistSQLExecutorRuleAware<ShadowRule> {
    
    private ShadowRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowDefaultShadowAlgorithmStatement sqlStatement) {
        return Arrays.asList("shadow_algorithm_name", "type", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowDefaultShadowAlgorithmStatement sqlStatement, final ContextManager contextManager) {
        String defaultAlgorithm = rule.getConfiguration().getDefaultShadowAlgorithmName();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : rule.getConfiguration().getShadowAlgorithms().entrySet().stream()
                .filter(each -> each.getKey().equals(defaultAlgorithm)).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).entrySet()) {
            result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), entry.getValue().getProps()));
        }
        return result;
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<ShowDefaultShadowAlgorithmStatement> getType() {
        return ShowDefaultShadowAlgorithmStatement.class;
    }
}
