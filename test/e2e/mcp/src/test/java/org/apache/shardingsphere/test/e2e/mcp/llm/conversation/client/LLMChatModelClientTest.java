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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client;

import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LLMChatModelClientTest {
    
    @Test
    void assertWaitUntilReadyWithModelList() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = createResponse(200, "{\"data\":[{\"id\":\"qwen3:1.7b\"}]}");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);
        new LLMChatModelClient(createConfiguration(1), httpClient).waitUntilReady();
        verify(httpClient).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }
    
    @Test
    void assertWaitUntilReadyWithCompletionProbe() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> modelListResponse = createResponse(200, "{\"data\":[]}");
        HttpResponse<String> completionResponse = createResponse(200, "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(modelListResponse, completionResponse);
        new LLMChatModelClient(createConfiguration(1), httpClient).waitUntilReady();
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        List<HttpRequest> actualRequests = requestCaptor.getAllValues();
        assertThat(actualRequests.get(0).uri().toString(), is("http://127.0.0.1:11434/v1/models"));
        assertThat(actualRequests.get(1).uri().toString(), is("http://127.0.0.1:11434/v1/chat/completions"));
    }
    
    @Test
    void assertWaitUntilReadyReportsProbeFailure() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> modelListResponse = createResponse(200, "{\"data\":[]}");
        HttpResponse<String> completionResponse = createResponse(401, "{\"error\":{\"code\":\"unauthorized\"}}");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(modelListResponse, completionResponse);
        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> new LLMChatModelClient(createConfiguration(1), httpClient).waitUntilReady());
        verify(httpClient, times(2)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        assertTrue(actualException.getMessage().startsWith("Model service is not ready for `qwen3:1.7b` after 1 readiness attempt(s), elapsedMillis="));
        assertTrue(actualException.getMessage().endsWith(
                "timeoutSeconds=1. Last readiness failure: completion readiness request returned HTTP 401 with error code `unauthorized`."));
    }
    
    private LLME2EConfiguration createConfiguration(final int readyTimeoutSeconds) {
        return new LLME2EConfiguration("http://127.0.0.1:11434/v1", "openai-compatible", "qwen3:1.7b", "ollama", readyTimeoutSeconds, 30, 10,
                Path.of("target/llm-e2e"), "run-id", RuntimeMode.DOCKER);
    }
    
    @SuppressWarnings("unchecked")
    private HttpResponse<String> createResponse(final int statusCode, final String body) {
        HttpResponse<String> result = mock(HttpResponse.class);
        when(result.statusCode()).thenReturn(statusCode);
        when(result.body()).thenReturn(body);
        return result;
    }
}
