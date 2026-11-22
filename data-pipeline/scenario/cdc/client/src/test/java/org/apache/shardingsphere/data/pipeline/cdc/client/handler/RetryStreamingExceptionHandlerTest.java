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

package org.apache.shardingsphere.data.pipeline.cdc.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryStreamingExceptionHandlerTest {
    
    @Mock
    private CDCClient cdcClient;
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @BeforeEach
    void setUp() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        connectionContext.getStreamingIds().add("foo_stream_id");
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.attr(ClientConnectionContext.CONTEXT_KEY).set(connectionContext);
        when(channelHandlerContext.channel()).thenReturn(channel);
    }
    
    @Test
    void assertRestartStreamingWhenRetryTimesNotExceed() {
        new RetryStreamingExceptionHandler(cdcClient, 2, 10).handleException(channelHandlerContext, new RuntimeException(""));
        verify(cdcClient, timeout(3000L)).restartStreaming("foo_stream_id");
        verify(cdcClient, never()).stopStreaming(any());
    }
    
    @Test
    void assertStopStreamingWhenRetryTimesExceed() {
        new RetryStreamingExceptionHandler(cdcClient, 0, 10).handleException(channelHandlerContext, new RuntimeException(""));
        verify(cdcClient, timeout(3000L)).stopStreaming("foo_stream_id");
        verify(cdcClient, never()).restartStreaming(any());
    }
}
