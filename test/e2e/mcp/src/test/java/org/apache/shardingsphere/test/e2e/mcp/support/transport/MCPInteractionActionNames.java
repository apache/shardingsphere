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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * MCP interaction action names used by the LLM E2E support layer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPInteractionActionNames {
    
    public static final String LIST_RESOURCES = "mcp_list_resources";
    
    public static final String READ_RESOURCE = "mcp_read_resource";
    
    public static final String RESOURCE_LIST_KIND = "resource_list";
    
    public static final String RESOURCE_READ_KIND = "resource_read";
}
