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
import org.apache.shardingsphere.infra.exception.InvalidDataNodesFormatException;

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
        // TODO remove duplicated splitting?
        boolean isIncludeInstance = isActualDataNodesIncludedDataSourceInstance(dataNode);
        if (!isIncludeInstance && !isValidDataNode(dataNode, 2)) {
            throw new InvalidDataNodesFormatException(dataNode);
        }
        if (isIncludeInstance && !isValidDataNode(dataNode, 3)) {
            throw new InvalidDataNodesFormatException(dataNode);
        }
        List<String> segments = Splitter.on(DELIMITER).splitToList(dataNode);
        dataSourceName = isIncludeInstance ? segments.get(0) + DELIMITER + segments.get(1) : segments.get(0);
        tableName = segments.get(isIncludeInstance ? 2 : 1);
    }
    
    private static boolean isValidDataNode(final String dataNodeStr, final Integer tier) {
        return dataNodeStr.contains(DELIMITER) && tier == Splitter.on(DELIMITER).omitEmptyStrings().splitToList(dataNodeStr).size();
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
    
    /**
     * Format data node as string.
     *
     * @return formatted data node
     */
    public String format() {
        return dataSourceName + DELIMITER + tableName;
    }
    
    /**
     * Get formatted text length.
     *
     * @return formatted text length
     */
    public int getFormattedTextLength() {
        return dataSourceName.length() + DELIMITER.length() + tableName.length();
    }
    
    /**
     * Is Actual data nodes three tier structure.
     *
     * @param actualDataNodes data source map
     * @return boolean
     */
    public static boolean isActualDataNodesIncludedDataSourceInstance(final String actualDataNodes) {
        return isValidDataNode(actualDataNodes, 3);
    }
}
