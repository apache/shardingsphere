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

package org.apache.shardingsphere.mcp.tool;

import lombok.Getter;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;

import java.util.Set;

/**
 * Tool request contract for metadata discovery.
 */
@Getter
public final class ToolRequest {
    
    private final String toolName;
    
    private final String database;
    
    private final String schema;
    
    private final String objectName;
    
    private final String parentObjectType;
    
    private final String query;
    
    private final Set<MetadataObjectType> objectTypes;
    
    private final int pageSize;
    
    private final String pageToken;
    
    /**
     * Construct a metadata tool request.
     *
     * @param toolName tool identifier
     * @param database logical database name or empty string
     * @param schema schema name or empty string
     * @param objectName object name or parent object name depending on the tool
     * @param parentObjectType parent object type name or empty string
     * @param query search query or empty string
     * @param objectTypes object type filter for search or empty set
     * @param pageSize requested page size
     * @param pageToken requested page token or empty string
     */
    public ToolRequest(final String toolName, final String database, final String schema, final String objectName,
                       final String parentObjectType, final String query, final Set<MetadataObjectType> objectTypes,
                       final int pageSize, final String pageToken) {
        this.toolName = toolName;
        this.database = database;
        this.schema = schema;
        this.objectName = objectName;
        this.parentObjectType = parentObjectType;
        this.query = query;
        this.objectTypes = objectTypes;
        this.pageSize = pageSize;
        this.pageToken = pageToken;
    }
}
