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
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class MemoryPipelineChannelCreatorTest {
    
    @Test
    public void assertInitWithBlockQueueSize() throws Exception {
        PipelineChannelCreator creator = new MemoryPipelineChannelCreator();
        creator.init(PropertiesBuilder.build(new Property("block-queue-size", "200")));
        assertThat(Plugins.getMemberAccessor().get(MemoryPipelineChannelCreator.class.getDeclaredField("blockQueueSize"), creator), is(200));
    }
    
    @Test
    public void assertInitWithoutBlockQueueSize() throws Exception {
        PipelineChannelCreator creator = new MemoryPipelineChannelCreator();
        creator.init(new Properties());
        assertThat(Plugins.getMemberAccessor().get(MemoryPipelineChannelCreator.class.getDeclaredField("blockQueueSize"), creator), is(10000));
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
