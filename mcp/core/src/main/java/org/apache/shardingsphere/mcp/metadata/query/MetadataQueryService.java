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

package org.apache.shardingsphere.mcp.metadata.query;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityCatalog;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Query normalized metadata snapshots.
 */
public final class MetadataQueryService {
    
    private static final String UNSUPPORTED_INDEX_RESOURCE_MESSAGE = "Index resources are not supported for the current database.";
    
    /**
     * Query databases.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param predicate database filter
     * @return metadata query result
     */
    public MetadataQueryResult queryDatabases(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final Predicate<String> predicate) {
        List<MetadataObject> result = new LinkedList<>();
        for (String each : databaseMetadataSnapshots.getDatabaseTypes().keySet()) {
            if (predicate.test(each)) {
                result.add(new MetadataObject(each, "", MetadataObjectType.DATABASE, each, "", ""));
            }
        }
        return MetadataQueryResult.success(sortMetadataObjects(result));
    }
    
    /**
     * Query metadata objects for one logical database.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param databaseName logical database name
     * @param objectType metadata object type
     * @param predicate metadata object filter
     * @return metadata query result
     */
    public MetadataQueryResult queryMetadataObjects(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final String databaseName,
                                                    final MetadataObjectType objectType, final Predicate<MetadataObject> predicate) {
        Set<MetadataObjectType> supportedMetadataObjectTypes = getSupportedMetadataObjectTypes(databaseMetadataSnapshots, databaseName);
        if (MetadataObjectType.INDEX == objectType && !supportedMetadataObjectTypes.contains(MetadataObjectType.INDEX)) {
            return MetadataQueryResult.error(MCPErrorCode.UNSUPPORTED, UNSUPPORTED_INDEX_RESOURCE_MESSAGE);
        }
        if (!supportedMetadataObjectTypes.contains(objectType)) {
            return MetadataQueryResult.success(Collections.emptyList());
        }
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObject each : databaseMetadataSnapshots.getMetadataObjects()) {
            if (databaseName.equals(each.getDatabase()) && objectType == each.getObjectType() && predicate.test(each)) {
                result.add(each);
            }
        }
        return MetadataQueryResult.success(sortMetadataObjects(result));
    }
    
    private Set<MetadataObjectType> getSupportedMetadataObjectTypes(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final String databaseName) {
        DatabaseMetadataSnapshot snapshot = databaseMetadataSnapshots.findSnapshot(databaseName).orElseThrow(() -> new IllegalStateException("Database does not exist."));
        return DatabaseCapabilityCatalog.find(databaseName, snapshot.getDatabaseType(), snapshot.getDatabaseVersion())
                .map(DatabaseCapability::getSupportedMetadataObjectTypes).orElseGet(Collections::emptySet);
    }
    
    private List<MetadataObject> sortMetadataObjects(final Collection<MetadataObject> metadataObjects) {
        List<MetadataObject> result = new LinkedList<>(metadataObjects);
        result.sort(Comparator.comparing(MetadataObject::getDatabase)
                .thenComparing(MetadataObject::getSchema)
                .thenComparing(each -> each.getObjectType().name())
                .thenComparing(MetadataObject::getParentObjectName)
                .thenComparing(MetadataObject::getName));
        return result;
    }
}
