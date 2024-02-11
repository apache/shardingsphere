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

package org.apache.shardingsphere.transaction.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.transaction.distsql.statement.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show transaction rule executor.
 */
@Setter
public final class ShowTransactionRuleExecutor implements DistSQLQueryExecutor<ShowTransactionRuleStatement>, DistSQLExecutorRuleAware<TransactionRule> {
    
    private TransactionRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowTransactionRuleStatement sqlStatement) {
        return Arrays.asList("default_type", "provider_type", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowTransactionRuleStatement sqlStatement, final ContextManager contextManager) {
        return Collections.singleton(new LocalDataQueryResultRow(rule.getDefaultType().name(), rule.getProviderType(), rule.getProps()));
    }
    
    @Override
    public Class<TransactionRule> getRuleClass() {
        return TransactionRule.class;
    }
    
    @Override
    public Class<ShowTransactionRuleStatement> getType() {
        return ShowTransactionRuleStatement.class;
    }
}
