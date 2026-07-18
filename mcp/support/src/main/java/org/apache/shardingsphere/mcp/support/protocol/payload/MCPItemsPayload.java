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

package org.apache.shardingsphere.mcp.support.protocol.payload;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ShardingSphere application item-list payload.
 *
 * <p>Payload pagination fields are application-level fields, not MCP list cursor fields.</p>
 */
public final class MCPItemsPayload implements MCPSuccessPayload {
    
    private static final String CONTINUATION_MODE = "continuation_mode";
    
    private static final String CONTINUATION_MODE_NONE = "none";
    
    private static final String CONTINUATION_MODE_PAGINATION = "pagination";
    
    private final List<?> items;
    
    private final String nextPageToken;
    
    private final Map<String, Object> navigation;
    
    private final String responseMode;
    
    public MCPItemsPayload(final List<?> items) {
        this(items, "");
    }
    
    public MCPItemsPayload(final List<?> items, final String nextPageToken) {
        this(items, nextPageToken, Collections.emptyMap(), MCPResponseMode.LIST);
    }
    
    public MCPItemsPayload(final List<?> items, final Map<String, Object> navigation) {
        this(items, "", navigation, MCPResponseMode.LIST);
    }
    
    public MCPItemsPayload(final List<?> items, final String nextPageToken, final Map<String, Object> navigation, final String responseMode) {
        this.items = null == items ? Collections.emptyList() : items;
        this.nextPageToken = nextPageToken;
        this.navigation = null == navigation ? Collections.emptyMap() : navigation;
        this.responseMode = null == responseMode || responseMode.isEmpty() ? MCPResponseMode.LIST : responseMode;
    }
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(items.size() + navigation.size() + 4, 1F);
        result.put("response_mode", responseMode);
        result.put(MCPPayloadFieldNames.ITEMS, items);
        result.put("count", items.size());
        if (null != nextPageToken && !nextPageToken.isEmpty()) {
            result.put("next_page_token", nextPageToken);
        }
        result.put(CONTINUATION_MODE, resolveContinuationMode());
        result.putAll(navigation);
        return result;
    }
    
    private String resolveContinuationMode() {
        Object value = navigation.get(CONTINUATION_MODE);
        if (value instanceof String && !((String) value).isEmpty()) {
            return value.toString();
        }
        return null == nextPageToken || nextPageToken.isEmpty() ? CONTINUATION_MODE_NONE : CONTINUATION_MODE_PAGINATION;
    }
}
