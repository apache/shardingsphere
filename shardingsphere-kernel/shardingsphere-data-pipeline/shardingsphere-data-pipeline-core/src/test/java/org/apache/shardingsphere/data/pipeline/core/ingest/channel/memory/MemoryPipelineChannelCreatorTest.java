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

package org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory;

import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MemoryPipelineChannelCreatorTest {

    @Test
    public void assertInit() throws Exception {
        Properties result = new Properties();
        result.setProperty("block-queue-size", "200");
        MemoryPipelineChannelCreator memoryPipelineChannelCreator = new MemoryPipelineChannelCreator();
        memoryPipelineChannelCreator.init(result);
        assertThat(memoryPipelineChannelCreator.getProps(), is(result));
        Integer blockQueueSize = ReflectionUtil.getFieldValue(memoryPipelineChannelCreator, "blockQueueSize", Integer.class);
        assertThat(blockQueueSize, is(200));
    }

    @Test
    public void assertCreatePipelineChannel() {
        MemoryPipelineChannelCreator memoryPipelineChannelCreator = new MemoryPipelineChannelCreator();
        PipelineChannel pipelineChannel = memoryPipelineChannelCreator.createPipelineChannel(1, records -> { });
        assertThat(pipelineChannel, instanceOf(SimpleMemoryPipelineChannel.class));
        PipelineChannel pipelineChannelMultiplex = memoryPipelineChannelCreator.createPipelineChannel(2, records -> { });
        assertThat(pipelineChannelMultiplex, instanceOf(MultiplexMemoryPipelineChannel.class));
    }
}
