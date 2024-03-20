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

package org.apache.shardingsphere.broadcast.distsql.handler.update;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.statement.DropBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropBroadcastTableRuleExecutorTest {
    
    private final DropBroadcastTableRuleExecutor executor = new DropBroadcastTableRuleExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("sharding_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedRule() {
        DropBroadcastTableRuleStatement sqlStatement = new DropBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getConfiguration()).thenReturn(new BroadcastRuleConfiguration(Collections.emptyList()));
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        BroadcastRuleConfiguration config = new BroadcastRuleConfiguration(new LinkedList<>());
        config.getTables().add("t_address");
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getConfiguration()).thenReturn(config);
        executor.setRule(rule);
        DropBroadcastTableRuleStatement sqlStatement = new DropBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        BroadcastRuleConfiguration toBeDroppedConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertTrue(toBeDroppedConfig.getTables().isEmpty());
    }
}
