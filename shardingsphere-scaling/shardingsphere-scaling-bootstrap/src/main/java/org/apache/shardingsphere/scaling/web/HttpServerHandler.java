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
import org.apache.shardingsphere.scaling.core.ScalingJobController;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.utils.SyncConfigurationUtil;
import org.apache.shardingsphere.scaling.utils.ResponseContentUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Http server handler.
 */
@Slf4j
public final class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    private static final Pattern URL_PATTERN = Pattern.compile("(^/scaling/job/(start|stop|list))|(^/scaling/job/(progress|check)/\\d+)", Pattern.CASE_INSENSITIVE);
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
    private static final ScalingJobController SCALING_JOB_CONTROLLER = new ScalingJobController();
    
    @Override
    protected void channelRead0(final ChannelHandlerContext context, final FullHttpRequest request) {
        String requestPath = request.uri();
        String requestBody = request.content().toString(CharsetUtil.UTF_8);
        log.info("Http request path: {}", requestPath);
        log.info("Http request body: {}", requestBody);
        HttpMethod method = request.method();
        if (!URL_PATTERN.matcher(requestPath).matches()) {
            response(GSON.toJson(ResponseContentUtil.handleBadRequest("Not support request!")), context, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if ("/scaling/job/start".equalsIgnoreCase(requestPath) && method.equals(HttpMethod.POST)) {
            startJob(context, requestBody);
            return;
        }
        if (requestPath.contains("/scaling/job/progress/") && method.equals(HttpMethod.GET)) {
            getJobProgress(context, requestPath);
            return;
        }
        if ("/scaling/job/list".equalsIgnoreCase(requestPath) && method.equals(HttpMethod.GET)) {
            listAllJobs(context);
            return;
        }
        if ("/scaling/job/stop".equalsIgnoreCase(requestPath) && method.equals(HttpMethod.POST)) {
            stopJob(context, requestBody);
            return;
        }
        if (requestPath.contains("/scaling/job/check/") && method.equals(HttpMethod.GET)) {
            checkJob(context, requestPath);
            return;
        }
        response(GSON.toJson(ResponseContentUtil.handleBadRequest("Not support request!")), context, HttpResponseStatus.BAD_REQUEST);
    }
    
    private void startJob(final ChannelHandlerContext context, final String requestBody) {
        ScalingConfiguration scalingConfig = GSON.fromJson(requestBody, ScalingConfiguration.class);
        ShardingScalingJob shardingScalingJob = new ShardingScalingJob(scalingConfig);
        shardingScalingJob.getSyncConfigurations().addAll(SyncConfigurationUtil.toSyncConfigurations(scalingConfig));
        SCALING_JOB_CONTROLLER.start(shardingScalingJob);
        response(GSON.toJson(ResponseContentUtil.success()), context, HttpResponseStatus.OK);
    }
    
    private void getJobProgress(final ChannelHandlerContext context, final String requestPath) {
        int jobId = Integer.parseInt(requestPath.split("/")[4]);
        try {
            SyncProgress progresses = SCALING_JOB_CONTROLLER.getProgresses(jobId);
            response(GSON.toJson(ResponseContentUtil.build(progresses)), context, HttpResponseStatus.OK);
        } catch (final ScalingJobNotFoundException ex) {
            response(GSON.toJson(ResponseContentUtil.handleBadRequest(ex.getMessage())), context, HttpResponseStatus.BAD_REQUEST);
        }
    }
    
    private void listAllJobs(final ChannelHandlerContext context) {
        List<ShardingScalingJob> shardingScalingJobs = SCALING_JOB_CONTROLLER.listShardingScalingJobs();
        response(GSON.toJson(ResponseContentUtil.build(shardingScalingJobs)), context, HttpResponseStatus.OK);
    }
    
    private void stopJob(final ChannelHandlerContext context, final String requestBody) {
        ShardingScalingJob shardingScalingJob = GSON.fromJson(requestBody, ShardingScalingJob.class);
        //TODO, Exception handling
        SCALING_JOB_CONTROLLER.stop(shardingScalingJob.getJobId());
        response(GSON.toJson(ResponseContentUtil.success()), context, HttpResponseStatus.OK);
    }
    
    private void checkJob(final ChannelHandlerContext context, final String requestPath) {
        int jobId = Integer.parseInt(requestPath.split("/")[4]);
        try {
            Map<String, DataConsistencyCheckResult> dataConsistencyCheckResultMap = SCALING_JOB_CONTROLLER.check(jobId);
            response(GSON.toJson(ResponseContentUtil.build(dataConsistencyCheckResultMap)), context, HttpResponseStatus.OK);
        } catch (final ScalingJobNotFoundException ex) {
            response(GSON.toJson(ResponseContentUtil.handleBadRequest(ex.getMessage())), context, HttpResponseStatus.BAD_REQUEST);
        }
    }
    
    private void response(final String content, final ChannelHandlerContext context, final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        context.writeAndFlush(response);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        log.error("Http request handle occur error:", cause);
        response(GSON.toJson(ResponseContentUtil.handleException(cause.toString())), context, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        context.close();
    }
}
