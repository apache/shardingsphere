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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint;

import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.HttpTransportSecurityHeaderUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.TransportHeaderConstraintException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loopback origin header constraint.
 */
public final class LoopbackOriginHeaderConstraint implements TransportHeaderConstraint {
    
    private static final String ORIGIN_HEADER = "Origin";
    
    private static final String FORBIDDEN_MESSAGE = "Origin is not allowed for the current binding.";
    
    @Override
    public void validate(final Map<String, List<String>> headers) throws TransportHeaderConstraintException {
        String origin = HttpTransportSecurityHeaderUtils.getFirstHeaderValue(headers, ORIGIN_HEADER);
        if (origin.isEmpty()) {
            return;
        }
        try {
            String host = Objects.toString(URI.create(origin).getHost(), "").trim();
            if (!HttpTransportHostUtils.isLoopbackHost(host)) {
                throw new TransportHeaderConstraintException(403, FORBIDDEN_MESSAGE);
            }
        } catch (final IllegalArgumentException ex) {
            throw new TransportHeaderConstraintException(403, FORBIDDEN_MESSAGE);
        }
    }
}
