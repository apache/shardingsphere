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

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;

import java.util.List;
import java.util.Optional;

/**
 * Data node.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class DataNode {
    
    private static final String DELIMITER = ".";
    
    private static final String ASTERISK = "*";
    
    private final String dataSourceName;
    
    private final String schemaName;
    
    private final String tableName;
    
    /**
     * Constructs a data node with well-formatted string.
     *
     * @param dataNode string of data node. use {@code .} to split data source name and table name.
     */
    public DataNode(final String dataNode) {
        validateDataNodeFormat(dataNode);
        List<String> segments = Splitter.on(DELIMITER).splitToList(dataNode);
        boolean isIncludeSchema = 3 == segments.size();
        dataSourceName = segments.get(0);
        schemaName = isIncludeSchema ? segments.get(1) : null;
        tableName = segments.get(isIncludeSchema ? 2 : 1);
    }
    
    /**
     * Constructs a data node with well-formatted string.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param dataNode data node use {@code .} to split schema name and table name
     */
    public DataNode(final String databaseName, final DatabaseType databaseType, final String dataNode) {
        ShardingSpherePreconditions.checkState(dataNode.contains(DELIMITER), () -> new InvalidDataNodeFormatException(dataNode));
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        boolean containsSchema = dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable() && isValidDataNode(dataNode, 3);
        List<String> segments = Splitter.on(DELIMITER).limit(containsSchema ? 3 : 2).splitToList(dataNode);
        dataSourceName = segments.get(0);
        schemaName = getSchemaName(databaseName, dialectDatabaseMetaData, containsSchema, segments);
        tableName = containsSchema ? segments.get(2).toLowerCase() : segments.get(1).toLowerCase();
    }
    
    private String getSchemaName(final String databaseName, final DialectDatabaseMetaData dialectDatabaseMetaData, final boolean containsSchema, final List<String> segments) {
        return dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().map(optional -> containsSchema ? segments.get(1) : ASTERISK).orElse(databaseName);
    }
    
    private boolean isValidDataNode(final String dataNodeStr, final int tier) {
        if (hasInvalidDelimiterStructure(dataNodeStr)) {
            return false;
        }
        List<String> segments = Splitter.on(DELIMITER).splitToList(dataNodeStr);
        return isAnySegmentIsEmptyOrContainsOnlyWhitespace(tier, segments);
    }
    
    private boolean hasInvalidDelimiterStructure(final String dataNodeStr) {
        return !dataNodeStr.contains(DELIMITER) || hasLeadingOrTrailingDelimiter(dataNodeStr) || hasConsecutiveDelimiters(dataNodeStr) || hasWhitespaceAroundDelimiters(dataNodeStr);
    }
    
    private boolean hasLeadingOrTrailingDelimiter(final String dataNodeStr) {
        return dataNodeStr.startsWith(DELIMITER) || dataNodeStr.endsWith(DELIMITER);
    }
    
    private boolean hasConsecutiveDelimiters(final String dataNodeStr) {
        return dataNodeStr.contains(DELIMITER + DELIMITER);
    }
    
    private boolean hasWhitespaceAroundDelimiters(final String dataNodeStr) {
        return dataNodeStr.contains(" " + DELIMITER) || dataNodeStr.contains(DELIMITER + " ");
    }
    
    private boolean isAnySegmentIsEmptyOrContainsOnlyWhitespace(final int tier, final List<String> segments) {
        return segments.stream().noneMatch(each -> each.trim().isEmpty()) && tier == segments.size();
    }
    
    /**
     * Validates the data node format based on its structure.
     *
     * @param dataNode the data node string to validate
     * @throws InvalidDataNodeFormatException if the format is invalid
     */
    private void validateDataNodeFormat(final String dataNode) {
        ShardingSpherePreconditions.checkState(isValidDataNode(dataNode, 2) || isValidDataNode(dataNode, 3), () -> new InvalidDataNodeFormatException(dataNode));
    }
    
    /**
     * Format data node as string with schema.
     *
     * @return formatted data node
     */
    public String format() {
        return null == schemaName ? formatWithoutSchema() : formatWithSchema();
    }
    
    /**
     * Format data node as string.
     *
     * @param databaseType database type
     * @return formatted data node
     */
    public String format(final DatabaseType databaseType) {
        return shouldIncludeSchema(databaseType) ? formatWithSchema() : formatWithoutSchema();
    }
    
    private boolean shouldIncludeSchema(final DatabaseType databaseType) {
        return null != schemaName && new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().getDefaultSchema().isPresent();
    }
    
    private String formatWithSchema() {
        return String.join(DELIMITER, dataSourceName, schemaName, tableName);
    }
    
    private String formatWithoutSchema() {
        return String.join(DELIMITER, dataSourceName, tableName);
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
        return Objects.equal(Optional.ofNullable(dataSourceName).map(CaseInsensitiveString::of).orElse(null), Optional.ofNullable(dataNode.dataSourceName).map(CaseInsensitiveString::of).orElse(null))
                && Objects.equal(Optional.ofNullable(tableName).map(CaseInsensitiveString::of).orElse(null), Optional.ofNullable(dataNode.tableName).map(CaseInsensitiveString::of).orElse(null))
                && Objects.equal(Optional.ofNullable(schemaName).map(CaseInsensitiveString::of).orElse(null), Optional.ofNullable(dataNode.schemaName).map(CaseInsensitiveString::of).orElse(null));
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(Optional.ofNullable(dataSourceName).map(CaseInsensitiveString::of).orElse(null), Optional.ofNullable(tableName).map(CaseInsensitiveString::of).orElse(null),
                Optional.ofNullable(schemaName).map(CaseInsensitiveString::of).orElse(null));
    }
}
