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

package org.apache.shardingsphere.scaling.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleEnabledException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.EnableShardingScalingRuleStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EnableShardingScalingRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final EnableShardingScalingRuleStatementUpdater updater = new EnableShardingScalingRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutShardingRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckNotExist() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getScaling().put("default_scaling", null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("new_scaling"), currentRuleConfig);
    }
    
    @Test(expected = RuleEnabledException.class)
    public void assertCheckEnabled() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        String scalingName = "default_scaling";
        currentRuleConfig.getScaling().put(scalingName, null);
        currentRuleConfig.setScalingName(scalingName);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(scalingName), currentRuleConfig);
    }

    @Test
    public void assertBuildToBeAlteredRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        String scalingName = "default_scaling";
        currentRuleConfig.getScaling().put(scalingName, null);
        ShardingRuleConfiguration result = updater.buildToBeAlteredRuleConfiguration(createSQLStatement(scalingName));
        assertNotNull(result.getScalingName());
        assertThat(result.getScalingName(), is(scalingName));
    }
    
    @Test
    public void assertUpdateSuccess() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        String scalingName = "new_scaling";
        currentRuleConfig.getScaling().put("default_scaling", null);
        currentRuleConfig.getScaling().put(scalingName, null);
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(createSQLStatement(scalingName));
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        assertThat(currentRuleConfig.getScalingName(), is("new_scaling"));
    }
    
    private EnableShardingScalingRuleStatement createSQLStatement(final String scalingName) {
        return new EnableShardingScalingRuleStatement(scalingName);
    }
}
