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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetDefaultSingleTableStorageUnitExecutorTest {
    
    private final SetDefaultSingleTableStorageUnitExecutor executor = new SetDefaultSingleTableStorageUnitExecutor();
    
    @Test
    void assertCheckWithInvalidResource() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(new SetDefaultSingleTableStorageUnitStatement("bar_ds")));
    }
    
    @Test
    void assertBuild() {
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration());
        executor.setRule(rule);
        SingleRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement("foo_ds"));
        assertTrue(toBeCreatedRuleConfig.getDefaultDataSource().isPresent());
        assertThat(toBeCreatedRuleConfig.getDefaultDataSource().get(), is("foo_ds"));
    }
    
    @Test
    void assertRandom() {
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration();
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        executor.buildToBeCreatedRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement(null));
        assertFalse(currentConfig.getDefaultDataSource().isPresent());
    }
}
