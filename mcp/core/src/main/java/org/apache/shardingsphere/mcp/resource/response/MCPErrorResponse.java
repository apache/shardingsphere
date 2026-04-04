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

package org.apache.shardingsphere.mcp.resource.response;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.protocol.MCPError;

import java.util.Locale;
import java.util.Map;

/**
 * Response for resource errors.
 */
@RequiredArgsConstructor
public final class MCPErrorResponse implements MCPResourceResponse {
    
    private final MCPError error;
    
    @Override
    public Map<String, Object> toPayload() {
        return Map.of("error_code", error.getCode().name().toLowerCase(Locale.ENGLISH), "message", error.getMessage());
    }
}
