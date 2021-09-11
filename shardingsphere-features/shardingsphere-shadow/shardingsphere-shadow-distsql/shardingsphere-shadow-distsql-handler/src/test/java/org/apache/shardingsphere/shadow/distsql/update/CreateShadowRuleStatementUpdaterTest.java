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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.CreateShadowRuleStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    @Mock
    private ShadowRuleConfiguration currentConfiguration;
    
    private final CreateShadowRuleStatementUpdater updater = new CreateShadowRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
        when(currentConfiguration.getDataSources()).thenReturn(Collections.singletonMap("initRuleName", null));
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleName() throws DistSQLException {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment, ruleSegment), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateRuleNameInMetaData() throws DistSQLException {
        when(currentConfiguration.getDataSources()).thenReturn(Collections.singletonMap("ruleName", null));
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment), currentConfiguration);
    }
    
    @Test(expected = ResourceInUsedException.class)
    public void assertExecuteWithDuplicateResource() throws DistSQLException {
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, null),
                new ShadowRuleSegment("ruleName1", "ds", null, null));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfiguration);
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertExecuteWithDuplicateResourceInMetaData() throws DistSQLException {
        List<String> dataSources = Arrays.asList("ds0", "ds1");
        when(resource.getNotExistedResources(any())).thenReturn(dataSources);
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds3", null, null));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfiguration);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteDuplicateTable() throws DistSQLException {
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.emptyList())),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order", Collections.emptyList())));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfiguration);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteDuplicateTableInMetaData() throws DistSQLException {
        when(currentConfiguration.getTables()).thenReturn(Collections.singletonMap("t_order", null));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.emptyList())));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfiguration);
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertExecuteDuplicateAlgorithm() throws DistSQLException {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", prop));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfiguration);
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertExecuteDuplicateAlgorithmWithoutConfiguration() throws DistSQLException {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", prop));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment))));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, null);
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertExecuteDuplicateAlgorithmInMetaData() throws DistSQLException {
        Properties prop = new Properties();
        prop.setProperty("type", "value");
        when(currentConfiguration.getShadowAlgorithms()).thenReturn(Collections.singletonMap("algorithmName", new ShardingSphereAlgorithmConfiguration("type", prop)));
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("type", prop));
        CreateShadowRuleStatement sqlStatement = createSQLStatement(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))));
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentConfiguration);
    }
    
    private CreateShadowRuleStatement createSQLStatement(final ShadowRuleSegment... ruleSegments) {
        return new CreateShadowRuleStatement(Arrays.asList(ruleSegments));
    }
}
