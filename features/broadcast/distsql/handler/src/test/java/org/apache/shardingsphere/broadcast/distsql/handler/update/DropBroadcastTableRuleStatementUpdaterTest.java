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
import org.apache.shardingsphere.broadcast.distsql.parser.statement.DropBroadcastTableRuleStatement;
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

class DropBroadcastTableRuleStatementUpdaterTest {
    
    private ShardingSphereDatabase database;
    
    private final DropBroadcastTableRuleStatementUpdater updater = new DropBroadcastTableRuleStatementUpdater();
    
    @BeforeEach
    void setUp() {
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("sharding_db");
    }
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() {
        DropBroadcastTableRuleStatement statement = new DropBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, statement, null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedRule() {
        DropBroadcastTableRuleStatement statement = new DropBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, statement, new BroadcastRuleConfiguration(Collections.emptyList())));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        BroadcastRuleConfiguration configuration = new BroadcastRuleConfiguration(new LinkedList<>());
        configuration.getTables().add("t_address");
        DropBroadcastTableRuleStatement statement = new DropBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        assertTrue(updater.updateCurrentRuleConfiguration(statement, configuration));
        assertTrue(configuration.getTables().isEmpty());
    }
}
