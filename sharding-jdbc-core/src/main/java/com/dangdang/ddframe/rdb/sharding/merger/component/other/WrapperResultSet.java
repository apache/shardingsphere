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

package com.dangdang.ddframe.rdb.sharding.merger.component.other;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDelegateResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.IndexColumn;
import lombok.Getter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * 原始结果集包装类.
 * 
 * @author gaohongtao
 */
public class WrapperResultSet extends AbstractDelegateResultSetAdapter {
    
    @Getter
    private final boolean isEmpty;
    
    private final Map<String, Integer> columnLabelIndexMap;
    
    private boolean isFirstNext;
    
    public WrapperResultSet(final ResultSet resultSet) throws SQLException {
        isEmpty = !resultSet.next();
        if (isEmpty) {
            columnLabelIndexMap = Collections.emptyMap();
            return;
        }
        setDelegatedResultSet(resultSet);
        increaseStat();
        columnLabelIndexMap = getColumnLabelIndexMap();
    }
    
    private Map<String, Integer> getColumnLabelIndexMap() throws SQLException {
        ResultSetMetaData resultSetMetaData = getCurrentResultSet().getMetaData();
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount());
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            result.put(resultSetMetaData.getColumnLabel(i), i);
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (isEmpty) {
            return false;
        }
        if (!isFirstNext) {
            return isFirstNext = true;
        }
        return super.next();
    }
    
    /**
     * 获取列索引.
     * 
     * @param indexColumn 基于索引的列
     * @return 列索引
     */
    public int getColumnIndex(final IndexColumn indexColumn) {
        if (indexColumn.getColumnLabel().isPresent() && columnLabelIndexMap.containsKey(indexColumn.getColumnLabel().get())) {
            return columnLabelIndexMap.get(indexColumn.getColumnLabel().get());
        } else if (indexColumn.getColumnName().isPresent() && columnLabelIndexMap.containsKey(indexColumn.getColumnName().get())) {
            return columnLabelIndexMap.get(indexColumn.getColumnName().get());
        }
        throw new IllegalArgumentException(String.format("Cannot find index for column '%s' from ResultSet '%s'", indexColumn, columnLabelIndexMap));
    }
}
