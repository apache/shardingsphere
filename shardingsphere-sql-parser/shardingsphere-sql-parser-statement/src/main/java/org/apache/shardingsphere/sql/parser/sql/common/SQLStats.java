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

package org.apache.shardingsphere.sql.parser.sql.common;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public final class SQLStats {
    
    private final Map<String, SimpleTableSegment> tables = new LinkedHashMap<>();
    
    private final Map<Integer, ColumnSegment> columns = new LinkedHashMap<>();
    
    /**
     * add table to tables.
     * @param tableSegment SimpleTableSegment.
     */
    public void addTable(final SimpleTableSegment tableSegment) {
        if (!tables.containsKey(tableSegment.getTableName().getIdentifier().getValue())) {
            tables.put(tableSegment.getTableName().getIdentifier().getValue(), tableSegment);
        }
    }
    
    /**
     * add column to columns.
     * @param column ColumnSegment.
     */
    public void addColumn(final ColumnSegment column) {
        int columnHashcode = column.hashCode();
        if (!columns.containsKey(columnHashcode)) {
            columns.put(columnHashcode, column);
        }
    }
}
