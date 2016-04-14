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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * 数据行.
 *
 * @author gaohongtao
 */
// TODO 名字最好再考虑下, ROW有点大, 而且guava, swing等已经用了
public class Row {
    
    private final Object[] rowData;
    
    public Row(final ResultSet resultSet) throws SQLException {
        rowData = getRowData(resultSet);
    }
    
    // TODO rename => fillRowData?
    private Object[] getRowData(final ResultSet resultSet) throws SQLException {
        // TODO 最好不要用md这类缩写
        ResultSetMetaData md = resultSet.getMetaData();
        Object[] result = new Object[md.getColumnCount()];
        for (int i = 0; i < md.getColumnCount(); i++) {
            result[i] = resultSet.getObject(i + 1);
        }
        return result;
    }
    
    void setCell(final int index, final Object value) {
        Preconditions.checkArgument(containsCell(index));
        rowData[index - 1] = value;
    }
    
    // TODO javadoc
    public Object getCell(final int index) {
        Preconditions.checkArgument(containsCell(index));
        return rowData[index - 1];
    }
    
    // TODO javadoc
    // TODO 改名? 这名字不能望文生义, 比如: isIndexOutOfRange
    public boolean containsCell(final int index) {
        return index - 1 > -1 && index - 1 < rowData.length;
    }
    
    @Override
    public String toString() {
        // TODO 试试 Arrays.toString(rowData)
        return String.format("value is : %s", Lists.transform(Arrays.asList(rowData), new Function<Object, Object>() {
    
            @Override
            public Object apply(final Object input) {
                return null == input ? "nil" : input;
            }
        }));
    }
}
