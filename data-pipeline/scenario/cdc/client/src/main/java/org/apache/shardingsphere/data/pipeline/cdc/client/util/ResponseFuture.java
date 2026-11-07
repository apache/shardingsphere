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

package org.apache.shardingsphere.data.pipeline.cdc.client.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.exception.GetResultTimeoutException;
import org.apache.shardingsphere.data.pipeline.cdc.client.exception.ServerResultException;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Response future.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class ResponseFuture {
    
    @Getter(AccessLevel.PRIVATE)
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    
    private final String requestId;
    
    private final Type requestType;
    
    private Status status;
    
    private String errorCode;
    
    private String errorMessage;
    
    private Object result;
    
    /**
     * Wait response result.
     *
     * @param timeoutMillis timeout milliseconds
     * @param connectionContext connection context
     * @return response result
     * @throws GetResultTimeoutException get result timeout
     * @throws ServerResultException server result exception
     */
    @SneakyThrows(InterruptedException.class)
    public Object waitResponseResult(final long timeoutMillis, final ClientConnectionContext connectionContext) {
        if (!countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            connectionContext.getResponseFutureMap().remove(requestId);
            throw new GetResultTimeoutException("Get result timeout");
        }
        connectionContext.getResponseFutureMap().remove(requestId);
        if (!Strings.isNullOrEmpty(errorMessage)) {
            throw new ServerResultException(String.format("Get %s response failed, code:%s, reason: %s", requestType.name(), errorCode, errorMessage));
        }
        return result;
    }
    
    /**
     * Count down.
     */
    public void countDown() {
        countDownLatch.countDown();
    }
}
