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

package org.apache.shardingsphere.scaling.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.scaling.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingRuleConfigurationSegment;

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
        InputConfiguration inputConfig = convertToInputConfiguration(segment.getInputSegment());
        OutputConfiguration outputConfig = convertToOutputConfiguration(segment.getOutputSegment());
        ShardingSphereAlgorithmConfiguration streamChannel = convertToAlgorithm(segment.getStreamChannel());
        ShardingSphereAlgorithmConfiguration completionDetector = convertToAlgorithm(segment.getCompletionDetector());
        ShardingSphereAlgorithmConfiguration dataConsistencyChecker = convertToAlgorithm(segment.getDataConsistencyCalculator());
        return new OnRuleAlteredActionConfiguration(inputConfig, outputConfig, streamChannel, completionDetector, dataConsistencyChecker);
    }
    
    private static InputConfiguration convertToInputConfiguration(final InputOrOutputSegment inputSegment) {
        if (null == inputSegment) {
            return null;
        }
        return new InputConfiguration(inputSegment.getWorkerThread(), inputSegment.getBatchSize(), inputSegment.getShardingSize(), convertToAlgorithm(inputSegment.getRateLimiter()));
    }
    
    private static OutputConfiguration convertToOutputConfiguration(final InputOrOutputSegment outputSegment) {
        if (null == outputSegment) {
            return null;
        }
        return new OutputConfiguration(outputSegment.getWorkerThread(), outputSegment.getBatchSize(), convertToAlgorithm(outputSegment.getRateLimiter()));
    }
    
    private static ShardingSphereAlgorithmConfiguration convertToAlgorithm(final AlgorithmSegment segment) {
        if (null == segment) {
            return null;
        }
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }
}
