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
import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Query result set for authority rule.
 */
public final class AuthorityRuleQueryResultSet implements GlobalRuleDistSQLResultSet {
    
    private static final String USERS = "users";
    
    private static final String PROVIDER = "provider";
    
    private static final String PROPS = "props";
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatement sqlStatement) {
        Optional<AuthorityRule> rule = ruleMetaData.findSingleRule(AuthorityRule.class);
        rule.ifPresent(optional -> data = buildData(optional.getConfiguration()).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final AuthorityRuleConfiguration ruleConfig) {
        Collection<Collection<Object>> result = new LinkedList<>();
        result.add(Arrays.asList(ruleConfig.getUsers().stream().map(each -> each.getGrantee().toString()).collect(Collectors.joining("; ")),
                ruleConfig.getProvider().getType(), ruleConfig.getProvider().getProps().isEmpty() ? "" : ruleConfig.getProvider().getProps()));
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(USERS, PROVIDER, PROPS);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowAuthorityRuleStatement.class.getName();
    }
}
