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

package org.apache.shardingsphere.sharding.auditor;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.executor.audit.exception.SQLAuditException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingSQLAuditorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommonSQLStatementContext sqlStatementContext;
    
    @Mock
    private Grantee grantee;
    
    @Mock
    private ShardingAuditStrategyConfiguration auditStrategy;
    
    @Mock
    private HintValueContext hintValueContext;
    
    private final Map<String, ShardingSphereDatabase> databases = Collections.singletonMap("foo_db", mock(ShardingSphereDatabase.class));
    
    @BeforeEach
    void setUp() {
        when(hintValueContext.findDisableAuditNames()).thenReturn(new HashSet<>(Collections.singletonList("auditor_1")));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("foo_table"));
        TableRule tableRule = mock(TableRule.class);
        when(rule.findTableRule("foo_table")).thenReturn(Optional.of(tableRule));
        when(rule.getAuditStrategyConfiguration(tableRule)).thenReturn(auditStrategy);
        when(auditStrategy.getAuditorNames()).thenReturn(Collections.singleton("auditor_1"));
    }
    
    @Test
    void assertCheckSuccess() {
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        new ShardingSQLAuditor().audit(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"), rule, hintValueContext);
        verify(rule.getAuditors().get("auditor_1")).check(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"));
    }
    
    @Test
    void assertCheckSuccessByDisableAuditNames() {
        when(auditStrategy.isAllowHintDisable()).thenReturn(true);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        new ShardingSQLAuditor().audit(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"), rule, hintValueContext);
        verify(rule.getAuditors().get("auditor_1"), times(0)).check(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"));
    }
    
    @Test
    void assertCheckFailed() {
        ShardingAuditAlgorithm auditAlgorithm = rule.getAuditors().get("auditor_1");
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        doThrow(new SQLAuditException("Not allow DML operation without sharding conditions"))
                .when(auditAlgorithm).check(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"));
        SQLAuditException ex = assertThrows(SQLAuditException.class,
                () -> new ShardingSQLAuditor().audit(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"), rule, hintValueContext));
        assertThat(ex.getMessage(), is("SQL audit failed, error message: Not allow DML operation without sharding conditions."));
        verify(rule.getAuditors().get("auditor_1")).check(sqlStatementContext, Collections.emptyList(), grantee, globalRuleMetaData, databases.get("foo_db"));
    }
}
