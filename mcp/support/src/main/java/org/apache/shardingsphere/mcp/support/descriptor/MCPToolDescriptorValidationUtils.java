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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

import java.util.Collection;
import java.util.Map;

/**
 * MCP tool descriptor validation utilities.
 */
public final class MCPToolDescriptorValidationUtils {
    
    private MCPToolDescriptorValidationUtils() {
    }
    
    /**
     * Validate required output fields.
     *
     * @param descriptor tool descriptor
     * @param requiredFields required fields
     */
    public static void validateRequiredOutputFields(final MCPToolDescriptor descriptor, final Collection<String> requiredFields) {
        Map<?, ?> properties = (Map<?, ?>) descriptor.getOutputSchema().get("properties");
        for (String each : requiredFields) {
            ShardingSpherePreconditions.checkState(properties.containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` outputSchema must declare `%s`.", descriptor.getName(), each)));
            Object property = properties.get(each);
            ShardingSpherePreconditions.checkState(property instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `%s` outputSchema property `%s` must be an object.", descriptor.getName(), each)));
            Object description = ((Map<?, ?>) property).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool output field `%s.%s` description", descriptor.getName(), each));
        }
    }
    
    /**
     * Check description.
     *
     * @param value description value
     * @param label description label
     */
    public static void checkDescription(final String value, final String label) {
        ShardingSpherePreconditions.checkState(null != value && !value.isBlank(), () -> new IllegalStateException(String.format("%s is required.", label)));
    }
}
