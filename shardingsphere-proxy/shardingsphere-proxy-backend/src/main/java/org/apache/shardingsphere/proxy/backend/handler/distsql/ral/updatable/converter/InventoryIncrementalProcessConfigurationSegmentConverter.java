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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.InventoryIncrementalProcessConfigurationSegment;
import org.apache.shardingsphere.distsql.parser.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineReadConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineWriteConfiguration;

/**
 * Inventory incremental process configuration segment converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryIncrementalProcessConfigurationSegmentConverter {
    
    /**
     * Convert to pipeline process configuration.
     *
     * @param segment process configuration segment
     * @return pipeline process configuration
     */
    public static PipelineProcessConfiguration convert(final InventoryIncrementalProcessConfigurationSegment segment) {
        PipelineReadConfiguration readConfig = convertToReadConfiguration(segment.getReadSegment());
        PipelineWriteConfiguration writeConfig = convertToWriteConfiguration(segment.getWriteSegment());
        AlgorithmConfiguration streamChannel = convertToAlgorithm(segment.getStreamChannel());
        return new PipelineProcessConfiguration(readConfig, writeConfig, streamChannel);
    }
    
    private static PipelineReadConfiguration convertToReadConfiguration(final ReadOrWriteSegment readSegment) {
        if (null == readSegment) {
            return null;
        }
        return new PipelineReadConfiguration(readSegment.getWorkerThread(), readSegment.getBatchSize(), readSegment.getShardingSize(), convertToAlgorithm(readSegment.getRateLimiter()));
    }
    
    private static PipelineWriteConfiguration convertToWriteConfiguration(final ReadOrWriteSegment writeSegment) {
        if (null == writeSegment) {
            return null;
        }
        return new PipelineWriteConfiguration(writeSegment.getWorkerThread(), writeSegment.getBatchSize(), convertToAlgorithm(writeSegment.getRateLimiter()));
    }
    
    private static AlgorithmConfiguration convertToAlgorithm(final AlgorithmSegment segment) {
        if (null == segment) {
            return null;
        }
        return new AlgorithmConfiguration(segment.getName(), segment.getProps());
    }
}
