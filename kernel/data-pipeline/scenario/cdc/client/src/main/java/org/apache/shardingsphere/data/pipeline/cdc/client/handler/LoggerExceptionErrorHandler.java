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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ServerErrorResult;

/**
 * Logger exception error handler.
 */
@RequiredArgsConstructor
@Slf4j
public final class LoggerExceptionErrorHandler implements ExceptionHandler, ServerErrorResultHandler {
    
    @Override
    public void handleServerError(final ChannelHandlerContext ctx, final ServerErrorResult result) {
        log.error("Server error, code: {}, message: {}", result.getErrorCode(), result.getErrorMessage());
    }
    
    @Override
    public void handleException(final ChannelHandlerContext ctx, final Throwable throwable) {
        log.error("Exception error: ", throwable);
    }
}
