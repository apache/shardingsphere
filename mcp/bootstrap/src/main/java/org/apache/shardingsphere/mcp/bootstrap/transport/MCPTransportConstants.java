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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.spec.ProtocolVersions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * MCP transport constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportConstants {
    
    public static final String PROTOCOL_VERSION = ProtocolVersions.MCP_2025_11_25;
    
    public static final List<String> SUPPORTED_PROTOCOL_VERSIONS = Collections.singletonList(PROTOCOL_VERSION);
    
    public static final String SERVER_NAME = "Apache ShardingSphere MCP";
    
    public static final String SERVER_INSTRUCTIONS = "Apache ShardingSphere MCP. Read `shardingsphere://capabilities` first as the current public-surface source of truth, "
            + "then use resource-first metadata discovery. "
            + "Use `execute_query` only for read-only SELECT or EXPLAIN ANALYZE. Use `execute_update` with `execution_mode=preview` before side effects, "
            + "and continue from `next_actions` or `recovery.next_actions` instead of guessing hidden tools or arguments.";
}
