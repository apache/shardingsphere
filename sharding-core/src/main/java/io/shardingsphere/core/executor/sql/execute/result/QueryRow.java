/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor.sql.execute.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.LinkedList;
import java.util.List;

/**
 * Query row.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class QueryRow {
    
    private final List<Object> rowData;
    
    private final List<Integer> distinctColumnIndexes = new LinkedList<>();
    
    public QueryRow(final List<Object> rowData, final List<Integer> distinctColumnIndexes) {
        this.rowData = rowData;
        this.distinctColumnIndexes.addAll(distinctColumnIndexes);
    }
    
    private boolean isEqual(final QueryRow queryRow) {
        if (-1 == distinctColumnIndex) {
            return rowData.equals(queryRow.getRowData());
        }
        return rowData.get(distinctColumnIndex).equals(queryRow.getDistinctColumnIndex());
    }
    
    /**
     * Get column value.
     *
     * @param columnIndex column index
     * @return column value
     */
    public Object getColumnValue(final int columnIndex) {
        return rowData.get(columnIndex - 1);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }
        return isEqual((QueryRow) obj);
    }
    
    @Override
    public int hashCode() {
        if (-1 == distinctColumnIndex) {
            return rowData.hashCode();
        }
        return rowData.get(distinctColumnIndex).hashCode();
    }
}
