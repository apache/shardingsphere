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

package org.apache.shardingsphere.data.pipeline.core.spi.ingest.channel;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.AckCallback;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MultiplexMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.SimpleMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;

import java.util.Properties;

/**
 * Memory implementation of pipeline channel factory.
 */
@Getter
@Setter
public final class MemoryPipelineChannelFactory implements PipelineChannelFactory {
    
    public static final String TYPE = "MEMORY";
    
    private static final String BLOCK_QUEUE_SIZE_KEY = "block-queue-size";
    
    private int blockQueueSize = 10000;
    
    private Properties props = new Properties();
    
    @Override
    public void init() {
        String blockQueueSizeValue = props.getProperty(BLOCK_QUEUE_SIZE_KEY);
        if (!Strings.isNullOrEmpty(blockQueueSizeValue)) {
            blockQueueSize = Integer.parseInt(blockQueueSizeValue);
        }
    }
    
    @Override
    public PipelineChannel createPipelineChannel(final int outputConcurrency, final AckCallback ackCallback) {
        if (1 == outputConcurrency) {
            return new SimpleMemoryPipelineChannel(blockQueueSize, ackCallback);
        }
        return new MultiplexMemoryPipelineChannel(outputConcurrency, blockQueueSize, ackCallback);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
