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

package org.apache.shardingsphere.data.pipeline.core.channel.memory;

import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannelAckCallback;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannelCreator;

import java.util.Properties;

/**
 * Pipeline channel creator of memory.
 */
public final class MemoryPipelineChannelCreator implements PipelineChannelCreator {
    
    private static final String BLOCK_QUEUE_SIZE_KEY = "block-queue-size";
    
    private static final String BLOCK_QUEUE_SIZE_DEFAULT_VALUE = "2000";
    
    private int queueSize;
    
    @Override
    public void init(final Properties props) {
        queueSize = Integer.parseInt(props.getProperty(BLOCK_QUEUE_SIZE_KEY, BLOCK_QUEUE_SIZE_DEFAULT_VALUE));
    }
    
    @Override
    public PipelineChannel newInstance(final int importerBatchSize, final PipelineChannelAckCallback ackCallback) {
        int queueSize = this.queueSize / importerBatchSize;
        return new MemoryPipelineChannel(queueSize, ackCallback);
    }
    
    @Override
    public String getType() {
        return "MEMORY";
    }
}
