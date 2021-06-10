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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.util.ResponseContentUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Http server handler.
 */
@Slf4j
public final class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    @Override
    protected void channelRead0(final ChannelHandlerContext context, final FullHttpRequest request) {
        String requestPath = request.uri().toLowerCase();
        String requestBody = request.content().toString(CharsetUtil.UTF_8);
        log.info("Http request path: {}", requestPath);
        log.info("Http request body: {}", requestBody);
        if ("/scaling/job/start".equalsIgnoreCase(requestPath) && request.method().equals(HttpMethod.POST)) {
            startJob(context, requestBody);
            return;
        }
        if ("/scaling/job/list".equalsIgnoreCase(requestPath)) {
            listJobs(context);
            return;
        }
        if (requestPath.startsWith("/scaling/job/progress/")) {
            getJobProgress(context, requestPath);
            return;
        }
        if (requestPath.startsWith("/scaling/job/stop/")) {
            stopJob(context, requestPath);
            return;
        }
        if (requestPath.contains("/scaling/job/check/")) {
            checkJob(context, requestPath);
            return;
        }
        if (requestPath.contains("/scaling/job/reset/")) {
            resetJob(context, requestPath);
            return;
        }
        response(ResponseContentUtil.handleBadRequest("Not support request!"), context, HttpResponseStatus.BAD_REQUEST);
    }
    
    private void startJob(final ChannelHandlerContext context, final String requestBody) {
        Optional<Long> jobId = scalingAPI.start(GSON.fromJson(requestBody, JobConfiguration.class));
        if (jobId.isPresent()) {
            response(ResponseContentUtil.build(jobId.get()), context, HttpResponseStatus.OK);
            return;
        }
        response(ResponseContentUtil.handleBadRequest("Invalid scaling job config!"), context, HttpResponseStatus.BAD_REQUEST);
    }
    
    private void listJobs(final ChannelHandlerContext context) {
        List<JobInfo> jobContexts = scalingAPI.list();
        response(ResponseContentUtil.build(jobContexts), context, HttpResponseStatus.OK);
    }
    
    private void getJobProgress(final ChannelHandlerContext context, final String requestPath) {
        try {
            response(ResponseContentUtil.build(scalingAPI.getProgress(getJobId(requestPath))), context, HttpResponseStatus.OK);
        } catch (final ScalingJobNotFoundException ex) {
            response(ResponseContentUtil.handleBadRequest(ex.getMessage()), context, HttpResponseStatus.BAD_REQUEST);
        }
    }
    
    private void stopJob(final ChannelHandlerContext context, final String requestPath) {
        scalingAPI.stop(getJobId(requestPath));
        response(ResponseContentUtil.success(), context, HttpResponseStatus.OK);
    }
    
    private void checkJob(final ChannelHandlerContext context, final String requestPath) {
        try {
            response(ResponseContentUtil.build(scalingAPI.dataConsistencyCheck(getJobId(requestPath))), context, HttpResponseStatus.OK);
        } catch (final ScalingJobNotFoundException ex) {
            response(ResponseContentUtil.handleBadRequest(ex.getMessage()), context, HttpResponseStatus.BAD_REQUEST);
        }
    }
    
    private void resetJob(final ChannelHandlerContext context, final String requestPath) {
        try {
            scalingAPI.reset(getJobId(requestPath));
            response(ResponseContentUtil.success(), context, HttpResponseStatus.OK);
        } catch (final ScalingJobNotFoundException | SQLException ex) {
            response(ResponseContentUtil.handleBadRequest(ex.getMessage()), context, HttpResponseStatus.BAD_REQUEST);
        }
    }
    
    private long getJobId(final String requestPath) {
        return Long.parseLong(requestPath.split("/")[4]);
    }
    
    private void response(final Object content, final ChannelHandlerContext context, final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(GSON.toJson(content), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        context.writeAndFlush(response);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        log.error("Http request handle occur error:", cause);
        response(ResponseContentUtil.handleException(cause.toString()), context, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        context.close();
    }
}
