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

import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.LinkedList;

/**
 * YAML MCP resource navigation descriptor.
 */
@Getter
@Setter
public final class YamlMCPResourceNavigationDescriptor {
    
    @NotBlank(message = "is required")
    private String from;
    
    @NotBlank(message = "is required")
    private String to;
    
    private Collection<String> requiredArguments = new LinkedList<>();
    
    private Collection<String> carriedArguments = new LinkedList<>();
    
    @NotBlank(message = "is required")
    private String description;
}
