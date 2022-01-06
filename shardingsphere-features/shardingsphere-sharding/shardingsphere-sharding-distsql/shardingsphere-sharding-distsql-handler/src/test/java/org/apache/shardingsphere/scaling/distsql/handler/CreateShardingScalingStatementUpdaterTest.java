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

package org.apache.shardingsphere.scaling.distsql.handler;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingScalingStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final CreateShardingScalingStatementUpdater updater = new CreateShardingScalingStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutShardingRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckWithExist() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getScaling().put("default_scaling", null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), currentRuleConfig);
    }
    
    @Test
    public void assertCheckSuccess() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), currentRuleConfig);
    }
    
    @Test
    public void assertBuildToBeCreatedRuleConfiguration() {
        ShardingRuleConfiguration result = updater.buildToBeCreatedRuleConfiguration(createSQLStatement("default_scaling"));
        assertThat(result.getScaling().size(), is(1));
        assertThat(result.getScaling().keySet().iterator().next(), is("default_scaling"));
    }
    
    @Test
    public void assertUpdateSuccess() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(createSQLStatement("default_scaling"));
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        assertThat(currentRuleConfig.getScaling().size(), is(1));
        assertThat(currentRuleConfig.getScaling().keySet().iterator().next(), is("default_scaling"));
        assertThat(currentRuleConfig.getScalingName(), is("default_scaling"));
    }
    
    private CreateShardingScalingStatement createSQLStatement(final String scalingName) {
        return new CreateShardingScalingStatement(scalingName);
    }
}
