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
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;

import javax.validation.Valid;
import java.util.Collection;
import java.util.LinkedList;

/**
 * YAML MCP descriptor catalog.
 */
@Getter
@Setter
public final class YamlMCPDescriptorCatalog implements YamlConfiguration {
    
    private Collection<@Valid YamlMCPResourceDescriptor> resources = new LinkedList<>();
    
    private Collection<@Valid YamlMCPResourceDescriptor> resourceTemplates = new LinkedList<>();
    
    private Collection<@Valid YamlMCPToolDescriptor> tools = new LinkedList<>();
    
    private Collection<@Valid YamlMCPPromptDescriptor> prompts = new LinkedList<>();
    
    private Collection<@Valid YamlMCPCompletionTargetDescriptor> completionTargets = new LinkedList<>();
    
    private Collection<@Valid YamlMCPResourceNavigationDescriptor> resourceNavigation = new LinkedList<>();
}
