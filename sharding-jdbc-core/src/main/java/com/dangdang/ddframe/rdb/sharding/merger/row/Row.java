/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.row;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.common.base.Preconditions;

/**
 * 数据行.
 * 每个数据行对象代表结果集中的一行数据.
 * 
 * @author gaohongtao
 */
public class Row {
    
    private final Object[] rowData;
    
    public Row(final ResultSet resultSet) throws SQLException {
        rowData = getRowData(resultSet);
    }
    
    private Object[] getRowData(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData md = resultSet.getMetaData();
        Object[] result = new Object[md.getColumnCount()];
        for (int i = 0; i < md.getColumnCount(); i++) {
            result[i] = resultSet.getObject(i + 1);
        }
        return result;
    }
    
    protected void setCell(final int index, final Object value) {
        Preconditions.checkArgument(containsCell(index));
        rowData[index - 1] = value;
    }
    
    /**
     * 通过索引访问数据行中的单元格.
     * 
     * @param index 索引
     * @return 单元格中的数据
     */
    public Object getCell(final int index) {
        Preconditions.checkArgument(containsCell(index));
        return rowData[index - 1];
    }
    
    /**
     * 判断数据行中是否包含该索引.
     * 
     * @param index 索引
     * @return true 包含 false 不包含
     */
    public boolean containsCell(final int index) {
        return index - 1 > -1 && index - 1 < rowData.length;
    }
}
