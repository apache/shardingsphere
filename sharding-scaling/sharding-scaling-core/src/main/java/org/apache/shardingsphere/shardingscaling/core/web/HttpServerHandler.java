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

package org.apache.shardingsphere.shardingscaling.core.web;

import com.google.gson.Gson;
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
import org.apache.shardingsphere.shardingscaling.core.ShardingScalingJob;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ScalingJobController;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.shardingscaling.core.web.entity.Job;
import org.apache.shardingsphere.shardingscaling.core.web.entity.ResponseContent;
import org.apache.shardingsphere.shardingscaling.core.web.entity.ResponseMessage;
import org.apache.shardingsphere.shardingscaling.core.web.util.SyncConfigurationUtil;

import java.util.regex.Pattern;


/**
 * Http server handler.
 *
 * @author ssxlulu
 */
@Slf4j
public final class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Pattern URL_PATTERN = Pattern.compile("(^/shardingscaling/job/(start|stop))|(^/shardingscaling/job/progress/\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final Gson GSON = new Gson();

    private static final ScalingJobController SCALING_JOB_CONTROLLER = new ScalingJobController();

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final FullHttpRequest request) {
        String requestPath = request.uri();
        String requestBody = request.content().toString(CharsetUtil.UTF_8);
        HttpMethod method = request.method();
        if (!URL_PATTERN.matcher(requestPath).matches()) {
            throw new RuntimeException(ResponseMessage.BAD_REQUEST);
        }
        if ("/shardingscaling/job/start".equalsIgnoreCase(requestPath) && method.equals(HttpMethod.POST)) {
            ScalingConfiguration scalingConfiguration = GSON.fromJson(requestBody, ScalingConfiguration.class);
            ShardingScalingJob shardingScalingJob = new ShardingScalingJob("Local Sharding Scaling Job");
            shardingScalingJob.getSyncConfigurations().addAll(SyncConfigurationUtil.toSyncConfigurations(scalingConfiguration));
            //TODO, Exception handling
            SCALING_JOB_CONTROLLER.start(shardingScalingJob);
            ResponseContent<Job> responseContent = new ResponseContent<>(ResponseMessage.START_SUCCESS, new Job(shardingScalingJob.getJobId()));
            response(GSON.toJson(responseContent), channelHandlerContext, HttpResponseStatus.OK);
            return;
        }
        if (requestPath.contains("/shardingscaling/job/progress/") && method.equals(HttpMethod.GET)) {
            Integer jobId = Integer.valueOf(requestPath.split("/")[4]);
            try {
                SyncProgress progresses = SCALING_JOB_CONTROLLER.getProgresses(jobId);
                ResponseContent<SyncProgress> responseContent = new ResponseContent<>(ResponseMessage.GET_PROGRESS_SUCCESS, progresses);
                response(GSON.toJson(responseContent), channelHandlerContext, HttpResponseStatus.OK);
            } catch (ScalingJobNotFoundException e) {
                ResponseContent<String> responseContent = new ResponseContent<>(ResponseMessage.GET_PROGRESS_ERROR, e.getMessage());
                response(GSON.toJson(responseContent), channelHandlerContext, HttpResponseStatus.OK);
            }
            return;
        }
        if ("/shardingscaling/job/stop".equalsIgnoreCase(requestPath) && method.equals(HttpMethod.POST)) {
            Job job = GSON.fromJson(requestBody, Job.class);
            //TODO, Exception handling
            SCALING_JOB_CONTROLLER.stop(job.getId());
            ResponseContent<String> responseContent = new ResponseContent<>(ResponseMessage.STOP_SUCCESS, "");
            response(GSON.toJson(responseContent), channelHandlerContext, HttpResponseStatus.OK);
            return;
        }
        throw new RuntimeException(ResponseMessage.BAD_REQUEST);
    }

    /**
     * response to client.
     *
     * @param content content for response
     * @param ctx     channelHandlerContext
     * @param status  http response status
     */
    private void response(final String content, final ChannelHandlerContext ctx, final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("request error", cause);
        response(ResponseMessage.BAD_REQUEST, ctx, HttpResponseStatus.BAD_REQUEST);
        ctx.close();
    }

}
