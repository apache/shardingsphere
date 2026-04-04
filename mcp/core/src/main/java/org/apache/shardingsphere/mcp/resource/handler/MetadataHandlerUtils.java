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

package org.apache.shardingsphere.mcp.resource.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Metadata handler utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetadataHandlerUtils {
    
    /**
     * Create databases result.
     * 
     * @param runtimeContext runtime context
     * @param predicate predicate
     * @return databases result
     */
    public static MCPResourceResponse createDatabasesResult(final MCPRuntimeContext runtimeContext, final Predicate<String> predicate) {
        return createSortedMetadataResult(runtimeContext.getDatabaseMetadataSnapshots().getDatabaseTypes().keySet().stream()
                .filter(predicate).map(each -> new MetadataObject(each, "", MetadataObjectType.DATABASE, each, "", "")).collect(Collectors.toList()));
    }
    
    /**
     * Create metadata result.
     * 
     * @param runtimeContext runtime context
     * @param databaseName database name
     * @param objectType object type
     * @param predicate predicate
     * @return metadata result
     */
    public static MCPResourceResponse createMetadataResult(final MCPRuntimeContext runtimeContext, final String databaseName,
                                                           final MetadataObjectType objectType, final Predicate<MetadataObject> predicate) {
        Set<MetadataObjectType> supportedMetadataObjectTypes = getSupportedMetadataObjectTypes(runtimeContext, databaseName);
        if (MetadataObjectType.INDEX == objectType && !supportedMetadataObjectTypes.contains(MetadataObjectType.INDEX)) {
            return new MCPErrorResponse(MCPErrorCode.UNSUPPORTED, "Index resources are not supported for the current database.");
        }
        if (!supportedMetadataObjectTypes.contains(objectType)) {
            return createSortedMetadataResult(Collections.emptyList());
        }
        return createSortedMetadataResult(runtimeContext.getDatabaseMetadataSnapshots().getMetadataObjects().stream()
                .filter(each -> databaseName.equals(each.getDatabase()) && objectType == each.getObjectType() && predicate.test(each)).collect(Collectors.toList()));
    }
    
    private static Set<MetadataObjectType> getSupportedMetadataObjectTypes(final MCPRuntimeContext runtimeContext, final String databaseName) {
        runtimeContext.getDatabaseMetadataSnapshots().findDatabaseType(databaseName).orElseThrow(() -> new IllegalStateException("Database does not exist."));
        return runtimeContext.getCapabilityBuilder().buildDatabaseCapability(databaseName).map(DatabaseCapability::getSupportedMetadataObjectTypes).orElseGet(Collections::emptySet);
    }
    
    private static MCPResourceResponse createSortedMetadataResult(final Collection<MetadataObject> metadataObjects) {
        return new MCPMetadataResponse(sortMetadataObjects(metadataObjects));
    }
    
    private static List<MetadataObject> sortMetadataObjects(final Collection<MetadataObject> metadataObjects) {
        List<MetadataObject> result = new LinkedList<>(metadataObjects);
        result.sort(Comparator.comparing(MetadataObject::getDatabase)
                .thenComparing(MetadataObject::getSchema)
                .thenComparing(each -> each.getObjectType().name())
                .thenComparing(MetadataObject::getParentObjectName)
                .thenComparing(MetadataObject::getName));
        return result;
    }
}
