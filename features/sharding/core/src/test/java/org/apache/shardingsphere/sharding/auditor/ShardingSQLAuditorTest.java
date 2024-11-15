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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditor;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.audit.DMLWithoutShardingKeyException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSQLAuditorTest {
    
    @Mock(extraInterfaces = TableAvailable.class, answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Mock
    private RuleMetaData globalRuleMetaData;
    
    @Mock
    private ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingRule rule;
    
    @Mock
    private ShardingAuditStrategyConfiguration auditStrategy;
    
    private ShardingSQLAuditor sqlAuditor;
    
    @BeforeEach
    void setUp() {
        when(hintValueContext.getDisableAuditNames()).thenReturn(Collections.singleton("foo_auditor"));
        when(((TableAvailable) sqlStatementContext).getTablesContext().getTableNames()).thenReturn(Collections.singletonList("foo_tbl"));
        when(((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(rule.findShardingTable("foo_tbl")).thenReturn(Optional.of(shardingTable));
        when(rule.getAuditStrategyConfiguration(shardingTable)).thenReturn(auditStrategy);
        when(auditStrategy.getAuditorNames()).thenReturn(Arrays.asList("foo_auditor", "bar_auditor"));
        sqlAuditor = (ShardingSQLAuditor) OrderedSPILoader.getServices(SQLAuditor.class, Collections.singleton(rule)).get(rule);
    }
    
    @Test
    void assertAuditSuccessWithNotTableAvailable() {
        QueryContext queryContext = mock(QueryContext.class);
        sqlAuditor.audit(queryContext, globalRuleMetaData, database, rule);
        verify(queryContext, times(0)).getHintValueContext();
    }
    
    @Test
    void assertAuditSuccessWithDisableAuditNames() {
        when(auditStrategy.isAllowHintDisable()).thenReturn(true);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, mock(ConnectionContext.class), mock(ShardingSphereMetaData.class));
        sqlAuditor.audit(queryContext, globalRuleMetaData, database, rule);
        verify(rule.getAuditors().get("foo_auditor"), times(0)).check(sqlStatementContext, Collections.emptyList(), globalRuleMetaData, database);
    }
    
    @Test
    void assertAuditSuccessWithoutDisableAuditNames() {
        when(hintValueContext.getDisableAuditNames()).thenReturn(Collections.singleton("bar_auditor"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, mock(ConnectionContext.class), mock(ShardingSphereMetaData.class));
        sqlAuditor.audit(queryContext, globalRuleMetaData, database, rule);
        verify(rule.getAuditors().get("foo_auditor")).check(sqlStatementContext, Collections.emptyList(), globalRuleMetaData, database);
    }
    
    @Test
    void assertAuditFailed() {
        ShardingAuditAlgorithm auditAlgorithm = rule.getAuditors().get("foo_auditor");
        doThrow(new DMLWithoutShardingKeyException()).when(auditAlgorithm).check(sqlStatementContext, Collections.emptyList(), globalRuleMetaData, database);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, mock(ConnectionContext.class), mock(ShardingSphereMetaData.class));
        DMLWithoutShardingKeyException ex = assertThrows(DMLWithoutShardingKeyException.class, () -> sqlAuditor.audit(queryContext, globalRuleMetaData, database, rule));
        assertThat(ex.getMessage(), is("Not allow DML operation without sharding conditions."));
    }
}
