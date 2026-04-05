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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.exception.MCPNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Query normalized metadata snapshots.
 */
@RequiredArgsConstructor
public final class MetadataQueryService {
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots;
    
    /**
     * Query databases.
     *
     * @return metadata objects
     */
    public List<MetadataObject> queryDatabases() {
        return sortMetadataObjects(databaseMetadataSnapshots.getDatabaseSnapshots().keySet().stream().map(this::createDatabaseMetadataObject).collect(Collectors.toList()));
    }
    
    /**
     * Query database.
     *
     * @param databaseName logical database name
     * @return metadata object
     */
    public Optional<MetadataObject> queryDatabase(final String databaseName) {
        return databaseMetadataSnapshots.findSnapshot(databaseName).isPresent() ? Optional.of(createDatabaseMetadataObject(databaseName)) : Optional.empty();
    }
    
    private MetadataObject createDatabaseMetadataObject(final String databaseName) {
        return new MetadataObject(databaseName, "", MetadataObjectType.DATABASE, databaseName, "", "");
    }
    
    /**
     * Query metadata objects for one logical database.
     *
     * @param databaseName logical database name
     * @param objectType metadata object type
     * @param queryCondition metadata object query condition
     * @return metadata query result
     * @throws MCPNotFoundException when the logical database does not exist
     * @throws MCPUnsupportedException when the metadata object type is unsupported by the database
     */
    public List<MetadataObject> queryMetadataObjects(final String databaseName, final MetadataObjectType objectType, final MetadataObjectQueryCondition queryCondition) {
        Set<MetadataObjectType> supportedMetadataObjectTypes = getSupportedMetadataObjectTypes(databaseName);
        if (MetadataObjectType.INDEX == objectType && !supportedMetadataObjectTypes.contains(MetadataObjectType.INDEX)) {
            throw new MCPUnsupportedException("Index resources are not supported for the current database.");
        }
        if (!supportedMetadataObjectTypes.contains(objectType)) {
            return Collections.emptyList();
        }
        List<MetadataObject> result = new LinkedList<>();
        for (MetadataObject each : databaseMetadataSnapshots.getMetadataObjects()) {
            if (databaseName.equals(each.getDatabase()) && objectType == each.getObjectType() && queryCondition.matches(each)) {
                result.add(each);
            }
        }
        return sortMetadataObjects(result);
    }
    
    /**
     * Judge whether the metadata object type is supported for the database.
     *
     * @param databaseName database name
     * @param objectType metadata object type
     * @return whether supported or not
     */
    public boolean isSupportedMetadataObjectType(final String databaseName, final MetadataObjectType objectType) {
        return getSupportedMetadataObjectTypes(databaseName).contains(objectType);
    }
    
    private Set<MetadataObjectType> getSupportedMetadataObjectTypes(final String databaseName) {
        Optional<MCPDatabaseCapability> databaseCapability = new MCPDatabaseCapabilityProvider(databaseMetadataSnapshots).provide(databaseName);
        return databaseCapability.isPresent() ? databaseCapability.get().getSupportedMetadataObjectTypes() : Collections.emptySet();
    }
    
    private List<MetadataObject> sortMetadataObjects(final Collection<MetadataObject> metadataObjects) {
        List<MetadataObject> result = new LinkedList<>(metadataObjects);
        result.sort(this::compareMetadataObjects);
        return result;
    }
    
    private int compareMetadataObjects(final MetadataObject left, final MetadataObject right) {
        int compareResult = left.getDatabase().compareTo(right.getDatabase());
        if (0 != compareResult) {
            return compareResult;
        }
        compareResult = left.getSchema().compareTo(right.getSchema());
        if (0 != compareResult) {
            return compareResult;
        }
        compareResult = left.getObjectType().name().compareTo(right.getObjectType().name());
        if (0 != compareResult) {
            return compareResult;
        }
        compareResult = left.getParentObjectName().compareTo(right.getParentObjectName());
        if (0 != compareResult) {
            return compareResult;
        }
        return left.getName().compareTo(right.getName());
    }
}
