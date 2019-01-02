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

package io.shardingsphere.core.executor.sql.execute.row;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
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
    
    private final List<Integer> distinctColumnIndexes;
    
    public QueryRow(final List<Object> rowData) {
        this(rowData, Collections.<Integer>emptyList());
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
        return this == obj || null != obj && getClass() == obj.getClass() && isEqual((QueryRow) obj);
    }
    
    private boolean isEqual(final QueryRow queryRow) {
        if (distinctColumnIndexes.isEmpty()) {
            return rowData.equals(queryRow.getRowData());
        }
        return distinctColumnIndexes.equals(queryRow.getDistinctColumnIndexes()) && isEqualPartly(queryRow);
    }
    
    private boolean isEqualPartly(final QueryRow queryRow) {
        for (int i = 0; i < distinctColumnIndexes.size(); i++) {
            if (!rowData.get(i).equals(queryRow.getRowData().get(i))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return distinctColumnIndexes.isEmpty() ? rowData.hashCode() : Lists.transform(distinctColumnIndexes, new Function<Integer, Object>() {
    
            @Override
            public Object apply(final Integer input) {
                return rowData.get(input - 1);
            }
        }).hashCode();
    }
}
