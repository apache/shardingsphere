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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.shardingsphere.mcp.api.session.MCPSessionAttribution;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Session attribution resolver.
 */
@Getter
public final class SessionAttributionResolver {

    private final SessionAttributionSourceConfiguration config;

    public SessionAttributionResolver(final SessionAttributionSourceConfiguration config) {
        this.config = config;
    }

    /**
     * Resolve session attribution from HTTP request.
     *
     * @param request HTTP request
     * @return session attribution
     */
    public Optional<MCPSessionAttribution> resolve(final HttpServletRequest request) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        String subject = getHeaderValue(request, config.getSubjectHeader());
        if (subject.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MCPSessionAttribution(subject, getHeaderValue(request, config.getSourceHeader()),
                resolveAttributes(Collections.list(request.getHeaderNames()), name -> getHeaderValue(request, name))));
    }

    /**
     * Resolve session attribution from header map.
     *
     * @param headers headers
     * @return session attribution
     */
    public Optional<MCPSessionAttribution> resolve(final Map<String, List<String>> headers) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        String subject = getHeaderValue(headers, config.getSubjectHeader());
        if (subject.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MCPSessionAttribution(subject, getHeaderValue(headers, config.getSourceHeader()),
                resolveAttributes(headers.keySet(), name -> getHeaderValue(headers, name))));
    }

    /**
     * Determine whether session attribution is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return null != config;
    }

    /**
     * Get summary for diagnostics.
     *
     * @return summary
     */
    public String getSummary() {
        return !isEnabled() ? "disabled" : String.format("trusted-header:%s", config.getSubjectHeader());
    }

    private Map<String, String> resolveAttributes(final Iterable<String> headerNames, final HeaderValueReader headerValueReader) {
        Map<String, String> result = new LinkedHashMap<>();
        String attributeHeaderPrefix = config.getAttributeHeaderPrefix();
        if (attributeHeaderPrefix.isEmpty()) {
            return result;
        }
        String normalizedPrefix = attributeHeaderPrefix.toLowerCase(Locale.ENGLISH);
        for (String each : headerNames) {
            String actualHeaderName = Objects.toString(each, "").trim();
            if (actualHeaderName.toLowerCase(Locale.ENGLISH).startsWith(normalizedPrefix)) {
                result.put(actualHeaderName.substring(attributeHeaderPrefix.length()), headerValueReader.read(actualHeaderName));
            }
        }
        return result;
    }

    private String getHeaderValue(final HttpServletRequest request, final String headerName) {
        return Objects.toString(request.getHeader(headerName), "").trim();
    }

    private String getHeaderValue(final Map<String, List<String>> headers, final String headerName) {
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            if (headerName.equalsIgnoreCase(entry.getKey()) && !entry.getValue().isEmpty()) {
                return Objects.toString(entry.getValue().get(0), "").trim();
            }
        }
        return "";
    }

    @FunctionalInterface
    private interface HeaderValueReader {

        String read(String headerName);
    }
}
