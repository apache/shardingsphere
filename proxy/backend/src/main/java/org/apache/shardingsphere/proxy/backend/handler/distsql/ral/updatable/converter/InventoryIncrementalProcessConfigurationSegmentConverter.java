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
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineWriteConfiguration;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.InventoryIncrementalProcessConfigurationSegment;
import org.apache.shardingsphere.distsql.parser.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;

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
        return null == readSegment
                ? null
                : new PipelineReadConfiguration(readSegment.getWorkerThread(), readSegment.getBatchSize(), readSegment.getShardingSize(), convertToAlgorithm(readSegment.getRateLimiter()));
    }
    
    private static PipelineWriteConfiguration convertToWriteConfiguration(final ReadOrWriteSegment writeSegment) {
        return null == writeSegment ? null : new PipelineWriteConfiguration(writeSegment.getWorkerThread(), writeSegment.getBatchSize(), convertToAlgorithm(writeSegment.getRateLimiter()));
    }
    
    private static AlgorithmConfiguration convertToAlgorithm(final AlgorithmSegment segment) {
        return null == segment ? null : new AlgorithmConfiguration(segment.getName(), segment.getProps());
    }
}
