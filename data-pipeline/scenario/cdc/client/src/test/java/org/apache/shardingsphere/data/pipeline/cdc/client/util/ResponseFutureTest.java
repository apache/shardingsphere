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

import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.exception.GetResultTimeoutException;
import org.apache.shardingsphere.data.pipeline.cdc.client.exception.ServerResultException;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResponseFutureTest {
    
    @Test
    void assertWaitResponseResultTimeout() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        ResponseFuture responseFuture = new ResponseFuture("foo_req_id", Type.LOGIN);
        connectionContext.getResponseFutureMap().put("foo_req_id", responseFuture);
        GetResultTimeoutException ex = assertThrows(GetResultTimeoutException.class, () -> responseFuture.waitResponseResult(1L, connectionContext));
        assertThat(ex.getMessage(), is("Get result timeout"));
        assertFalse(connectionContext.getResponseFutureMap().containsKey("foo_req_id"));
    }
    
    @Test
    void assertWaitResponseResultSuccess() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        ResponseFuture responseFuture = new ResponseFuture("foo_req_id", Type.START_STREAMING);
        connectionContext.getResponseFutureMap().put("foo_req_id", responseFuture);
        responseFuture.setResult("success_result");
        responseFuture.countDown();
        Object actualResult = responseFuture.waitResponseResult(1000L, connectionContext);
        assertThat(actualResult, is("success_result"));
        assertFalse(connectionContext.getResponseFutureMap().containsKey("foo_req_id"));
    }
    
    @Test
    void assertWaitResponseResultWithServerError() {
        ClientConnectionContext connectionContext = new ClientConnectionContext();
        ResponseFuture responseFuture = new ResponseFuture("foo_req_id", Type.STOP_STREAMING);
        connectionContext.getResponseFutureMap().put("foo_req_id", responseFuture);
        responseFuture.setErrorCode("500");
        responseFuture.setErrorMessage("mock error");
        responseFuture.countDown();
        ServerResultException ex = assertThrows(ServerResultException.class, () -> responseFuture.waitResponseResult(1000L, connectionContext));
        assertThat(ex.getMessage(), is("Get STOP_STREAMING response failed, code:500, reason: mock error"));
        assertFalse(connectionContext.getResponseFutureMap().containsKey("foo_req_id"));
    }
}
