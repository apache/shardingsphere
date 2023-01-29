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

package org.apache.shardingsphere.authority.checker;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class AuthorityCheckerTest {
    
    @Test
    public void assertCheckIsAuthorizedDatabase() {
        Collection<ShardingSphereUser> users = Collections.singleton(new ShardingSphereUser("root", "", "localhost"));
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(users, new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        assertTrue(new AuthorityChecker(new AuthorityRule(ruleConfig, Collections.emptyMap()), new Grantee("root", "localhost")).isAuthorized("db0"));
    }
    
    @Test
    public void assertCheck() {
        Collection<ShardingSphereUser> users = Collections.singleton(new ShardingSphereUser("root", "", "localhost"));
        AuthorityRule rule = new AuthorityRule(new AuthorityRuleConfiguration(users, new AlgorithmConfiguration("ALL_PERMITTED", new Properties())), Collections.emptyMap());
        AuthorityChecker authorityChecker = new AuthorityChecker(rule, new Grantee("root", "localhost"));
        authorityChecker.isAuthorized(mock(SelectStatementContext.class), null);
        authorityChecker.isAuthorized(mock(InsertStatementContext.class), null);
        authorityChecker.isAuthorized(mock(CreateTableStatementContext.class), null);
    }
}
