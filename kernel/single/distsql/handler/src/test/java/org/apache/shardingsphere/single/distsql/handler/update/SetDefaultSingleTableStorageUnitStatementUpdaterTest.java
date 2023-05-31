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

@ExtendWith(MockitoExtension.class)
class SetDefaultSingleTableStorageUnitStatementUpdaterTest {
    
    private final SetDefaultSingleTableStorageUnitStatementUpdater updater = new SetDefaultSingleTableStorageUnitStatementUpdater();
    
    @Test
    void assertCheckWithInvalidResource() {
        assertThrows(MissingRequiredStorageUnitsException.class,
                () -> updater.checkSQLStatement(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), new SetDefaultSingleTableStorageUnitStatement("bar_ds"), mock(SingleRuleConfiguration.class)));
    }
    
    @Test
    void assertBuild() {
        SingleRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(mock(SingleRuleConfiguration.class), new SetDefaultSingleTableStorageUnitStatement("foo_ds"));
        assertTrue(toBeCreatedRuleConfig.getDefaultDataSource().isPresent());
        assertThat(toBeCreatedRuleConfig.getDefaultDataSource().get(), is("foo_ds"));
    }
    
    @Test
    void assertUpdate() {
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration();
        SingleRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentConfig, new SetDefaultSingleTableStorageUnitStatement("foo_ds"));
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertTrue(currentConfig.getDefaultDataSource().isPresent());
        assertThat(currentConfig.getDefaultDataSource().get(), is("foo_ds"));
    }
    
    @Test
    void assertRandom() {
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration();
        SingleRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentConfig, new SetDefaultSingleTableStorageUnitStatement(null));
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertFalse(currentConfig.getDefaultDataSource().isPresent());
    }
}
