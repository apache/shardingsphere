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

package org.apache.shardingsphere.mcp.api.tool.descriptor;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP tool value definition.
 */
@Getter
public final class MCPToolValueDefinition {
    
    private final Type type;
    
    private final String description;
    
    private final MCPToolValueDefinition itemDefinition;
    
    private final Collection<String> enumValues;
    
    private final Collection<MCPToolFieldDefinition> objectProperties;
    
    private final boolean additionalProperties;
    
    private final Object defaultValue;
    
    private final Integer minimumValue;
    
    private final Integer maximumValue;
    
    private final Collection<Object> examples;
    
    private final String pattern;
    
    @Builder
    private MCPToolValueDefinition(final Type type, final String description, final MCPToolValueDefinition itemDefinition, final Collection<String> enumValues,
                                   final Collection<MCPToolFieldDefinition> objectProperties, final Boolean additionalProperties, final Object defaultValue,
                                   final Integer minimumValue, final Integer maximumValue, final Collection<Object> examples, final String pattern) {
        this.type = type;
        this.description = description;
        this.itemDefinition = itemDefinition;
        this.enumValues = null == enumValues ? Collections.emptyList() : enumValues;
        this.objectProperties = null == objectProperties ? Collections.emptyList() : objectProperties;
        this.additionalProperties = null == additionalProperties || additionalProperties;
        this.defaultValue = defaultValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.examples = null == examples ? Collections.emptyList() : examples;
        this.pattern = null == pattern ? "" : pattern;
    }
    
    /**
     * Create string value definition.
     *
     * @param description value description
     * @return string value definition
     */
    public static MCPToolValueDefinition string(final String description) {
        return builder().type(Type.STRING).description(description).build();
    }
    
    /**
     * Create enum string value definition.
     *
     * @param description value description
     * @param enumValues enum values
     * @return enum string value definition
     */
    public static MCPToolValueDefinition stringEnum(final String description, final Collection<String> enumValues) {
        return builder().type(Type.STRING).description(description).enumValues(enumValues).build();
    }
    
    /**
     * Create integer value definition.
     *
     * @param description value description
     * @return integer value definition
     */
    public static MCPToolValueDefinition integer(final String description) {
        return builder().type(Type.INTEGER).description(description).build();
    }
    
    /**
     * Create array value definition.
     *
     * @param description value description
     * @param itemDefinition item value definition
     * @return array value definition
     */
    public static MCPToolValueDefinition array(final String description, final MCPToolValueDefinition itemDefinition) {
        return builder().type(Type.ARRAY).description(description).itemDefinition(itemDefinition).build();
    }
    
    /**
     * Create boolean value definition.
     *
     * @param description value description
     * @return boolean value definition
     */
    public static MCPToolValueDefinition bool(final String description) {
        return builder().type(Type.BOOLEAN).description(description).build();
    }
    
    /**
     * Create object value definition.
     *
     * @param description value description
     * @return object value definition
     */
    public static MCPToolValueDefinition object(final String description) {
        return builder().type(Type.OBJECT).description(description).build();
    }
    
    /**
     * Create object value definition.
     *
     * @param description value description
     * @param objectProperties object properties
     * @param additionalProperties whether additional properties are allowed
     * @return object value definition
     */
    public static MCPToolValueDefinition object(final String description, final Collection<MCPToolFieldDefinition> objectProperties, final boolean additionalProperties) {
        return builder().type(Type.OBJECT).description(description).objectProperties(objectProperties).additionalProperties(additionalProperties).build();
    }
    
    /**
     * To schema fragment.
     *
     * @return schema fragment
     */
    public Map<String, Object> toSchemaFragment() {
        return switch (type) {
            case STRING -> toScalarSchemaFragment("string");
            case INTEGER -> toScalarSchemaFragment("integer");
            case ARRAY -> toArraySchemaFragment();
            case BOOLEAN -> toScalarSchemaFragment("boolean");
            case OBJECT -> toObjectSchemaFragment();
        };
    }
    
    private Map<String, Object> toScalarSchemaFragment(final String type) {
        final Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("type", type);
        result.put("description", description);
        if (!enumValues.isEmpty()) {
            result.put("enum", enumValues);
        }
        appendSchemaAttributes(result);
        return result;
    }
    
    private Map<String, Object> toArraySchemaFragment() {
        final Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("type", "array");
        result.put("description", description);
        result.put("items", itemDefinition.toSchemaFragment());
        appendSchemaAttributes(result);
        return result;
    }
    
    private Map<String, Object> toObjectSchemaFragment() {
        final Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("description", description);
        if (objectProperties.isEmpty()) {
            result.put("additionalProperties", additionalProperties);
            appendSchemaAttributes(result);
            return result;
        }
        final Map<String, Object> properties = new LinkedHashMap<>(objectProperties.size(), 1F);
        final Collection<String> required = new ArrayList<>(objectProperties.size());
        for (final MCPToolFieldDefinition each : objectProperties) {
            properties.put(each.getName(), each.getValueDefinition().toSchemaFragment());
            if (each.isRequired()) {
                required.add(each.getName());
            }
        }
        result.put("properties", properties);
        result.put("required", required);
        result.put("additionalProperties", additionalProperties);
        appendSchemaAttributes(result);
        return result;
    }
    
    private void appendSchemaAttributes(final Map<String, Object> result) {
        if (null != defaultValue) {
            result.put("default", defaultValue);
        }
        if (null != minimumValue) {
            result.put("minimum", minimumValue);
        }
        if (null != maximumValue) {
            result.put("maximum", maximumValue);
        }
        if (!examples.isEmpty()) {
            result.put("examples", examples);
        }
        if (!pattern.isEmpty()) {
            result.put("pattern", pattern);
        }
    }
    
    /**
     * Tool value type.
     */
    public enum Type {
        
        STRING,
        
        INTEGER,
        
        ARRAY,
        
        BOOLEAN,
        
        OBJECT
    }
}
