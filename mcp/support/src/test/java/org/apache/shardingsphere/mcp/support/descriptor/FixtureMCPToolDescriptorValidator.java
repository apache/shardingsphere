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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

import java.util.Map;

/**
 * Fixture MCP tool descriptor validator.
 */
public final class FixtureMCPToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return "database_gateway_extension_test_tool".equals(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        Object properties = toolDescriptor.getOutputSchema().get("properties");
        if (!(properties instanceof Map) || !((Map<?, ?>) properties).containsKey("extension_marker")) {
            throw new IllegalStateException("Tool `database_gateway_extension_test_tool` outputSchema must declare `extension_marker`.");
        }
    }
}
