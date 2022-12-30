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

package org.apache.shardingsphere.shadow.distsql.update;

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.CreateShadowRuleStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShadowRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final CreateShadowRuleStatementUpdater updater = new CreateShadowRuleStatementUpdater();
    
    @Before
    public void before() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getName()).thenReturn("aa");
        when(currentConfig.getDataSources()).thenReturn(Collections.singletonList(new ShadowDataSourceConfiguration("initRuleName", "initDs0", "initDs0Shadow")));
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleName() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(false, ruleSegment, ruleSegment), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleNameInMetaData() {
        when(currentConfig.getDataSources()).thenReturn(Collections.singletonList(new ShadowDataSourceConfiguration("ruleName", "ds", "ds_shadow")));
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(false, ruleSegment), currentConfig);
    }
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertExecuteWithNotExistResource() {
        when(resourceMetaData.getNotExistedResources(any())).thenReturn(Arrays.asList("ds0", "ds1"));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(false, new ShadowRuleSegment("ruleName", "ds1", null, null));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteDuplicateAlgorithm() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", createProperties()));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(false, new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteDuplicateAlgorithmWithoutConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", createProperties()));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(false, new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(database, sqlStatement, null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertInvalidAlgorithmConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("type", createProperties()));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(false, new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test
    public void assertExecuteWithIfNotExists() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("SIMPLE_HINT", createProperties()));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(true, new ShadowRuleSegment("initRuleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    private CreateShadowRuleStatement createSQLStatement(final boolean ifNotExists, final ShadowRuleSegment... ruleSegments) {
        return new CreateShadowRuleStatement(ifNotExists, Arrays.asList(ruleSegments));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("type", "value");
        return result;
    }
}
