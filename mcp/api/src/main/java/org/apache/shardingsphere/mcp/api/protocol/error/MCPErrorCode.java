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

package org.apache.shardingsphere.mcp.api.protocol.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MCP error code.
 */
@RequiredArgsConstructor
@Getter
public enum MCPErrorCode {
    
    INVALID_REQUEST("invalid_request"),
    
    NOT_FOUND("not_found"),
    
    UNSUPPORTED("unsupported"),
    
    TIMEOUT("timeout"),
    
    UNAVAILABLE("unavailable"),
    
    TRANSACTION_STATE_ERROR("transaction_state_error"),
    
    QUERY_FAILED("query_failed"),
    
    RATE_LIMITED("rate_limited"),
    
    INVALID_OUTPUT_SCHEMA("invalid_output_schema");
    
    private final String code;
}
