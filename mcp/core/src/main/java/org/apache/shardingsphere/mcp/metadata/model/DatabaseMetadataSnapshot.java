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

package org.apache.shardingsphere.mcp.metadata.model;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Database metadata snapshot.
 */
@Getter
public final class DatabaseMetadataSnapshot {
    
    private final String databaseType;
    
    private final String databaseVersion;
    
    private final List<MetadataObject> metadataObjects;
    
    private final List<SchemaMetadata> schemas;
    
    public DatabaseMetadataSnapshot(final String databaseType, final String databaseVersion, final Collection<MetadataObject> metadataObjects) {
        this.databaseType = databaseType;
        this.databaseVersion = databaseVersion;
        this.metadataObjects = new LinkedList<>(metadataObjects);
        schemas = MetadataHierarchyBuilder.buildSchemas(metadataObjects);
    }
}
