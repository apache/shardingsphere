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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AuthorityRuleQueryResultSetTest {
    
    @Test
    public void assertExecute() {
        ShardingSphereRuleMetaData ruleMetaData = mockGlobalRuleMetaData();
        GlobalRuleDistSQLResultSet resultSet = new AuthorityRuleQueryResultSet();
        resultSet.init(ruleMetaData, mock(ShowAuthorityRuleStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("root@localhost"));
        assertTrue(actual.contains("ALL_PERMITTED"));
        assertTrue(actual.contains(""));
    }
    
    private ShardingSphereRuleMetaData mockGlobalRuleMetaData() {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        when(authorityRule.getConfiguration()).thenReturn(createAuthorityRuleConfiguration());
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class);
        when(result.findSingleRule(AuthorityRule.class)).thenReturn(Optional.of(authorityRule));
        return result;
    }
    
    private AuthorityRuleConfiguration createAuthorityRuleConfiguration() {
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        return new AuthorityRuleConfiguration(Collections.singleton(root), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
    }
}
