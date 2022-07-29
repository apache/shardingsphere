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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropDefaultSingleTableRuleUpdaterTest {
    
    private final DropDefaultSingleTableRuleStatementUpdater updater = new DropDefaultSingleTableRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void setUp() throws Exception {
        when(database.getName()).thenReturn("sharding_db");
        when(database.getResource().getDataSources()).thenReturn(Collections.singletonMap("ds_0", new MockedDataSource()));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutConfig() throws Exception {
        updater.checkSQLStatement(database, new DropDefaultSingleTableRuleStatement(), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutResource() throws Exception {
        updater.checkSQLStatement(database, new DropDefaultSingleTableRuleStatement(), new SingleTableRuleConfiguration());
    }
    
    @Test
    public void assertCheckWithIfExists() throws Exception {
        DropDefaultSingleTableRuleStatement statement = new DropDefaultSingleTableRuleStatement(true);
        SingleTableRuleConfiguration currentConfig = new SingleTableRuleConfiguration();
        updater.checkSQLStatement(database, statement, currentConfig);
        updater.checkSQLStatement(database, statement, null);
    }
    
    @Test
    public void assertUpdate() {
        SingleTableRuleConfiguration currentConfig = new SingleTableRuleConfiguration();
        currentConfig.setDefaultDataSource("default");
        updater.updateCurrentRuleConfiguration(new DropDefaultSingleTableRuleStatement(), currentConfig);
        assertFalse(currentConfig.getDefaultDataSource().isPresent());
    }
}
