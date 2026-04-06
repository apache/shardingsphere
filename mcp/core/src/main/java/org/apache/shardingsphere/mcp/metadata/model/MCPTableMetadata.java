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
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP table metadata.
 */
@RequiredArgsConstructor
@Getter
public final class MCPTableMetadata {
    
    private final String database;
    
    private final String schema;
    
    private final String table;
    
    private final List<MCPColumnMetadata> columns;
    
    private final List<MCPIndexMetadata> indexes;
    
    /**
     * Create summary.
     *
     * @return table metadata summary
     */
    public MCPTableMetadata createSummary() {
        return new MCPTableMetadata(database, schema, table, Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Create detail.
     *
     * @return table metadata detail
     */
    public MCPTableMetadata createDetail() {
        return new MCPTableMetadata(database, schema, table,
                columns.stream().sorted(Comparator.comparing(MCPColumnMetadata::getColumn)).collect(Collectors.toList()),
                indexes.stream().sorted(Comparator.comparing(MCPIndexMetadata::getIndex)).collect(Collectors.toList()));
    }
}
