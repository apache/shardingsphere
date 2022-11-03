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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
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
import java.util.List;
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
        when(currentConfig.getDataSources()).thenReturn(Collections.singletonMap("initRuleName", new ShadowDataSourceConfiguration("initDs0", "initDs0Shadow")));
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleName() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(ruleSegment, ruleSegment), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleNameInMetaData() {
        when(currentConfig.getDataSources()).thenReturn(Collections.singletonMap("ruleName", null));
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(ruleSegment), currentConfig);
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertExecuteWithNotExistResource() {
        List<String> dataSources = Arrays.asList("ds0", "ds1");
        when(resourceMetaData.getNotExistedResources(any())).thenReturn(dataSources);
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds1", null, null));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteDuplicateAlgorithm() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", prop));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteDuplicateAlgorithmWithoutConfiguration() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", prop));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(database, sqlStatement, null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertInvalidAlgorithmConfiguration() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("type", prop));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    private CreateShadowRuleStatement createSQLStatement(final ShadowRuleSegment... ruleSegments) {
        return new CreateShadowRuleStatement(Arrays.asList(ruleSegments));
    }
}
