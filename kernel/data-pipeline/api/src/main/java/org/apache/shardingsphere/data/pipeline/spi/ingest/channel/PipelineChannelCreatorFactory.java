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

package org.apache.shardingsphere.data.pipeline.spi.ingest.channel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Pipeline channel creator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineChannelCreatorFactory {
    
    static {
        ShardingSphereServiceLoader.register(PipelineChannelCreator.class);
    }
    
    /**
     * Create new instance of pipeline channel creator.
     *
     * @param pipelineChannelCreatorConfig pipeline channel creator configuration
     * @return created instance
     */
    public static PipelineChannelCreator newInstance(final AlgorithmConfiguration pipelineChannelCreatorConfig) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(pipelineChannelCreatorConfig, PipelineChannelCreator.class);
    }
    
    /**
     * Judge whether contains pipeline channel creator.
     *
     * @param pipelineChannelCreatorType pipeline channel creator type
     * @return contains pipeline channel creator or not
     */
    public static boolean contains(final String pipelineChannelCreatorType) {
        return TypedSPIRegistry.findRegisteredService(PipelineChannelCreator.class, pipelineChannelCreatorType).isPresent();
    }
}
