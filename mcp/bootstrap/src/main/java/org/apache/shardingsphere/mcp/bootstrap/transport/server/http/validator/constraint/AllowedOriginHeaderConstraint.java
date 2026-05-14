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
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportOriginUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Allowed origin header constraint.
 */
public final class AllowedOriginHeaderConstraint implements TransportHeaderConstraint {
    
    private final Set<String> allowedOrigins;
    
    public AllowedOriginHeaderConstraint(final Collection<String> allowedOrigins) {
        this.allowedOrigins = Set.copyOf(HttpTransportOriginUtils.normalizeOrigins(allowedOrigins));
    }
    
    @Override
    public String getConstraintKey() {
        return "Origin";
    }
    
    @Override
    public void validate(final String value) throws ServerTransportSecurityException {
        String actualOrigin = HttpTransportOriginUtils.normalizeOrigin(value);
        ShardingSpherePreconditions.checkState(!actualOrigin.isEmpty() && allowedOrigins.contains(actualOrigin),
                () -> new ServerTransportSecurityException(403, "Origin is not allowed for the current binding."));
    }
}
