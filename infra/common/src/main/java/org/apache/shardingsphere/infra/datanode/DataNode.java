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

package org.apache.shardingsphere.infra.datanode;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;

import java.util.List;

/**
 * Data node.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class DataNode {
    
    private static final String DELIMITER = ".";
    
    private static final String ASTERISK = "*";
    
    private final String dataSourceName;
    
    private final String tableName;
    
    // TODO add final for schemaName
    private String schemaName;
    
    /**
     * Constructs a data node with well-formatted string.
     *
     * @param dataNode string of data node. use {@code .} to split data source name and table name.
     */
    public DataNode(final String dataNode) {
        // Validate data node format first
        validateDataNodeFormat(dataNode);
        
        // Split only once
        List<String> segments = Splitter.on(DELIMITER).splitToList(dataNode);
        
        // Determine if instance is included and set fields accordingly
        boolean isIncludeInstance = segments.size() == 3;
        dataSourceName = isIncludeInstance ? segments.get(0) + DELIMITER + segments.get(1) : segments.get(0);
        tableName = segments.get(isIncludeInstance ? 2 : 1);
    }
    
    /**
     * Constructs a data node with well-formatted string.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param dataNode string of data node. use {@code .} to split data source name and table name
     */
    public DataNode(final String databaseName, final DatabaseType databaseType, final String dataNode) {
        ShardingSpherePreconditions.checkState(dataNode.contains(DELIMITER), () -> new InvalidDataNodeFormatException(dataNode));
        boolean containsSchema = isSchemaAvailable(databaseType) && isValidDataNode(dataNode, 3);
        List<String> segments = Splitter.on(DELIMITER).limit(containsSchema ? 3 : 2).splitToList(dataNode);
        dataSourceName = segments.get(0);
        schemaName = getSchemaName(databaseName, databaseType, containsSchema, segments);
        tableName = containsSchema ? segments.get(2).toLowerCase() : segments.get(1).toLowerCase();
    }
    
    private boolean isSchemaAvailable(final DatabaseType databaseType) {
        return new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable();
    }
    
    private String getSchemaName(final String databaseName, final DatabaseType databaseType, final boolean containsSchema, final List<String> segments) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
            return containsSchema ? segments.get(1) : ASTERISK;
        }
        return databaseName;
    }
    
    private boolean isValidDataNode(final String dataNodeStr, final Integer tier) {
        if (!dataNodeStr.contains(DELIMITER)) {
            return false;
        }
        
        // Check for leading or trailing delimiter
        if (dataNodeStr.startsWith(DELIMITER) || dataNodeStr.endsWith(DELIMITER)) {
            return false;
        }
        
        // Check for consecutive delimiters (which would create empty segments)
        if (dataNodeStr.contains(DELIMITER + DELIMITER)) {
            return false;
        }
        
        // Check for whitespace around delimiters
        if (dataNodeStr.contains(" " + DELIMITER) || dataNodeStr.contains(DELIMITER + " ")) {
            return false;
        }
        
        List<String> segments = Splitter.on(DELIMITER).splitToList(dataNodeStr);
        
        // Check if any segment is empty or contains only whitespace
        for (String segment : segments) {
            if (segment.trim().isEmpty()) {
                return false;
            }
        }
        
        return tier == segments.size();
    }
    
    /**
     * Validates the data node format based on its structure.
     *
     * @param dataNode the data node string to validate
     * @throws InvalidDataNodeFormatException if the format is invalid
     */
    private void validateDataNodeFormat(final String dataNode) {
        // Check if it's a valid 2-segment or 3-segment format
        if (!isValidDataNode(dataNode, 2) && !isValidDataNode(dataNode, 3)) {
            throw new InvalidDataNodeFormatException(dataNode);
        }
    }
    
    /**
     * Format data node as string with schema.
     *
     * @return formatted data node
     */
    public String format() {
        return null == schemaName ? String.join(DELIMITER, dataSourceName, tableName) : String.join(DELIMITER, dataSourceName, schemaName, tableName);
    }
    
    /**
     * Format data node as string.
     *
     * @param databaseType database type
     * @return formatted data node
     */
    public String format(final DatabaseType databaseType) {
        return null != schemaName && new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().getDefaultSchema().isPresent()
                ? String.join(DELIMITER, dataSourceName, schemaName, tableName)
                : String.join(DELIMITER, dataSourceName, tableName);
    }
    
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (null == object || getClass() != object.getClass()) {
            return false;
        }
        DataNode dataNode = (DataNode) object;
        return Objects.equal(dataSourceName.toUpperCase(), dataNode.dataSourceName.toUpperCase())
                && Objects.equal(tableName.toUpperCase(), dataNode.tableName.toUpperCase())
                && Objects.equal(null == schemaName ? null : schemaName.toUpperCase(), null == dataNode.schemaName ? null : dataNode.schemaName.toUpperCase());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(dataSourceName.toUpperCase(), tableName.toUpperCase(), null == schemaName ? null : schemaName.toUpperCase());
    }
}
