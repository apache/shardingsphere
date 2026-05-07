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

package org.apache.shardingsphere.mcp.api.resource.descriptor;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * MCP resource descriptor.
 */
@Getter
public final class MCPResourceDescriptor {

    private static final String RESOURCE_KIND_KEY = "resourceKind";

    private static final String LEGACY_KIND_KEY = "kind";

    private static final String OBJECT_SCOPE_KEY = "objectScope";

    private static final String FEATURE_KEY = "feature";

    private static final String RELATED_TOOLS_KEY = "relatedTools";

    private static final String RELATED_RESOURCES_KEY = "relatedResources";

    private static final String USE_BEFORE_KEY = "useBefore";

    private final String uriTemplate;

    private final String name;

    private final String title;

    private final String description;

    private final String mimeType;

    private final List<MCPResourceParameterDescriptor> parameters;

    private final MCPResourceAnnotations annotations;

    private final String resourceKind;

    private final String objectScope;

    private final String feature;

    private final List<String> relatedTools;

    private final List<String> relatedResources;

    private final List<String> useBefore;

    private final Map<String, Object> meta;

    public MCPResourceDescriptor(final String uriTemplate, final String name, final String title, final String description, final String mimeType) {
        this(uriTemplate, name, title, description, mimeType, Collections.emptyList(), MCPResourceAnnotations.EMPTY, Collections.emptyMap());
    }

    public MCPResourceDescriptor(final String uriTemplate, final String name, final String title, final String description, final String mimeType,
                                 final List<MCPResourceParameterDescriptor> parameters, final MCPResourceAnnotations annotations, final Map<String, Object> meta) {
        this(uriTemplate, name, title, description, mimeType, parameters, annotations, getStringValue(meta, RESOURCE_KIND_KEY, LEGACY_KIND_KEY),
                getStringValue(meta, OBJECT_SCOPE_KEY), getStringValue(meta, FEATURE_KEY), getStringList(meta, RELATED_TOOLS_KEY), getStringList(meta, RELATED_RESOURCES_KEY),
                getStringList(meta, USE_BEFORE_KEY), cleanMeta(meta));
    }

    public MCPResourceDescriptor(final String uriTemplate, final String name, final String title, final String description, final String mimeType,
                                 final List<MCPResourceParameterDescriptor> parameters, final MCPResourceAnnotations annotations, final String resourceKind,
                                 final String objectScope, final String feature, final List<String> relatedTools, final List<String> relatedResources,
                                 final List<String> useBefore, final Map<String, Object> meta) {
        this.uriTemplate = uriTemplate;
        this.name = name;
        this.title = title;
        this.description = description;
        this.mimeType = mimeType;
        this.parameters = null == parameters ? Collections.emptyList() : parameters;
        this.annotations = null == annotations ? MCPResourceAnnotations.EMPTY : annotations;
        this.resourceKind = resourceKind;
        this.objectScope = objectScope;
        this.feature = feature;
        this.relatedTools = null == relatedTools ? Collections.emptyList() : relatedTools;
        this.relatedResources = null == relatedResources ? Collections.emptyList() : relatedResources;
        this.useBefore = null == useBefore ? Collections.emptyList() : useBefore;
        this.meta = cleanMeta(meta);
    }

    /**
     * Get URI pattern for compatibility with the resource matcher.
     *
     * @return URI pattern
     */
    public String getUriPattern() {
        return uriTemplate;
    }

    /**
     * Judge whether the resource is a URI template.
     *
     * @return true if the resource is a URI template
     */
    public boolean isTemplated() {
        return MCPUriTemplateUtils.isTemplated(uriTemplate);
    }

    private static String getStringValue(final Map<String, Object> meta, final String key) {
        Object value = null == meta ? null : meta.get(key);
        return null == value ? null : value.toString();
    }

    private static String getStringValue(final Map<String, Object> meta, final String key, final String fallbackKey) {
        String result = getStringValue(meta, key);
        return null == result ? getStringValue(meta, fallbackKey) : result;
    }

    private static List<String> getStringList(final Map<String, Object> meta, final String key) {
        Object value = null == meta ? null : meta.get(key);
        if (!(value instanceof Iterable)) {
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        for (Object each : (Iterable<?>) value) {
            result.add(each.toString());
        }
        return result;
    }

    private static Map<String, Object> cleanMeta(final Map<String, Object> meta) {
        if (null == meta) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>(meta);
        result.remove(RESOURCE_KIND_KEY);
        result.remove(LEGACY_KIND_KEY);
        result.remove(OBJECT_SCOPE_KEY);
        result.remove(FEATURE_KEY);
        result.remove(RELATED_TOOLS_KEY);
        result.remove(RELATED_RESOURCES_KEY);
        result.remove(USE_BEFORE_KEY);
        return result;
    }
}
