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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeMap;
import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.data.pipeline.cdc.client.constant.ClientConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.exception.ServerResultException;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ResponseFuture;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ServerErrorResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CDCRequestHandlerTest {
    
    @Mock
    private Consumer<List<Record>> consumer;
    
    @Mock
    private ExceptionHandler exceptionHandler;
    
    @Mock
    private ServerErrorResultHandler errorResultHandler;
    
    private CDCRequestHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new CDCRequestHandler(consumer, exceptionHandler, errorResultHandler);
    }
    
    @Test
    void assertChannelRegisteredAndInactive() {
        Channel channel = mockChannel(null);
        ChannelHandlerContext ctx = mockChannelHandlerContext(channel);
        handler.channelRegistered(ctx);
        assertThat(channel.attr(ClientConnectionContext.CONTEXT_KEY).get().getStatus().get(), is(ClientConnectionStatus.NOT_LOGGED_IN));
        handler.channelInactive(ctx);
        verify(ctx).fireChannelInactive();
    }
    
    @Test
    void assertHandleNonSucceedResponseWithFuture() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        ResponseFuture responseFuture = new ResponseFuture("foo_req", Type.START_STREAMING);
        connectionContext.getResponseFutureMap().put("foo_req", responseFuture);
        ChannelHandlerContext ctx = mockChannelHandlerContext(mockChannel(connectionContext));
        CDCResponse response = CDCResponse.newBuilder().setRequestId("foo_req").setStatus(Status.FAILED).setErrorCode("500").setErrorMessage("mock error").build();
        handler.channelRead(ctx, response);
        ArgumentCaptor<ServerErrorResult> resultCaptor = ArgumentCaptor.forClass(ServerErrorResult.class);
        verify(errorResultHandler).handleServerError(eq(ctx), resultCaptor.capture());
        ServerErrorResult actualResult = resultCaptor.getValue();
        assertThat(actualResult.getErrorCode(), is("500"));
        assertThat(actualResult.getErrorMessage(), is("mock error"));
        assertThat(actualResult.getRequestType(), is(Type.START_STREAMING));
        assertThat(responseFuture.getErrorCode(), is("500"));
        assertThat(responseFuture.getErrorMessage(), is("mock error"));
        ServerResultException ex = assertThrows(ServerResultException.class, () -> responseFuture.waitResponseResult(100L, connectionContext));
        assertThat(ex.getMessage(), is("Get START_STREAMING response failed, code:500, reason: mock error"));
        assertTrue(connectionContext.getResponseFutureMap().isEmpty());
    }
    
    @Test
    void assertHandleNonSucceedResponseWithoutFuture() {
        ChannelHandlerContext ctx = mockChannelHandlerContext(mockChannel(new ClientConnectionContext()));
        handler.channelRead(ctx, CDCResponse.newBuilder().setRequestId("foo_req").setStatus(Status.FAILED).setErrorCode("404").setErrorMessage("not found").build());
        ArgumentCaptor<ServerErrorResult> resultCaptor = ArgumentCaptor.forClass(ServerErrorResult.class);
        verify(errorResultHandler).handleServerError(eq(ctx), resultCaptor.capture());
        assertThat(resultCaptor.getValue().getRequestType(), is(Type.UNKNOWN));
    }
    
    @Test
    void assertHandleServerGreeting() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        ChannelHandlerContext ctx = mockChannelHandlerContext(mockChannel(connectionContext));
        CDCResponse response = CDCResponse.newBuilder().setRequestId("foo_req").setStatus(Status.SUCCEED)
                .setServerGreetingResult(ServerGreetingResult.newBuilder().setServerVersion("1.0").setProtocolVersion("1").build()).build();
        handler.channelRead(ctx, response);
        assertTrue(connectionContext.getStreamingIds().isEmpty());
        verifyNoInteractions(errorResultHandler, consumer, exceptionHandler);
    }
    
    @Test
    void assertHandleLoginResponse() {
        Channel channel = mockChannel(null);
        ChannelHandlerContext ctx = mockChannelHandlerContext(channel);
        handler.channelRegistered(ctx);
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        ResponseFuture responseFuture = new ResponseFuture("foo_req", Type.LOGIN);
        connectionContext.getResponseFutureMap().put("foo_req", responseFuture);
        handler.channelRead(ctx, CDCResponse.newBuilder().setRequestId("foo_req").setStatus(Status.SUCCEED).build());
        assertThat(connectionContext.getStatus().get(), is(ClientConnectionStatus.LOGGED_IN));
        assertDoesNotThrow(() -> responseFuture.waitResponseResult(500L, connectionContext));
    }
    
    @Test
    void assertHandleStreamDataResult() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        connectionContext.getStatus().set(ClientConnectionStatus.NOT_LOGGED_IN);
        ResponseFuture responseFuture = new ResponseFuture("foo_req", Type.STREAM_DATA);
        connectionContext.getResponseFutureMap().put("foo_req", responseFuture);
        CDCResponse response = CDCResponse.newBuilder()
                .setRequestId("foo_req").setStatus(Status.SUCCEED).setStreamDataResult(StreamDataResult.newBuilder().setStreamingId("stream_1").build()).build();
        handler.channelRead(mockChannelHandlerContext(mockChannel(connectionContext)), response);
        assertTrue(connectionContext.getStreamingIds().contains("stream_1"));
        assertThat(responseFuture.getResult(), is("stream_1"));
        assertThat(responseFuture.waitResponseResult(500L, connectionContext).toString(), is("stream_1"));
    }
    
    @Test
    void assertHandleDataRecordResult() {
        Channel channel = mockChannel(new ClientConnectionContext());
        DataRecordResult recordResult = DataRecordResult.newBuilder().setAckId("ack_1").addRecord(Record.newBuilder().build()).build();
        List<Record> expectedRecords = recordResult.getRecordList();
        handler.channelRead(mockChannelHandlerContext(channel), CDCResponse.newBuilder().setRequestId("foo_req").setStatus(Status.SUCCEED).setDataRecordResult(recordResult).build());
        verify(consumer).accept(expectedRecords);
        assertThat(expectedRecords.size(), is(1));
        ArgumentCaptor<CDCRequest> requestCaptor = ArgumentCaptor.forClass(CDCRequest.class);
        verify(channel).writeAndFlush(requestCaptor.capture());
        CDCRequest ackRequest = requestCaptor.getValue();
        assertThat(ackRequest.getType(), is(Type.ACK_STREAMING));
        assertThat(ackRequest.getAckStreamingRequestBody().getAckId(), is("ack_1"));
    }
    
    @Test
    void assertHandleSucceedWithoutPayload() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        ResponseFuture responseFuture = new ResponseFuture("foo_req", Type.DROP_STREAMING);
        connectionContext.getResponseFutureMap().put("foo_req", responseFuture);
        handler.channelRead(mockChannelHandlerContext(mockChannel(connectionContext)), CDCResponse.newBuilder().setRequestId("foo_req").setStatus(Status.SUCCEED).build());
        assertDoesNotThrow(() -> responseFuture.waitResponseResult(500L, connectionContext));
    }
    
    @Test
    void assertExceptionCaughtDelegates() {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        RuntimeException expectedCause = new RuntimeException("mock");
        handler.exceptionCaught(ctx, expectedCause);
        verify(exceptionHandler).handleException(ctx, expectedCause);
    }
    
    private Channel mockChannel(final ClientConnectionContext context) {
        Channel result = mock(Channel.class);
        AttributeMap attributeMap = new DefaultAttributeMap();
        when(result.attr(ClientConnectionContext.CONTEXT_KEY)).thenAnswer(invocation -> attributeMap.attr(invocation.getArgument(0)));
        result.attr(ClientConnectionContext.CONTEXT_KEY).set(context);
        when(result.writeAndFlush(any())).thenReturn(mock(ChannelFuture.class));
        return result;
    }
    
    private ChannelHandlerContext mockChannelHandlerContext(final Channel channel) {
        ChannelHandlerContext result = mock(ChannelHandlerContext.class);
        when(result.channel()).thenReturn(channel);
        when(result.fireChannelInactive()).thenReturn(result);
        return result;
    }
}
