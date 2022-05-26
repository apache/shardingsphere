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

import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreatorFactory;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithmFactory;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.scaling.distsql.handler.converter.ShardingScalingRuleStatementConverter;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingRuleConfigurationSegment;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;

import java.util.Collections;
import java.util.Properties;

/**
 * Create sharding scaling rule statement updater.
 */
public final class CreateShardingScalingRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingScalingRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final CreateShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkDuplicate(databaseName, sqlStatement, currentRuleConfig);
        checkAlgorithms(sqlStatement.getScalingRuleConfigSegment());
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", databaseName);
        }
    }
    
    private void checkDuplicate(final String databaseName, final CreateShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (currentRuleConfig.getScaling().containsKey(sqlStatement.getScalingName())) {
            throw new DuplicateRuleException("Scaling", databaseName, Collections.singletonList(sqlStatement.getScalingName()));
        }
    }
    
    private void checkAlgorithms(final ShardingScalingRuleConfigurationSegment segment) throws DistSQLException {
        if (null == segment) {
            return;
        }
        checkRateLimiterExist(segment);
        checkStreamChannelExist(segment);
        checkCompletionDetectorExist(segment);
        checkDataConsistencyCalculatorExist(segment);
    }
    
    private void checkRateLimiterExist(final ShardingScalingRuleConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getInputSegment()) {
            checkRateLimitAlgorithm(segment.getInputSegment().getRateLimiter());
        }
        if (null != segment.getOutputSegment()) {
            checkRateLimitAlgorithm(segment.getOutputSegment().getRateLimiter());
        }
    }
    
    private void checkRateLimitAlgorithm(final AlgorithmSegment rateLimit) throws DistSQLException {
        if (null != rateLimit && !JobRateLimitAlgorithmFactory.contains(rateLimit.getName())) {
            throw new InvalidAlgorithmConfigurationException("rate limit", rateLimit.getName());
        }
    }
    
    private void checkStreamChannelExist(final ShardingScalingRuleConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getStreamChannel() && !PipelineChannelCreatorFactory.contains(segment.getStreamChannel().getName())) {
            throw new InvalidAlgorithmConfigurationException("stream channel", segment.getStreamChannel().getName());
        }
    }
    
    private void checkCompletionDetectorExist(final ShardingScalingRuleConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getCompletionDetector() && !JobCompletionDetectAlgorithmFactory.contains(segment.getCompletionDetector().getName())) {
            throw new InvalidAlgorithmConfigurationException("completion detector", segment.getCompletionDetector().getName());
        }
    }
    
    private void checkDataConsistencyCalculatorExist(final ShardingScalingRuleConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getDataConsistencyCalculator()) {
            try {
                DataConsistencyCalculateAlgorithmFactory.newInstance(segment.getDataConsistencyCalculator().getName(), new Properties());
            } catch (final ServiceProviderNotFoundException ex) {
                throw new InvalidAlgorithmConfigurationException("data consistency calculator", segment.getDataConsistencyCalculator().getName());
            }
        }
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingScalingRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setScaling(Collections.singletonMap(sqlStatement.getScalingName(), buildScalingConfiguration(sqlStatement.getScalingRuleConfigSegment())));
        return result;
    }
    
    private OnRuleAlteredActionConfiguration buildScalingConfiguration(final ShardingScalingRuleConfigurationSegment segment) {
        return null == segment ? null : ShardingScalingRuleStatementConverter.convert(segment);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getScaling().putAll(toBeCreatedRuleConfig.getScaling());
        if (null == currentRuleConfig.getScalingName()) {
            currentRuleConfig.setScalingName(toBeCreatedRuleConfig.getScaling().keySet().iterator().next());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingScalingRuleStatement.class.getName();
    }
}
