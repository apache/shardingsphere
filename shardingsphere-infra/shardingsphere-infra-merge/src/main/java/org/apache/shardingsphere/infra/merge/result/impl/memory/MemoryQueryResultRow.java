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

package org.apache.shardingsphere.infra.merge.result.impl.memory;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;

import java.sql.SQLException;

/**
 * Memory query result row.
 */
@RequiredArgsConstructor
public final class MemoryQueryResultRow {
    
    private final Object[] data;
    
    public MemoryQueryResultRow(final QueryResult queryResult) throws SQLException {
        data = load(queryResult);
    }
    
    private Object[] load(final QueryResult queryResult) throws SQLException {
        int columnCount = queryResult.getColumnCount();
        Object[] result = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            result[i] = queryResult.getValue(i + 1, Object.class);
        }
        return result;
    }
    
    /**
     * Get data from cell.
     * 
     * @param columnIndex column index
     * @return data from cell
     */
    public Object getCell(final int columnIndex) {
        Preconditions.checkArgument(columnIndex > 0 && columnIndex < data.length + 1);
        return data[columnIndex - 1];
    }
    
    /**
     * Set data for cell.
     *
     * @param columnIndex column index
     * @param value data for cell
     */
    public void setCell(final int columnIndex, final Object value) {
        Preconditions.checkArgument(columnIndex > 0 && columnIndex < data.length + 1);
        data[columnIndex - 1] = value;
    }
}
