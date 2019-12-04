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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.controller.ScalingJobController;

import java.util.regex.Pattern;


/**
 * Http server handler.
 *
 * @author ssxlulu
 */
@Slf4j
public final class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Pattern URL_PATTERN = Pattern.compile("(^/shardingscaling/start)|(^/shardingscaling/(progress|stop)/\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final Gson GSON = new Gson();

    private static final ScalingJobController SCALING_JOB_CONTROLLER = new ScalingJobController();

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final FullHttpRequest request) {
        String requestPath = request.uri();
        String requestBody = request.content().toString(CharsetUtil.UTF_8);
        HttpMethod method = request.method();
        if (!URL_PATTERN.matcher(requestPath).matches()) {
            response("not support request", channelHandlerContext, HttpResponseStatus.BAD_REQUEST, request);
            return;
        }
        if ("/shardingscaling/start".equalsIgnoreCase(requestPath) && method.equals(HttpMethod.POST)) {
            startShardingScalingJob(requestBody);
            response("start", channelHandlerContext, HttpResponseStatus.OK, request);
            return;
        }
        if (requestPath.contains("/shardingscaling/progress/") && method.equals(HttpMethod.GET)) {
            //TODO
            response("progress", channelHandlerContext, HttpResponseStatus.OK, request);
            return;
        }
        if (requestPath.contains("/shardingscaling/stop/") && method.equals(HttpMethod.DELETE)) {
            //TODO
            response("stop", channelHandlerContext, HttpResponseStatus.OK, request);
            return;
        }
        response("not support request", channelHandlerContext, HttpResponseStatus.BAD_REQUEST, request);
    }

    /**
     * start sharding scaling job.
     *
     * @param requestBody json format configuration of sharding scaling job
     */
    private void startShardingScalingJob(final String requestBody) {
        //TODO
    }

    /**
     * response to client.
     *
     * @param content content for response
     * @param ctx channelHandlerContext
     * @param status http response status
     * @param request http request
     */
    private void response(final String content, final ChannelHandlerContext ctx, final HttpResponseStatus status, final HttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            if (!request.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        ChannelFuture future = ctx.writeAndFlush(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("request error", cause);
        ctx.close();
    }
}
