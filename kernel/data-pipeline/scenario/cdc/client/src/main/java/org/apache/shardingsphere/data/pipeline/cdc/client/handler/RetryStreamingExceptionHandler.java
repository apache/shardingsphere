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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Retry streaming exception handler.
 */
@Slf4j
public class RetryStreamingExceptionHandler implements ExceptionHandler {
    
    private final CDCClient cdcClient;
    
    private final AtomicInteger maxRetryTimes = new AtomicInteger(0);
    
    private final int retryIntervalMills;
    
    private final int retryTimes;
    
    public RetryStreamingExceptionHandler(final CDCClient cdcClient, final int maxRetryTimes, final int retryIntervalMills) {
        this.cdcClient = cdcClient;
        this.maxRetryTimes.set(maxRetryTimes);
        this.retryIntervalMills = retryIntervalMills;
        retryTimes = 0;
    }
    
    @Override
    public void handleException(final ChannelHandlerContext ctx, final Throwable throwable) {
        log.error("Socket error: {}", throwable.getMessage());
        reconnect(ctx);
    }
    
    @SneakyThrows(InterruptedException.class)
    private void reconnect(final ChannelHandlerContext ctx) {
        maxRetryTimes.incrementAndGet();
        if (null == cdcClient) {
            log.warn("CDC client is null, could not retry");
            return;
        }
        ClientConnectionContext connectionContext = ctx.channel().attr(ClientConnectionContext.CONTEXT_KEY).get();
        if (retryTimes > maxRetryTimes.get()) {
            log.warn("Retry times exceed 5, stop streaming");
            connectionContext.getStreamingIds().forEach(each -> CompletableFuture.runAsync(() -> cdcClient.stopStreaming(each)));
            return;
        }
        TimeUnit.MILLISECONDS.sleep(retryIntervalMills);
        log.info("Retry to restart streaming, retry count: {}", maxRetryTimes.get());
        connectionContext.getStreamingIds().forEach(each -> CompletableFuture.runAsync(() -> cdcClient.restartStreaming(each)));
    }
}
