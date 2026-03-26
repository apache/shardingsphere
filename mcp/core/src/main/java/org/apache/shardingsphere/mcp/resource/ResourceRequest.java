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

package org.apache.shardingsphere.mcp.resource;

import lombok.Getter;

/**
 * Request contract for one metadata resource load.
 */
@Getter
public final class ResourceRequest {
    
    private final String database;
    
    private final String schema;
    
    private final MetadataObjectType objectType;
    
    private final String objectName;
    
    private final String parentObjectType;
    
    private final String parentObjectName;
    
    /**
     * Construct a resource request.
     *
     * @param database logical database name or empty string
     * @param schema schema name or empty string
     * @param objectType target object type
     * @param objectName target object name or empty string
     * @param parentObjectType parent object type name or empty string
     * @param parentObjectName parent object name or empty string
     */
    public ResourceRequest(final String database, final String schema, final MetadataObjectType objectType,
                           final String objectName, final String parentObjectType, final String parentObjectName) {
        this.database = database;
        this.schema = schema;
        this.objectType = objectType;
        this.objectName = objectName;
        this.parentObjectType = parentObjectType;
        this.parentObjectName = parentObjectName;
    }
}
