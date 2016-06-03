/*
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

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractForwardingResultSetAdapter;
import lombok.Getter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * 原始结果集包装类.
 * 
 * @author gaohongtao
 */
public class WrapperResultSet extends AbstractForwardingResultSetAdapter {
    
    @Getter
    private final boolean isEmpty;
    
    private boolean isFirstNext;
    
    public WrapperResultSet(final ResultSet resultSet) throws SQLException {
        if (isEmpty = !resultSet.next()) {
            return;
        }
        setDelegate(resultSet);
        increaseStat();
    }
    
    @Override
    public boolean next() throws SQLException {
        if (!isFirstNext) {
            return isFirstNext = true;
        }
        return super.next();
    }
    
    /**
     * 获取列标签与列索引之间的映射.
     * 
     * @return 映射对象
     * @throws SQLException 访问元数据可能会抛出异常
     */
    public Map<String, Integer> getColumnLabelIndexMap() throws SQLException {
        ResultSetMetaData resultSetMetaData = getDelegate().getMetaData();
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount());
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            result.put(resultSetMetaData.getColumnLabel(i), i);
        }
        return result;
    }
}
