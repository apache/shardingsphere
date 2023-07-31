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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
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
    
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    
    private Status status;
    
    private String errorCode;
    
    private String errorMessage;
    
    private Object result;
    
    /**
     * Wait response.
     *
     * @param timeoutMillis timeout milliseconds
     * @return true if received response, false if timeout
     */
    @SneakyThrows(InterruptedException.class)
    public boolean waitResponse(final long timeoutMillis) {
        return countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
