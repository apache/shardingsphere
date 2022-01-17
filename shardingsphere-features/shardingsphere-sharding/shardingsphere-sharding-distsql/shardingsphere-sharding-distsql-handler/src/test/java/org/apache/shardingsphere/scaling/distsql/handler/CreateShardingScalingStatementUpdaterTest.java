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

import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingConfigurationSegment;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

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
        ShardingSphereServiceLoader.register(JobRateLimitAlgorithm.class);
        ShardingSphereServiceLoader.register(PipelineChannelFactory.class);
        ShardingSphereServiceLoader.register(JobCompletionDetectAlgorithm.class);
        ShardingSphereServiceLoader.register(DataConsistencyCheckAlgorithm.class);
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
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidRateLimiter() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidRateLimiter());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidStreamChannel() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidStreamChannel());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidCompletionDetector() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
        statement.setConfigurationSegment(createConfigurationWithInvalidCompletionDetector());
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckInvalidDataConsistencyChecker() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
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
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
        statement.setConfigurationSegment(createCompleteConfiguration());
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), currentRuleConfig);
    }
    
    @Test
    public void assertBuildNullConfiguration() {
        ShardingRuleConfiguration result = updater.buildToBeCreatedRuleConfiguration(createSQLStatement("default_scaling"));
        assertThat(result.getScaling().size(), is(1));
        assertThat(result.getScaling().keySet().iterator().next(), is("default_scaling"));
    }
    
    @Test
    public void assertBuildCompleteConfiguration() {
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
        statement.setConfigurationSegment(createCompleteConfiguration());
        ShardingRuleConfiguration result = updater.buildToBeCreatedRuleConfiguration(statement);
        assertThat(result.getScaling().size(), is(1));
        String key = result.getScaling().keySet().iterator().next();
        assertThat(key, is("default_scaling"));
        OnRuleAlteredActionConfiguration value = result.getScaling().get(key);
        assertThat(value.getInput().getRateLimiter().getType(), is("QPS"));
        assertThat(value.getOutput().getRateLimiter().getType(), is("TPS"));
        assertThat(value.getStreamChannel().getType(), is("MEMORY"));
        assertThat(value.getCompletionDetector().getType(), is("IDLE"));
        assertThat(value.getDataConsistencyChecker().getType(), is("DATA_MATCH"));
    }
    
    @Test
    public void assertUpdateSuccess() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        CreateShardingScalingStatement statement = new CreateShardingScalingStatement("default_scaling");
        statement.setConfigurationSegment(createCompleteConfiguration());
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        assertThat(currentRuleConfig.getScalingName(), is("default_scaling"));
        assertThat(currentRuleConfig.getScaling().size(), is(1));
        String key = currentRuleConfig.getScaling().keySet().iterator().next();
        assertThat(key, is("default_scaling"));
        OnRuleAlteredActionConfiguration value = currentRuleConfig.getScaling().get(key);
        assertThat(value.getInput().getRateLimiter().getType(), is("QPS"));
        assertThat(value.getOutput().getRateLimiter().getType(), is("TPS"));
        assertThat(value.getStreamChannel().getType(), is("MEMORY"));
        assertThat(value.getCompletionDetector().getType(), is("IDLE"));
        assertThat(value.getDataConsistencyChecker().getType(), is("DATA_MATCH"));
    }
    
    private CreateShardingScalingStatement createSQLStatement(final String scalingName) {
        return new CreateShardingScalingStatement(scalingName);
    }
    
    private ShardingScalingConfigurationSegment createConfigurationWithInvalidRateLimiter() {
        ShardingScalingConfigurationSegment result = new ShardingScalingConfigurationSegment();
        result.setInputSegment(createInputOrOutputSegment("TPS"));
        result.setOutputSegment(createInputOrOutputSegment("INVALID"));
        return result;
    }
    
    private InputOrOutputSegment createInputOrOutputSegment(final String type) {
        return new InputOrOutputSegment(10, 1000, createAlgorithmSegment(type));
    }
    
    private ShardingScalingConfigurationSegment createConfigurationWithInvalidStreamChannel() {
        ShardingScalingConfigurationSegment result = new ShardingScalingConfigurationSegment();
        result.setStreamChannel(createAlgorithmSegment("INVALID"));
        return result;
    }
    
    private ShardingScalingConfigurationSegment createConfigurationWithInvalidCompletionDetector() {
        ShardingScalingConfigurationSegment result = new ShardingScalingConfigurationSegment();
        result.setCompletionDetector(createAlgorithmSegment("INVALID"));
        return result;
    }
    
    private ShardingScalingConfigurationSegment createConfigurationWithInvalidDataConsistencyChecker() {
        ShardingScalingConfigurationSegment result = new ShardingScalingConfigurationSegment();
        result.setDataConsistencyChecker(createAlgorithmSegment("INVALID"));
        return result;
    }
    
    private ShardingScalingConfigurationSegment createCompleteConfiguration() {
        ShardingScalingConfigurationSegment result = new ShardingScalingConfigurationSegment();
        result.setInputSegment(createInputOrOutputSegment("QPS"));
        result.setOutputSegment(createInputOrOutputSegment("TPS"));
        result.setStreamChannel(createAlgorithmSegment("MEMORY"));
        result.setCompletionDetector(createAlgorithmSegment("IDLE"));
        result.setDataConsistencyChecker(createAlgorithmSegment("DATA_MATCH"));
        return result;
    }
    
    private AlgorithmSegment createAlgorithmSegment(final String type) {
        return new AlgorithmSegment(type, new Properties());
    }
}
