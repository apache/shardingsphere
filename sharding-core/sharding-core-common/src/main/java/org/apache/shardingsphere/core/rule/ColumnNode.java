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

package org.apache.shardingsphere.core.rule;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;

import java.util.List;

/**
 * Column unit node.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ColumnNode {
    
    private static final String DELIMITER = ".";
    
    private final String tableName;
    
    private final String columnName;
    
    /**
     * Constructs a column node with well-formatted string.
     *
     * @param columnNode string of column node. use {@code .} to split table name and column name.
     */
    public ColumnNode(final String columnNode) {
        if (!isValidColumnNode(columnNode)) {
            throw new ShardingConfigurationException("Invalid format for actual column node: '%s'", columnNode);
        }
        List<String> segments = Splitter.on(DELIMITER).splitToList(columnNode);
        tableName = segments.get(0);
        columnName = segments.get(1);
    }
    
    private static boolean isValidColumnNode(final String columnNodeStr) {
        return columnNodeStr.contains(DELIMITER) && 2 == Splitter.on(DELIMITER).splitToList(columnNodeStr).size();
    }
    
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (null == object || getClass() != object.getClass()) {
            return false;
        }
        ColumnNode columnNode = (ColumnNode) object;
        return Objects.equal(this.columnName.toUpperCase(), columnNode.columnName.toUpperCase())
            && Objects.equal(this.tableName.toUpperCase(), columnNode.tableName.toUpperCase());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(columnName.toUpperCase(), tableName.toUpperCase());
    }
}
