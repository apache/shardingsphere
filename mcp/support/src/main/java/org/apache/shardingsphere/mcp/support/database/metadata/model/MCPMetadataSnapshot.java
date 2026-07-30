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

package org.apache.shardingsphere.mcp.support.database.metadata.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * MCP metadata snapshot.
 */
@RequiredArgsConstructor
public final class MCPMetadataSnapshot {
    
    @Getter
    private final Collection<ShardingSphereSchema> schemas;
    
    private final Map<String, Collection<MCPSequenceMetadata>> sequences;
    
    /**
     * Get sequence metadata in a schema.
     *
     * @param schemaName schema name
     * @return sequence metadata
     */
    public Collection<MCPSequenceMetadata> getSequences(final String schemaName) {
        return sequences.getOrDefault(schemaName, Collections.emptyList());
    }
}
