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

import org.apache.shardingsphere.data.pipeline.api.ingest.channel.AckCallback;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class MemoryPipelineChannelCreatorTest {
    
    @Test
    public void assertInitWithBlockQueueSize() throws Exception {
        Properties props = new Properties();
        props.setProperty("block-queue-size", "200");
        PipelineChannelCreator creator = new MemoryPipelineChannelCreator();
        creator.init(props);
        assertThat(ReflectionUtil.getFieldValue(creator, "blockQueueSize", Integer.class), is(200));
    }
    
    @Test
    public void assertInitWithoutBlockQueueSize() throws Exception {
        PipelineChannelCreator creator = new MemoryPipelineChannelCreator();
        creator.init(new Properties());
        assertThat(ReflectionUtil.getFieldValue(creator, "blockQueueSize", Integer.class), is(10000));
    }
    
    @Test
    public void assertCreateSimpleMemoryPipelineChannel() {
        assertThat(new MemoryPipelineChannelCreator().createPipelineChannel(1, mock(AckCallback.class)), instanceOf(SimpleMemoryPipelineChannel.class));
    }
    
    @Test
    public void assertCreateMultiplexMemoryPipelineChannel() {
        assertThat(new MemoryPipelineChannelCreator().createPipelineChannel(2, mock(AckCallback.class)), instanceOf(MultiplexMemoryPipelineChannel.class));
    }
}
