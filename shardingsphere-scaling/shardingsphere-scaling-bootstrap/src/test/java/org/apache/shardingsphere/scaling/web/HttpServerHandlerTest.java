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

package org.apache.shardingsphere.scaling.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.apache.shardingsphere.scaling.web.entity.ResponseContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HttpServerHandlerTest {
    
    private HttpServerHandler httpServerHandler;
    
    @Mock
    private ScalingJobService scalingJobService;
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        httpServerHandler = new HttpServerHandler();
        ReflectionUtil.setFieldValue(httpServerHandler, "scalingJobService", scalingJobService);
    }
    
    @Test
    public void assertStartJobSuccess() {
        when(scalingJobService.start(any(JobConfiguration.class))).thenReturn(Optional.of(new JobContext()));
        ResponseContent<?> responseContent = execute("/scaling/job/start");
        assertTrue(responseContent.isSuccess());
    }
    
    @Test
    public void assertStartJobFailure() {
        when(scalingJobService.start(any(JobConfiguration.class))).thenReturn(Optional.empty());
        ResponseContent<?> responseContent = execute("/scaling/job/start");
        assertFalse(responseContent.isSuccess());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertListJobs() {
        when(scalingJobService.listJobs()).thenReturn(mockJobContexts());
        ResponseContent<?> responseContent = execute("/scaling/job/list");
        assertThat(((List<JobContext>) responseContent.getModel()).size(), is(2));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertGetJobProgress() {
        when(scalingJobService.getProgress(1L)).thenReturn(mockJobProgress());
        ResponseContent<?> responseContent = execute("/scaling/job/progress/1");
        Map<String, String> map = (Map<String, String>) responseContent.getModel();
        assertThat(map.get("status"), is("RUNNING"));
    }
    
    @Test
    public void assertStopJob() {
        ResponseContent<?> responseContent = execute("/scaling/job/stop/1");
        assertTrue(responseContent.isSuccess());
    }
    
    @Test
    public void assertDataConsistencyCheck() {
        when(scalingJobService.check(1L)).thenReturn(mockDataConsistency());
        ResponseContent<?> responseContent = execute("/scaling/job/check/1");
        assertTrue(responseContent.isSuccess());
    }
    
    @Test
    public void assertResetTargetTable() {
        ResponseContent<?> responseContent = execute("/scaling/job/reset/1");
        assertTrue(responseContent.isSuccess());
    }
    
    @Test
    public void assertChannelReadUnsupportedUrl() {
        ResponseContent<?> responseContent = execute("/scaling/1");
        assertThat(responseContent.getErrorMsg(), is("Not support request!"));
    }
    
    @Test
    public void assertChannelReadUnsupportedMethod() {
        ResponseContent<?> responseContent = execute("/scaling/1", HttpMethod.DELETE);
        assertThat(responseContent.getErrorMsg(), is("Not support request!"));
    }
    
    @Test
    public void assertExceptionCaught() {
        Throwable throwable = mock(Throwable.class);
        httpServerHandler.exceptionCaught(channelHandlerContext, throwable);
        verify(channelHandlerContext).close();
    }
    
    private ResponseContent<?> execute(final String uri) {
        return execute(uri, HttpMethod.POST);
    }
    
    private ResponseContent<?> execute(final String uri, final HttpMethod httpMethod) {
        ByteBuf byteBuf = Unpooled.copiedBuffer("{}", CharsetUtil.UTF_8);
        FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri, byteBuf);
        httpServerHandler.channelRead0(channelHandlerContext, fullHttpRequest);
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(channelHandlerContext).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse fullHttpResponse = argumentCaptor.getValue();
        return new Gson().fromJson(fullHttpResponse.content().toString(CharsetUtil.UTF_8), ResponseContent.class);
    }
    
    private List<JobContext> mockJobContexts() {
        List<JobContext> result = Lists.newArrayList();
        result.add(new JobContext());
        result.add(new JobContext());
        return result;
    }
    
    private JobProgress mockJobProgress() {
        return new JobProgress(1, "RUNNING");
    }
    
    private Map<String, DataConsistencyCheckResult> mockDataConsistency() {
        Map<String, DataConsistencyCheckResult> result = Maps.newHashMap();
        result.put("t_order", new DataConsistencyCheckResult(1, 1));
        return result;
    }
}
