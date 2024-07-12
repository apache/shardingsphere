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

import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetDefaultSingleTableStorageUnitExecutorTest {
    
    private final SetDefaultSingleTableStorageUnitExecutor executor = new SetDefaultSingleTableStorageUnitExecutor();
    
    @Test
    void assertCheckWithInvalidDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(any())).thenReturn(Collections.emptyList());
        executor.setDatabase(database);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(new SetDefaultSingleTableStorageUnitStatement("bar_ds")));
    }
    
    @Test
    void assertCheckWithLogicDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper().keySet()).thenReturn(Collections.singleton("logic_ds"));
        when(database.getRuleMetaData().getAttributes(any())).thenReturn(Collections.singleton(ruleAttribute));
        executor.setDatabase(database);
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(new SetDefaultSingleTableStorageUnitStatement("logic_ds")));
    }
    
    @Test
    void assertUpdate() {
        executor.setRule(mock(SingleRule.class));
        SingleRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement("foo_ds"));
        assertTrue(toBeAlteredRuleConfig.getDefaultDataSource().isPresent());
        assertThat(toBeAlteredRuleConfig.getDefaultDataSource().get(), is("foo_ds"));
        assertTrue(toBeAlteredRuleConfig.getTables().isEmpty());
        assertNull(executor.buildToBeDroppedRuleConfiguration(toBeAlteredRuleConfig));
    }
    
    @Test
    void assertRandom() {
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration();
        currentConfig.setDefaultDataSource("foo_ds");
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        SingleRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement(null));
        assertFalse(toBeAlteredRuleConfig.getDefaultDataSource().isPresent());
        SingleRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(toBeAlteredRuleConfig);
        assertNotNull(toBeDroppedRuleConfig);
        assertTrue(toBeDroppedRuleConfig.getDefaultDataSource().isPresent());
        assertThat(toBeDroppedRuleConfig.getDefaultDataSource().get(), is("foo_ds"));
        assertTrue(toBeDroppedRuleConfig.getTables().isEmpty());
    }
}
