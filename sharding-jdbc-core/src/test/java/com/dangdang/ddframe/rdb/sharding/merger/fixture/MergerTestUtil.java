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

package com.dangdang.ddframe.rdb.sharding.merger.fixture;

import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergerTestUtil {
    
    public static ResultSet mockResult(final List<String> columnNames, final List<Object> values) throws SQLException {
        ResultSet result = getResultSet(columnNames);
        when(result.next()).thenReturn(true, false);
        for (int i = 0; i < columnNames.size(); i++) {
            when(result.getObject(i + 1)).thenReturn(values.get(i));
        }
        return result;
    }
    
    private static ResultSet getResultSet(final List<String> columnNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(result.next()).thenReturn(true);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(columnNames.size());
        int count = 1;
        for (String each : columnNames) {
            when(resultSetMetaData.getColumnLabel(count)).thenReturn(SQLUtil.getExactlyValue(each));
            count++;
        }
        return result;
    }
}
