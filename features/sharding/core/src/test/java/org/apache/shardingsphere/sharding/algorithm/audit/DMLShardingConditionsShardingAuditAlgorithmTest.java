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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.audit.DMLWithoutShardingKeyException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DMLShardingConditionsShardingAuditAlgorithmTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    private ShardingAuditAlgorithm shardingAuditAlgorithm;
    
    @BeforeEach
    void setUp() {
        shardingAuditAlgorithm = TypedSPILoader.getService(ShardingAuditAlgorithm.class, "DML_SHARDING_CONDITIONS");
    }
    
    @Test
    void assertCheckWithNotDMLStatement() {
        assertDoesNotThrow(() -> shardingAuditAlgorithm.check(mock(SQLStatementContext.class), Collections.emptyList(), mock(RuleMetaData.class), database));
    }
    
    @Test
    void assertCheckWithoutShardingTable() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DMLStatement.class));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mock(ShardingRule.class))));
        assertDoesNotThrow(() -> shardingAuditAlgorithm.check(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database));
    }
    
    @Test
    void assertCheckWithShardingTable() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DMLStatement.class));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.isShardingTable("foo_tbl")).thenReturn(true);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        assertThrows(DMLWithoutShardingKeyException.class, () -> shardingAuditAlgorithm.check(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database));
    }
}
