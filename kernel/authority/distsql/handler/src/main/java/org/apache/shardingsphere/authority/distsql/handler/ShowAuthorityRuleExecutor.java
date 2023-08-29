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

package org.apache.shardingsphere.authority.distsql.handler;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.distsql.parser.statement.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.distsql.handler.ral.query.MetaDataRequiredQueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Show authority rule executor.
 */
public final class ShowAuthorityRuleExecutor implements MetaDataRequiredQueryableRALExecutor<ShowAuthorityRuleStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ShowAuthorityRuleStatement sqlStatement) {
        AuthorityRule rule = metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        AuthorityRuleConfiguration ruleConfig = rule.getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(ruleConfig.getUsers().stream().map(each -> each.getGrantee().toString()).collect(Collectors.joining("; ")),
                ruleConfig.getAuthorityProvider().getType(), ruleConfig.getAuthorityProvider().getProps().isEmpty() ? "" : ruleConfig.getAuthorityProvider().getProps()));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("users", "provider", "props");
    }
    
    @Override
    public Class<ShowAuthorityRuleStatement> getType() {
        return ShowAuthorityRuleStatement.class;
    }
}
