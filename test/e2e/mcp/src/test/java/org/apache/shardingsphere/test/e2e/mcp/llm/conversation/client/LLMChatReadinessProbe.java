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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe.ReadinessResult;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class LLMChatReadinessProbe {
    
    private static final long INITIAL_READINESS_INTERVAL_MILLIS = 250L;
    
    private static final long MAX_READINESS_INTERVAL_MILLIS = 2000L;
    
    private final LLME2EConfiguration config;
    
    private final LLMChatModelClient client;
    
    void waitUntilReady() throws InterruptedException {
        new ReadinessProbe(config.getReadyTimeoutSeconds() * 1000L, INITIAL_READINESS_INTERVAL_MILLIS, MAX_READINESS_INTERVAL_MILLIS)
                .waitUntilReady(this::checkReadiness, this::createReadinessException);
    }
    
    private ReadinessResult<Boolean> checkReadiness() throws InterruptedException {
        try {
            return isReadinessContractReady() ? ReadinessResult.ready(Boolean.TRUE) : ReadinessResult.retry(null);
        } catch (final IOException ex) {
            return ReadinessResult.retry(ex);
        } catch (final IllegalStateException ex) {
            return isNonRetryableReadinessFailure(ex) ? ReadinessResult.failed(ex) : ReadinessResult.retry(ex);
        }
    }
    
    private boolean isReadinessContractReady() throws IOException, InterruptedException {
        return isModelListReady() && isCompletionProbeReady() && isRequiredToolProbeReady() && isAutoToolProbeReady() && isFinalAnswerProbeReady();
    }
    
    private boolean isModelListReady() throws IOException, InterruptedException {
        HttpResponse<String> response = client.sendModelListRequest();
        if (200 != response.statusCode()) {
            throw new IllegalStateException(client.createHttpReadinessFailure("model-list", response));
        }
        return client.containsModel(response.body());
    }
    
    private boolean isCompletionProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = client.sendReadinessCompletionRequest(List.of(LLMChatMessage.user("Return ok.")), List.of(), "", false);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(client.createHttpReadinessFailure("completion", response));
        }
        return client.hasCompletionChoice(response.body());
    }
    
    private boolean isRequiredToolProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = client.sendReadinessCompletionRequest(List.of(LLMChatMessage.user("Call mcp_read_resource with uri mcp://readiness.")),
                client.createReadinessTools(), "required", false);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(client.createHttpReadinessFailure("tool-choice-required", response));
        }
        return client.hasToolCallChoice(response.body());
    }
    
    private boolean isAutoToolProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = client.sendReadinessCompletionRequest(List.of(LLMChatMessage.user("Return ok without calling tools.")), client.createReadinessTools(), "auto", false);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(client.createHttpReadinessFailure("tool-choice-auto", response));
        }
        return client.hasCompletionChoice(response.body());
    }
    
    private boolean isFinalAnswerProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = client.sendReadinessCompletionRequest(List.of(LLMChatMessage.user(
                "Respond with exactly {\"status\":\"ok\"} and no Markdown fences.")), List.of(), "none", true);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(client.createHttpReadinessFailure("tool-choice-none-json", response));
        }
        return client.hasJsonCompletionChoice(response.body());
    }
    
    private boolean isNonRetryableReadinessFailure(final Exception cause) {
        String message = Objects.toString(cause.getMessage(), "");
        return message.contains("HTTP 400") || message.contains("HTTP 401");
    }
    
    private IllegalStateException createReadinessException(final Exception cause, final int attemptCount, final long elapsedMillis) {
        IllegalStateException result = new IllegalStateException(createReadinessFailureMessage(cause, attemptCount, elapsedMillis));
        if (null != cause) {
            result.initCause(cause);
        }
        return result;
    }
    
    private String createReadinessFailureMessage(final Exception cause, final int attemptCount, final long elapsedMillis) {
        String result = String.format("Model service is not ready for `%s` after %d readiness attempt(s), elapsedMillis=%d, timeoutSeconds=%d.",
                config.getModelName(), attemptCount, elapsedMillis, config.getReadyTimeoutSeconds());
        return null == cause || null == cause.getMessage() || cause.getMessage().isBlank()
                ? result
                : result + " Last readiness failure: " + cause.getMessage();
    }
}
