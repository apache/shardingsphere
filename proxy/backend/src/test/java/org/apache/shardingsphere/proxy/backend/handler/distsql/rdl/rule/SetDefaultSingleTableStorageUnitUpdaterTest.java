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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SetDefaultSingleTableStorageUnitUpdaterTest {
    
    private final SetDefaultSingleTableStorageUnitStatementUpdater updater = new SetDefaultSingleTableStorageUnitStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private SingleRuleConfiguration currentConfig;
    
    @Before
    public void setUp() {
        when(database.getName()).thenReturn("sharding_db");
        when(database.getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("ds_0", new MockedDataSource()));
    }
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertCheckWithInvalidResource() {
        updater.checkSQLStatement(database, new SetDefaultSingleTableStorageUnitStatement("ds_1"), currentConfig);
    }
    
    @Test
    public void assertBuild() {
        SingleRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement("ds_0"));
        assertTrue(toBeCreatedRuleConfig.getDefaultDataSource().isPresent());
        assertThat(toBeCreatedRuleConfig.getDefaultDataSource().get(), is("ds_0"));
    }
    
    @Test
    public void assertUpdate() {
        SingleRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement("ds_0"));
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration();
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertTrue(currentConfig.getDefaultDataSource().isPresent());
        assertThat(currentConfig.getDefaultDataSource().get(), is("ds_0"));
    }
    
    @Test
    public void assertRandom() {
        SingleRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(new SetDefaultSingleTableStorageUnitStatement(null));
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration();
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertFalse(currentConfig.getDefaultDataSource().isPresent());
    }
}
