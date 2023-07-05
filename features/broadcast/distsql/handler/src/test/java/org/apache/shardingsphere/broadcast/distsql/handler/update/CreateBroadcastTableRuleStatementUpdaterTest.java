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
import org.apache.shardingsphere.broadcast.distsql.parser.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateBroadcastTableRuleStatementUpdaterTest {
    
    private final CreateBroadcastTableRuleStatementUpdater updater = new CreateBroadcastTableRuleStatementUpdater();
    
    @Test
    void assertCheckSQLStatementWithDuplicateBroadcastRule() {
        BroadcastRuleConfiguration currentConfiguration = mock(BroadcastRuleConfiguration.class);
        when(currentConfiguration.getTables()).thenReturn(Collections.singleton("t_address"));
        CreateBroadcastTableRuleStatement statement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(mock(ShardingSphereDatabase.class), statement, currentConfiguration));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfiguration() {
        BroadcastRuleConfiguration currentConfig = new BroadcastRuleConfiguration(new LinkedList<>());
        CreateBroadcastTableRuleStatement statement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        updater.checkSQLStatement(mock(ShardingSphereDatabase.class), statement, currentConfig);
        BroadcastRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentConfig, statement);
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertThat(currentConfig.getTables().size(), is(1));
        assertThat(currentConfig.getTables().iterator().next(), is("t_address"));
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
    }
}
