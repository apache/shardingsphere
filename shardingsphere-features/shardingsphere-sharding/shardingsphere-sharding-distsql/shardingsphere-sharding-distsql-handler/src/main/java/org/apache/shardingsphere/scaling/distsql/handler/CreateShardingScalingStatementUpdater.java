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
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.handler.converter.ShardingScalingStatementConverter;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingConfigurationSegment;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Create sharding scaling statement updater.
 */
public final class CreateShardingScalingStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingScalingStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateShardingScalingStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkDuplicate(schemaName, sqlStatement, currentRuleConfig);
        checkAlgorithms(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private void checkDuplicate(final String schemaName, final CreateShardingScalingStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (currentRuleConfig.getScaling().containsKey(sqlStatement.getScalingName())) {
            throw new DuplicateRuleException("Scaling", schemaName, Arrays.asList(sqlStatement.getScalingName()));
        }
    }
    
    private void checkAlgorithms(final CreateShardingScalingStatement sqlStatement) throws DistSQLException {
        if (null == sqlStatement.getConfigurationSegment()) {
            return;
        }
        checkRateLimiterExist(sqlStatement.getConfigurationSegment());
        checkStreamChannelExist(sqlStatement.getConfigurationSegment());
        checkCompletionDetectorExist(sqlStatement.getConfigurationSegment());
        checkDataConsistencyCheckerExist(sqlStatement.getConfigurationSegment());
    }
    
    private void checkRateLimiterExist(final ShardingScalingConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getInputSegment()) {
            checkRateLimiterAlgorithm(segment.getInputSegment().getRateLimiter());
        }
        if (null != segment.getOutputSegment()) {
            checkRateLimiterAlgorithm(segment.getOutputSegment().getRateLimiter());
        }
    }
    
    private void checkRateLimiterAlgorithm(final AlgorithmSegment rateLimiter) throws DistSQLException {
        checkAlgorithm(JobRateLimitAlgorithm.class, "rate limiter", rateLimiter);
    }
    
    private void checkStreamChannelExist(final ShardingScalingConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getStreamChannel()) {
            checkAlgorithm(PipelineChannelFactory.class, "stream channel", segment.getStreamChannel());
        }
    }
    
    private void checkCompletionDetectorExist(final ShardingScalingConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getCompletionDetector()) {
            checkAlgorithm(JobCompletionDetectAlgorithm.class, "completion detector", segment.getCompletionDetector());
        }
    }
    
    private void checkDataConsistencyCheckerExist(final ShardingScalingConfigurationSegment segment) throws DistSQLException {
        if (null != segment.getDataConsistencyChecker()) {
            checkAlgorithm(DataConsistencyCheckAlgorithm.class, "data consistency checker", segment.getDataConsistencyChecker());
        }
    }
    
    private <T extends TypedSPI> void checkAlgorithm(final Class<T> typedSPIClass, final String algorithmType, final AlgorithmSegment segment) throws DistSQLException {
        Optional<T> service = TypedSPIRegistry.findRegisteredService(typedSPIClass, segment.getName(), new Properties());
        if (!service.isPresent()) {
            throw new InvalidAlgorithmConfigurationException(algorithmType, segment.getName());
        }
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingScalingStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, OnRuleAlteredActionConfiguration> scalingConfigurationMap = new HashMap<>(1, 1);
        scalingConfigurationMap.put(sqlStatement.getScalingName(), buildScalingConfiguration(sqlStatement.getConfigurationSegment()));
        result.setScaling(scalingConfigurationMap);
        return result;
    }
    
    private OnRuleAlteredActionConfiguration buildScalingConfiguration(final ShardingScalingConfigurationSegment segment) {
        if (null == segment) {
            return buildNullScalingConfiguration();
        }
        return ShardingScalingStatementConverter.convert(segment);
    }
    
    private OnRuleAlteredActionConfiguration buildNullScalingConfiguration() {
        return null;
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
        return CreateShardingScalingStatement.class.getCanonicalName();
    }
}
