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

package org.apache.shardingsphere.scaling.core.execute.executor.record;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.scaling.core.job.position.Position;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Data record.
 */
@Setter
@Getter
@EqualsAndHashCode(of = {"tableName", "primaryKeyValue"}, callSuper = false)
@ToString
public final class DataRecord extends Record {
    
    private final List<Column> columns;
    
    private final List<Object> primaryKeyValue = new LinkedList<>();
    
    private final List<Object> oldPrimaryKeyValues = new ArrayList<>();
    
    private String type;
    
    private String tableName;
    
    public DataRecord(final Position<?> position, final int columnCount) {
        super(position);
        columns = new ArrayList<>(columnCount);
    }
    
    /**
     * Add a column to record.
     *
     * @param data column
     */
    public void addColumn(final Column data) {
        columns.add(data);
        if (data.isPrimaryKey()) {
            primaryKeyValue.add(data.getValue());
            oldPrimaryKeyValues.add(data.getOldValue());
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
     * Get key.
     *
     * @return key
     */
    public Key getKey() {
        return new Key(tableName, primaryKeyValue);
    }
    
    /**
     * Get old key.
     *
     * @return key
     */
    public Key getOldKey() {
        return new Key(tableName, oldPrimaryKeyValues);
    }
    
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class Key {
        
        private final String tableName;
        
        private final List<Object> primaryKeyValues;
    }
}
