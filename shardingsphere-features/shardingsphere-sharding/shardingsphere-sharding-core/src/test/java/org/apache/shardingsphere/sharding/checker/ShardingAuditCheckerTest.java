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

package org.apache.shardingsphere.sharding.checker;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.check.SQLCheckResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.checker.audit.ShardingAuditChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingAuditCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommonSQLStatementContext<?> sqlStatementContext;
    
    @Mock
    private Grantee grantee;
    
    @Mock
    private ShardingAuditStrategyConfiguration auditStrategy;
    
    private final ShardingAuditChecker checker = new ShardingAuditChecker();
    
    private final Map<String, ShardingSphereDatabase> databases = new LinkedHashMap<>();
    
    private final List<Object> parameters = Collections.emptyList();
    
    @Before
    public void setUp() {
        when(sqlStatementContext.getSqlHintExtractor().findDisableAuditNames()).thenReturn(new HashSet<>(Collections.singletonList("auditor_1")));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("foo_table"));
        TableRule tableRule = mock(TableRule.class);
        when(rule.findTableRule("foo_table")).thenReturn(Optional.of(tableRule));
        when(rule.getAuditStrategyConfiguration(tableRule)).thenReturn(auditStrategy);
        when(auditStrategy.getAuditorNames()).thenReturn(Collections.singleton("auditor_1"));
        databases.put("foo_db", mock(ShardingSphereDatabase.class));
    }
    
    @Test
    public void assertCheckSQLStatementPass() {
        when(rule.getAuditors().get("auditor_1").check(sqlStatementContext, parameters, grantee, databases.get("foo_db"))).thenReturn(new SQLCheckResult(true, ""));
        asserCheckResult(checker.check(sqlStatementContext, Collections.emptyList(), grantee, "foo_db", databases, rule), true, "");
        verify(rule.getAuditors().get("auditor_1"), times(1)).check(sqlStatementContext, parameters, grantee, databases.get("foo_db"));
    }
    
    @Test
    public void assertSQCheckPassByDisableAuditNames() {
        when(rule.getAuditors().get("auditor_1").check(sqlStatementContext, parameters, grantee, databases.get("foo_db"))).thenReturn(new SQLCheckResult(false, ""));
        when(auditStrategy.isAllowHintDisable()).thenReturn(true);
        asserCheckResult(checker.check(sqlStatementContext, Collections.emptyList(), grantee, "foo_db", databases, rule), true, "");
        verify(rule.getAuditors().get("auditor_1"), times(0)).check(sqlStatementContext, parameters, grantee, databases.get("foo_db"));
    }
    
    @Test
    public void assertSQLCheckNotPass() {
        when(rule.getAuditors().get("auditor_1").check(sqlStatementContext, parameters, grantee, databases.get("foo_db")))
                .thenReturn(new SQLCheckResult(false, "Not allow DML operation without sharding conditions"));
        asserCheckResult(checker.check(sqlStatementContext, Collections.emptyList(), grantee, "foo_db", databases, rule), false, "Not allow DML operation without sharding conditions");
        verify(rule.getAuditors().get("auditor_1"), times(1)).check(sqlStatementContext, parameters, grantee, databases.get("foo_db"));
    }
    
    private void asserCheckResult(final SQLCheckResult checkResult, final boolean isPassed, final String errorMessage) {
        assertThat(checkResult.isPassed(), is(isPassed));
        assertThat(checkResult.getErrorMessage(), is(errorMessage));
    }
}
