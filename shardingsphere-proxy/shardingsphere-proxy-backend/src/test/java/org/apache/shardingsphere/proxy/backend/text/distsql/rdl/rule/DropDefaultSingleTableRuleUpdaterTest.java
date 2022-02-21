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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropDefaultSingleTableRuleUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private DropDefaultSingleTableRuleStatementUpdater updater = new DropDefaultSingleTableRuleStatementUpdater();
    
    @Before
    public void setUp() throws Exception {
        when(shardingSphereMetaData.getName()).thenReturn("sharding_db");
        when(shardingSphereMetaData.getResource().getDataSources()).thenReturn(Collections.singletonMap("ds_0", mock(DataSource.class)));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutConfig() throws Exception {
        DropDefaultSingleTableRuleStatement statement = new DropDefaultSingleTableRuleStatement();
        updater.checkSQLStatement(shardingSphereMetaData, statement, null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutResource() throws Exception {
        DropDefaultSingleTableRuleStatement statement = new DropDefaultSingleTableRuleStatement();
        SingleTableRuleConfiguration currentConfiguration = new SingleTableRuleConfiguration();
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentConfiguration);
    }
    
    @Test
    public void assertUpdate() {
        DropDefaultSingleTableRuleStatement statement = new DropDefaultSingleTableRuleStatement();
        SingleTableRuleConfiguration currentConfiguration = new SingleTableRuleConfiguration();
        currentConfiguration.setDefaultDataSource("default");
        updater.updateCurrentRuleConfiguration(statement, currentConfiguration);
        assertFalse(currentConfiguration.getDefaultDataSource().isPresent());
    }
}
