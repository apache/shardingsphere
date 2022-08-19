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

package org.apache.shardingsphere.migration.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineReadConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineWriteConfiguration;
import org.apache.shardingsphere.infra.config.rule.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.migration.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.migration.distsql.statement.segment.ShardingScalingRuleConfigurationSegment;

/**
 * Sharding scaling rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingScalingRuleStatementConverter {
    
    /**
     * Convert sharding scaling rule configuration segment to on rule altered action configuration.
     *
     * @param segment sharding scaling rule configuration segment
     * @return on rule altered action configuration
     */
    public static OnRuleAlteredActionConfiguration convert(final ShardingScalingRuleConfigurationSegment segment) {
        PipelineReadConfiguration inputConfig = convertToInputConfiguration(segment.getInputSegment());
        PipelineWriteConfiguration outputConfig = convertToOutputConfiguration(segment.getOutputSegment());
        AlgorithmConfiguration streamChannel = convertToAlgorithm(segment.getStreamChannel());
        AlgorithmConfiguration completionDetector = convertToAlgorithm(segment.getCompletionDetector());
        AlgorithmConfiguration dataConsistencyChecker = convertToAlgorithm(segment.getDataConsistencyCalculator());
        return new OnRuleAlteredActionConfiguration(inputConfig, outputConfig, streamChannel, completionDetector, dataConsistencyChecker);
    }
    
    private static PipelineReadConfiguration convertToInputConfiguration(final InputOrOutputSegment inputSegment) {
        if (null == inputSegment) {
            return null;
        }
        return new PipelineReadConfiguration(inputSegment.getWorkerThread(), inputSegment.getBatchSize(), inputSegment.getShardingSize(), convertToAlgorithm(inputSegment.getRateLimiter()));
    }
    
    private static PipelineWriteConfiguration convertToOutputConfiguration(final InputOrOutputSegment outputSegment) {
        if (null == outputSegment) {
            return null;
        }
        return new PipelineWriteConfiguration(outputSegment.getWorkerThread(), outputSegment.getBatchSize(), convertToAlgorithm(outputSegment.getRateLimiter()));
    }
    
    private static AlgorithmConfiguration convertToAlgorithm(final AlgorithmSegment segment) {
        if (null == segment) {
            return null;
        }
        return new AlgorithmConfiguration(segment.getName(), segment.getProps());
    }
}
