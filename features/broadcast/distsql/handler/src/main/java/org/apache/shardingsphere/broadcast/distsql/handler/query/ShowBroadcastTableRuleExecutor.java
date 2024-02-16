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

package org.apache.shardingsphere.broadcast.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.broadcast.distsql.statement.ShowBroadcastTableRulesStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Show broadcast table rule executor.
 */
@Setter
public final class ShowBroadcastTableRuleExecutor implements DistSQLQueryExecutor<ShowBroadcastTableRulesStatement>, DistSQLExecutorRuleAware<BroadcastRule> {
    
    private BroadcastRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowBroadcastTableRulesStatement sqlStatement) {
        return Collections.singleton("broadcast_table");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowBroadcastTableRulesStatement sqlStatement, final ContextManager contextManager) {
        return rule.getConfiguration().getTables().stream().map(LocalDataQueryResultRow::new).collect(Collectors.toList());
    }
    
    @Override
    public Class<BroadcastRule> getRuleClass() {
        return BroadcastRule.class;
    }
    
    @Override
    public Class<ShowBroadcastTableRulesStatement> getType() {
        return ShowBroadcastTableRulesStatement.class;
    }
}
