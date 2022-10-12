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
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.AlterShadowRuleStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShadowRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final AlterShadowRuleStatementUpdater updater = new AlterShadowRuleStatementUpdater();
    
    @Before
    public void before() {
        Map<String, ShadowDataSourceConfiguration> map = new HashMap<>();
        map.put("initRuleName1", new ShadowDataSourceConfiguration("ds1", "ds_shadow1"));
        map.put("initRuleName2", new ShadowDataSourceConfiguration("ds2", "ds_shadow2"));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(currentConfig.getDataSources()).thenReturn(map);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteWithoutCurrentConfiguration() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(ruleSegment, ruleSegment), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleName() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(ruleSegment, ruleSegment), currentConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithRuleNameNotInMetaData() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(ruleSegment), currentConfig);
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertExecuteWithNotExistResource() {
        List<String> dataSources = Arrays.asList("ds", "ds0");
        when(resourceMetaData.getNotExistedResources(any())).thenReturn(dataSources);
        AlterShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("initRuleName1", "ds3", null, null));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertExecuteDuplicateAlgorithm() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", prop));
        AlterShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertExecuteDuplicateAlgorithmWithoutConfiguration() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", prop));
        AlterShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    @Test
    public void assertExecuteSuccess() {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment1 = new ShadowAlgorithmSegment("algorithmName1", new AlgorithmSegment("name", prop));
        ShadowAlgorithmSegment segment2 = new ShadowAlgorithmSegment("algorithmName2", new AlgorithmSegment("name", prop));
        AlterShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment1))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment2))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
    
    private AlterShadowRuleStatement createSQLStatement(final ShadowRuleSegment... ruleSegments) {
        return new AlterShadowRuleStatement(Arrays.asList(ruleSegments));
    }
}
