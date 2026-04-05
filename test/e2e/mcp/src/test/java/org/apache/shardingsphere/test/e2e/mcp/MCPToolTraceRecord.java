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

package org.apache.shardingsphere.test.e2e.mcp;

import java.util.Map;

final class MCPToolTraceRecord {
    
    private final int sequence;
    
    private final String toolName;
    
    private final Map<String, Object> arguments;
    
    private final Map<String, Object> structuredContent;
    
    MCPToolTraceRecord(final int sequence, final String toolName, final Map<String, Object> arguments,
                       final Map<String, Object> structuredContent) {
        this.sequence = sequence;
        this.toolName = toolName;
        this.arguments = Map.copyOf(arguments);
        this.structuredContent = Map.copyOf(structuredContent);
    }
    
    int sequence() {
        return sequence;
    }
    
    String toolName() {
        return toolName;
    }
    
    Map<String, Object> arguments() {
        return arguments;
    }
    
    Map<String, Object> structuredContent() {
        return structuredContent;
    }
}
