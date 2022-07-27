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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Show authority rule handler.
 */
public final class ShowAuthorityRuleHandler extends QueryableRALBackendHandler<ShowAuthorityRuleStatement> {
    
    private static final String USERS = "users";
    
    private static final String PROVIDER = "provider";
    
    private static final String PROPS = "props";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(USERS, PROVIDER, PROPS);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        AuthorityRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        return Collections.singleton(getRow(rule.getConfiguration()));
    }
    
    private LocalDataQueryResultRow getRow(final AuthorityRuleConfiguration authorityRuleConfig) {
        return new LocalDataQueryResultRow(authorityRuleConfig.getUsers().stream().map(each -> each.getGrantee().toString()).collect(Collectors.joining("; ")),
                authorityRuleConfig.getProvider().getType(), authorityRuleConfig.getProvider().getProps().size() == 0 ? "" : authorityRuleConfig.getProvider().getProps());
    }
}
