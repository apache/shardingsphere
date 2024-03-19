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
import org.apache.shardingsphere.broadcast.distsql.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateBroadcastTableRuleExecutorTest {
    
    private final CreateBroadcastTableRuleExecutor executor = new CreateBroadcastTableRuleExecutor();
    
    @Test
    void assertCheckSQLStatementWithEmptyStorageUnit() {
        BroadcastRuleConfiguration currentConfig = mock(BroadcastRuleConfiguration.class);
        when(currentConfig.getTables()).thenReturn(Collections.singleton("t_address"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        executor.setDatabase(database);
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        assertThrows(EmptyStorageUnitException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateBroadcastRule() {
        executor.setDatabase(mockShardingSphereDatabase());
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getTables()).thenReturn(Collections.singleton("t_address"));
        executor.setRule(rule);
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfiguration() {
        BroadcastRuleConfiguration currentConfig = new BroadcastRuleConfiguration(new LinkedList<>());
        executor.setDatabase(mockShardingSphereDatabase());
        BroadcastRule rule = mock(BroadcastRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        CreateBroadcastTableRuleStatement sqlStatement = new CreateBroadcastTableRuleStatement(false, Collections.singleton("t_address"));
        executor.checkBeforeUpdate(sqlStatement);
        BroadcastRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(toBeCreatedRuleConfig.getTables().size(), is(1));
        assertThat(toBeCreatedRuleConfig.getTables().iterator().next(), is("t_address"));
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        return database;
    }
}
