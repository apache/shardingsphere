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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe.ReadinessResult;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Packaged distribution HTTP runtime.
 */
public final class PackagedDistributionHttpRuntime implements AutoCloseable {
    
    private static final long STARTUP_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(20L);
    
    private static final long STARTUP_POLL_INTERVAL_MILLIS = 250L;
    
    private static final String ENDPOINT_MARKER = "endpoint=";
    
    private final PackagedDistributionProcessSupport processSupport;
    
    public PackagedDistributionHttpRuntime(final PreparedPackagedDistribution distribution) {
        processSupport = new PackagedDistributionProcessSupport(distribution, "mcp-packaged-http-e2e");
    }
    
    /**
     * Open an HTTP interaction client after the packaged distribution becomes ready.
     *
     * @return opened interaction client
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    public MCPInteractionClient openInteractionClient() throws IOException, InterruptedException {
        processSupport.startIfNeeded();
        return new ReadinessProbe(STARTUP_TIMEOUT_MILLIS, STARTUP_POLL_INTERVAL_MILLIS, STARTUP_POLL_INTERVAL_MILLIS)
                .waitUntilReady(this::openInteractionClientIfReady, this::createStartupFailure);
    }
    
    @Override
    public void close() {
        processSupport.close();
    }
    
    private ReadinessResult<MCPInteractionClient> openInteractionClientIfReady() throws InterruptedException {
        if (!processSupport.isAlive()) {
            return ReadinessResult.failed(null);
        }
        Optional<URI> endpointUri = findEndpointUri(processSupport.getOutputMessages());
        if (endpointUri.isEmpty()) {
            return ReadinessResult.retry(new IllegalStateException("Packaged MCP HTTP endpoint has not been reported yet."));
        }
        MCPHttpInteractionClient result = new MCPHttpInteractionClient(endpointUri.get(), HttpClient.newHttpClient());
        try {
            result.open();
            return ReadinessResult.ready(result);
        } catch (final IOException | IllegalStateException ex) {
            closeInteractionClientQuietly(result);
            return ReadinessResult.retry(new IllegalStateException("Packaged MCP HTTP distribution is not ready yet.", ex));
        }
    }
    
    private IllegalStateException createStartupFailure(final Exception cause, final int ignoredAttemptCount, final long ignoredElapsedMillis) {
        return new IllegalStateException("Packaged MCP HTTP distribution failed to become ready. output: " + ProcessOutputDiagnostics.format(processSupport.getOutputMessages()), cause);
    }
    
    private void closeInteractionClientQuietly(final MCPInteractionClient interactionClient) {
        try {
            interactionClient.close();
        } catch (final IOException ignored) {
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
    
    static Optional<URI> findEndpointUri(final Collection<String> outputMessages) {
        for (String each : outputMessages) {
            int startIndex = each.indexOf(ENDPOINT_MARKER);
            if (startIndex < 0) {
                continue;
            }
            startIndex += ENDPOINT_MARKER.length();
            int endIndex = each.indexOf(',', startIndex);
            String candidate = each.substring(startIndex, endIndex < 0 ? each.length() : endIndex).trim();
            try {
                URI result = URI.create(candidate);
                if ("http".equalsIgnoreCase(result.getScheme()) && null != result.getHost() && result.getPort() > 0) {
                    return Optional.of(result);
                }
            } catch (final IllegalArgumentException ignored) {
            }
        }
        return Optional.empty();
    }
}
