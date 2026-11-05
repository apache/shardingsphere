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

package org.apache.shardingsphere.data.pipeline.core.channel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTaskAckCallback;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * Incremental channel creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncrementalChannelCreator {
    
    /**
     * Create pipeline channel for incremental task.
     *
     * @param channelConfig pipeline channel configuration
     * @param progress incremental task progress
     * @return created pipeline channel
     */
    public static PipelineChannel create(final AlgorithmConfiguration channelConfig, final IncrementalTaskProgress progress) {
        return TypedSPILoader.getService(PipelineChannelCreator.class, channelConfig.getType(), channelConfig.getProps()).newInstance(5, new IncrementalTaskAckCallback(progress));
    }
}
