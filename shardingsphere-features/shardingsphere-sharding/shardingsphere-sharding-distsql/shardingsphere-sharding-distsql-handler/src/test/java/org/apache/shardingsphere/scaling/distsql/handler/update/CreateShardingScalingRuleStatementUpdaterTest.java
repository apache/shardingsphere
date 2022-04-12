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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingRuleConfigurationSegment;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingScalingRuleStatementUpdaterTest {
    
    private static final String LIMIT_TYPE_INPUT = "FIXTURE_INPUT";
    
    private static final String LIMIT_TYPE_OUTPUT = "FIXTURE_OUTPUT";
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final CreateShardingScalingRuleStatementUpdater updater = new CreateShardingScalingRuleStatementUpdater();
    
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
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidRateLimiter() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidRateLimiter());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidStreamChannel() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidStreamChannel());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidCompletionDetector() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidCompletionDetector());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidDataConsistencyChecker() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidDataConsistencyChecker());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test
    public void assertCheckSuccessWithoutConfiguration() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), currentRuleConfig);
    }
    
    @Test
    public void assertCheckSuccessWithCompleteConfiguration() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createCompleteConfiguration());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test
    public void assertBuildNullConfiguration() {
        ShardingRuleConfiguration result = updater.buildToBeCreatedRuleConfiguration(createSQLStatement("default_scaling"));
        assertThat(result.getScaling().size(), is(1));
        assertThat(result.getScaling().keySet().iterator().next(), is("default_scaling"));
    }
    
    @Test
    public void assertBuildCompleteConfiguration() {
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createCompleteConfiguration());
        ShardingRuleConfiguration result = updater.buildToBeCreatedRuleConfiguration(statement);
        assertThat(result.getScaling().size(), is(1));
        String key = result.getScaling().keySet().iterator().next();
        assertThat(key, is("default_scaling"));
        OnRuleAlteredActionConfiguration value = result.getScaling().get(key);
        assertThat(value.getInput().getRateLimiter().getType(), is(LIMIT_TYPE_INPUT));
        assertThat(value.getOutput().getRateLimiter().getType(), is(LIMIT_TYPE_OUTPUT));
        assertThat(value.getStreamChannel().getType(), is("MEMORY"));
        assertThat(value.getCompletionDetector().getType(), is("IDLE"));
        assertThat(value.getDataConsistencyChecker().getType(), is("DATA_MATCH"));
    }
    
    @Test
    public void assertUpdateSuccess() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingRuleStatement statement = new CreateShardingScalingRuleStatement("default_scaling");
        statement.setConfigurationSegment(createCompleteConfiguration());
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        assertThat(currentRuleConfig.getScalingName(), is("default_scaling"));
        assertThat(currentRuleConfig.getScaling().size(), is(1));
        String key = currentRuleConfig.getScaling().keySet().iterator().next();
        assertThat(key, is("default_scaling"));
        OnRuleAlteredActionConfiguration value = currentRuleConfig.getScaling().get(key);
        assertThat(value.getInput().getRateLimiter().getType(), is(LIMIT_TYPE_INPUT));
        assertThat(value.getOutput().getRateLimiter().getType(), is(LIMIT_TYPE_OUTPUT));
        assertThat(value.getStreamChannel().getType(), is("MEMORY"));
        assertThat(value.getCompletionDetector().getType(), is("IDLE"));
        assertThat(value.getDataConsistencyChecker().getType(), is("DATA_MATCH"));
        assertThat(value.getDataConsistencyChecker().getProps().getProperty("chunk-size"), is("1000"));
    }
    
    private CreateShardingScalingRuleStatement createSQLStatement(final String scalingName) {
        return new CreateShardingScalingRuleStatement(scalingName);
    }
    
    private ShardingScalingRuleConfigurationSegment createConfigurationWithInvalidRateLimiter() {
        ShardingScalingRuleConfigurationSegment result = new ShardingScalingRuleConfigurationSegment();
        result.setInputSegment(createInputOrOutputSegment(LIMIT_TYPE_OUTPUT));
        result.setOutputSegment(createInputOrOutputSegment("INVALID"));
        return result;
    }
    
    private InputOrOutputSegment createInputOrOutputSegment(final String type) {
        return new InputOrOutputSegment(10, 1000, createAlgorithmSegment(type));
    }
    
    private ShardingScalingRuleConfigurationSegment createConfigurationWithInvalidStreamChannel() {
        ShardingScalingRuleConfigurationSegment result = new ShardingScalingRuleConfigurationSegment();
        result.setStreamChannel(createAlgorithmSegment("INVALID"));
        return result;
    }
    
    private ShardingScalingRuleConfigurationSegment createConfigurationWithInvalidCompletionDetector() {
        ShardingScalingRuleConfigurationSegment result = new ShardingScalingRuleConfigurationSegment();
        result.setCompletionDetector(createAlgorithmSegment("INVALID"));
        return result;
    }
    
    private ShardingScalingRuleConfigurationSegment createConfigurationWithInvalidDataConsistencyChecker() {
        ShardingScalingRuleConfigurationSegment result = new ShardingScalingRuleConfigurationSegment();
        result.setDataConsistencyChecker(createAlgorithmSegment("INVALID"));
        return result;
    }
    
    private ShardingScalingRuleConfigurationSegment createCompleteConfiguration() {
        ShardingScalingRuleConfigurationSegment result = new ShardingScalingRuleConfigurationSegment();
        result.setInputSegment(createInputOrOutputSegment(LIMIT_TYPE_INPUT));
        result.setOutputSegment(createInputOrOutputSegment(LIMIT_TYPE_OUTPUT));
        result.setStreamChannel(createAlgorithmSegment("MEMORY"));
        result.setCompletionDetector(createAlgorithmSegment("IDLE"));
        AlgorithmSegment dataConsistencyChecker = createAlgorithmSegment("DATA_MATCH");
        dataConsistencyChecker.getProps().setProperty("chunk-size", "1000");
        result.setDataConsistencyChecker(dataConsistencyChecker);
        return result;
    }
    
    private AlgorithmSegment createAlgorithmSegment(final String type) {
        return new AlgorithmSegment(type, new Properties());
    }
}
