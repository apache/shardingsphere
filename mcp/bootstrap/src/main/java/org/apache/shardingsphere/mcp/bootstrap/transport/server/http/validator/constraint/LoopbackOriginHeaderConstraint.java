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

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;

import java.net.URI;
import java.util.Objects;

/**
 * Loopback origin header constraint.
 */
public final class LoopbackOriginHeaderConstraint implements TransportHeaderConstraint {
    
    @Override
    public String getConstraintKey() {
        return "Origin";
    }
    
    @Override
    public void validate(final String value) throws ServerTransportSecurityException {
        if (value.isEmpty()) {
            return;
        }
        try {
            String host = Objects.toString(URI.create(value).getHost(), "").trim();
            ShardingSpherePreconditions.checkState(HttpTransportHostUtils.isLoopbackHost(host), () -> new ServerTransportSecurityException(403, "Origin is not allowed for the current binding."));
        } catch (final IllegalArgumentException ex) {
            throw new ServerTransportSecurityException(403, "Origin is not allowed for the current binding.");
        }
    }
}
