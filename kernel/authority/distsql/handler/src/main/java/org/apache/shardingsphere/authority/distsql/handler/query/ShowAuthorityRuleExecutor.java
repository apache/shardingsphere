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

package org.apache.shardingsphere.authority.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.authority.distsql.statement.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show authority rule executor.
 */
@Setter
public final class ShowAuthorityRuleExecutor implements DistSQLQueryExecutor<ShowAuthorityRuleStatement>, DistSQLExecutorRuleAware<AuthorityRule> {
    
    private AuthorityRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowAuthorityRuleStatement sqlStatement) {
        return Arrays.asList("users", "provider", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowAuthorityRuleStatement sqlStatement, final ContextManager contextManager) {
        String users = rule.getConfiguration().getUsers().stream().map(each -> each.getGrantee().toString()).collect(Collectors.joining("; "));
        String provider = rule.getConfiguration().getPrivilegeProvider().getType();
        Properties props = rule.getConfiguration().getPrivilegeProvider().getProps().isEmpty() ? new Properties() : rule.getConfiguration().getPrivilegeProvider().getProps();
        return Collections.singleton(new LocalDataQueryResultRow(users, provider, props));
    }
    
    @Override
    public Class<AuthorityRule> getRuleClass() {
        return AuthorityRule.class;
    }
    
    @Override
    public Class<ShowAuthorityRuleStatement> getType() {
        return ShowAuthorityRuleStatement.class;
    }
}
