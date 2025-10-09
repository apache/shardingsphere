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

package org.apache.shardingsphere.data.pipeline.core.ingest.record;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data record.
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"schemaName", "tableName"}, callSuper = false)
@ToString
public final class DataRecord extends Record {
    
    private final PipelineSQLOperationType type;
    
    private final String schemaName;
    
    private final String tableName;
    
    private final List<Column> columns;
    
    private final Map<String, Column> columnMap;
    
    private final Collection<Object> uniqueKeyValue = new LinkedList<>();
    
    private final Collection<Object> oldUniqueKeyValues = new LinkedList<>();
    
    private String actualTableName;
    
    private Long csn;
    
    public DataRecord(final PipelineSQLOperationType type, final String tableName, final IngestPosition position, final int columnCount) {
        this(type, null, tableName, position, columnCount);
    }
    
    public DataRecord(final PipelineSQLOperationType type, final String schemaName, final String tableName, final IngestPosition position, final int columnCount) {
        super(position);
        this.type = type;
        this.schemaName = schemaName;
        this.tableName = tableName;
        columns = new ArrayList<>(columnCount);
        columnMap = new CaseInsensitiveMap<>(columnCount, 1F);
    }
    
    /**
     * Add a column to record.
     *
     * @param data column
     */
    public void addColumn(final Column data) {
        columns.add(data);
        columnMap.put(data.getName(), data);
        if (data.isUniqueKey()) {
            uniqueKeyValue.add(data.getValue());
            oldUniqueKeyValues.add(data.getOldValue());
        }
    }
    
    /**
     * Return column count.
     *
     * @return count
     */
    public int getColumnCount() {
        return columns.size();
    }
    
    /**
     * Get column by index.
     *
     * @param index of column
     * @return column
     */
    public Column getColumn(final int index) {
        return columns.get(index);
    }
    
    /**
     * Get column by name.
     *
     * @param name column name
     * @return column
     */
    public Column getColumn(final String name) {
        return columnMap.get(name);
    }
    
    /**
     * Get key.
     *
     * @return key
     */
    public Key getKey() {
        return new Key(tableName, uniqueKeyValue);
    }
    
    /**
     * Get old key.
     *
     * @return key
     */
    public Key getOldKey() {
        return new Key(tableName, oldUniqueKeyValues);
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class Key {
        
        private final String tableName;
        
        private final Collection<Object> uniqueKeyValues;
    }
}
