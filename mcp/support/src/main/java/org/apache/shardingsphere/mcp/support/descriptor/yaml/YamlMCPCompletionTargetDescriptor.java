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

package org.apache.shardingsphere.mcp.support.descriptor.yaml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.validator.MCPMetadataKey;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * YAML MCP completion target descriptor.
 */
@Getter
@Setter
public final class YamlMCPCompletionTargetDescriptor {
    
    @NotBlank(message = "is required")
    @Pattern(regexp = "prompt|resource", message = "must be prompt or resource")
    private String referenceType;
    
    @NotBlank(message = "is required")
    private String reference;
    
    @NotEmpty(message = "is required")
    @UniqueElements(message = "must not contain duplicate values")
    private Collection<@NotBlank(message = "is required") String> arguments = new LinkedList<>();
    
    @Max(value = 100, message = "must not exceed 100")
    @PositiveOrZero(message = "must be zero or positive")
    private int maxValues;
    
    @NotNull(message = "is required")
    private Map<@MCPMetadataKey String, Object> meta = new LinkedHashMap<>();
}
