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
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;

import java.util.Collection;
import java.util.LinkedList;

/**
 * YAML MCP tool value definition.
 */
@Getter
@Setter
public final class YamlMCPToolValueDefinition {

    private Type type;

    private String description;

    private YamlMCPToolValueDefinition itemDefinition;

    private Collection<String> enumValues = new LinkedList<>();

    private Collection<YamlMCPToolFieldDefinition> objectProperties = new LinkedList<>();

    private Boolean additionalProperties;

    private Object defaultValue;

    private Integer minimumValue;

    private Integer maximumValue;

    private Collection<Object> examples = new LinkedList<>();

    private String pattern;
}
