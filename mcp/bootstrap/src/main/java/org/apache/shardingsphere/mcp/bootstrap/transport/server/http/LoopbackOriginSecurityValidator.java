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

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

final class LoopbackOriginSecurityValidator implements ServerTransportSecurityValidator {
    
    private static final String ORIGIN_HEADER = "Origin";
    
    static ServerTransportSecurityValidator create(final String bindHost) {
        return isLoopbackHost(bindHost) ? new LoopbackOriginSecurityValidator() : NOOP;
    }
    
    private static boolean isLoopbackHost(final String rawHost) {
        String host = Objects.toString(rawHost, "").trim().toLowerCase(Locale.ENGLISH);
        return "127.0.0.1".equals(host) || "localhost".equals(host) || "::1".equals(host);
    }
    
    @Override
    public void validateHeaders(final Map<String, List<String>> headers) throws ServerTransportSecurityException {
        String origin = getOriginValue(headers);
        if (origin.isEmpty()) {
            return;
        }
        try {
            String host = Objects.toString(URI.create(origin).getHost(), "").trim();
            ShardingSpherePreconditions.checkState(isLoopbackHost(host), () -> new ServerTransportSecurityException(403, "Origin is not allowed for the current binding."));
        } catch (final IllegalArgumentException ex) {
            throw new ServerTransportSecurityException(403, "Origin is not allowed for the current binding.");
        }
    }
    
    private static String getOriginValue(final Map<String, List<String>> headers) {
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            if (ORIGIN_HEADER.equalsIgnoreCase(entry.getKey()) && !entry.getValue().isEmpty()) {
                return Objects.toString(entry.getValue().get(0), "").trim();
            }
        }
        return "";
    }
}
