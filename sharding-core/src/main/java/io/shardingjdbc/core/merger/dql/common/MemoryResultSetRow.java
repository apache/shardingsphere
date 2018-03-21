/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.merger.dql.common;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.merger.QueryResult;

import java.sql.SQLException;

/**
 * 内存数据行对象.
 * 
 * @author zhangliang
 */
public class MemoryResultSetRow {
    
    private final Object[] data;
    
    public MemoryResultSetRow(final QueryResult queryResult) throws SQLException {
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
     * 获取数据.
     * 
     * @param columnIndex 列索引
     * @return 数据
     */
    public Object getCell(final int columnIndex) {
        Preconditions.checkArgument(columnIndex > 0 && columnIndex < data.length + 1);
        return data[columnIndex - 1];
    }
    
    /**
     * 设置数据.
     *
     * @param columnIndex 列索引
     * @param value 值
     */
    public void setCell(final int columnIndex, final Object value) {
        Preconditions.checkArgument(columnIndex > 0 && columnIndex < data.length + 1);
        data[columnIndex - 1] = value;
    }
}
