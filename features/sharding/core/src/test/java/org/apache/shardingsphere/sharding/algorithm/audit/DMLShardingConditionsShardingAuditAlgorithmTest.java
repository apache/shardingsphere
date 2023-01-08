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

package org.apache.shardingsphere.sharding.algorithm.audit;

import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.executor.check.exception.SQLCheckException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class DMLShardingConditionsShardingAuditAlgorithmTest {
    
    private SQLStatementContext sqlStatementContext;
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    private ShardingAuditAlgorithm shardingAuditAlgorithm;
    
    @Before
    public void setUp() {
        shardingAuditAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()), ShardingAuditAlgorithm.class);
        sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        rule = mock(ShardingRule.class);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
    }
    
    @Test
    public void assertNotDMLStatementCheck() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        shardingAuditAlgorithm.check(sqlStatementContext, Collections.emptyList(), mock(Grantee.class), database);
        verify(database, times(0)).getRuleMetaData();
    }
    
    @Test
    public void assertAllBroadcastTablesCheck() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DMLStatement.class));
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singletonList(rule)));
        when(rule.isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(true);
        shardingAuditAlgorithm.check(sqlStatementContext, Collections.emptyList(), mock(Grantee.class), database);
    }
    
    @Test
    public void assertNotAllShardingTablesCheck() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DMLStatement.class));
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singletonList(rule)));
        when(rule.isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(false);
        shardingAuditAlgorithm.check(sqlStatementContext, Collections.emptyList(), mock(Grantee.class), database);
    }
    
    @Test(expected = SQLCheckException.class)
    public void assertEmptyShardingConditionsCheck() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DMLStatement.class));
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singletonList(rule)));
        when(rule.isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(false);
        when(rule.isShardingTable("t_order")).thenReturn(true);
        shardingAuditAlgorithm.check(sqlStatementContext, Collections.emptyList(), mock(Grantee.class), database);
    }
}
