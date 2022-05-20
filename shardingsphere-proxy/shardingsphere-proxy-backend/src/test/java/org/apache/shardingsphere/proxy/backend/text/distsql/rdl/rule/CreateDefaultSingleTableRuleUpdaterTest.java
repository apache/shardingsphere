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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabase;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDefaultSingleTableRuleUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private SingleTableRuleConfiguration currentConfig;
    
    private final CreateDefaultSingleTableRuleStatementUpdater updater = new CreateDefaultSingleTableRuleStatementUpdater();
    
    @Before
    public void setUp() throws Exception {
        when(database.getName()).thenReturn("sharding_db");
        when(database.getResource().getDataSources()).thenReturn(Collections.singletonMap("ds_0", mock(DataSource.class)));
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckWithInvalidResource() throws Exception {
        CreateDefaultSingleTableRuleStatement statement = new CreateDefaultSingleTableRuleStatement("ds_1");
        updater.checkSQLStatement(database, statement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckWithDuplicateResource() throws Exception {
        when(currentConfig.getDefaultDataSource()).thenReturn(Optional.of("single_table"));
        CreateDefaultSingleTableRuleStatement statement = new CreateDefaultSingleTableRuleStatement("ds_0");
        updater.checkSQLStatement(database, statement, currentConfig);
    }
    
    @Test
    public void assertBuild() {
        CreateDefaultSingleTableRuleStatement statement = new CreateDefaultSingleTableRuleStatement("ds_0");
        SingleTableRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(statement);
        assertTrue(toBeCreatedRuleConfig.getDefaultDataSource().isPresent());
        assertThat(toBeCreatedRuleConfig.getDefaultDataSource().get(), is("ds_0"));
    }
    
    @Test
    public void assertUpdate() {
        CreateDefaultSingleTableRuleStatement statement = new CreateDefaultSingleTableRuleStatement("ds_0");
        SingleTableRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(statement);
        SingleTableRuleConfiguration currentConfig = new SingleTableRuleConfiguration();
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertTrue(currentConfig.getDefaultDataSource().isPresent());
        assertThat(currentConfig.getDefaultDataSource().get(), is("ds_0"));
    }
}
