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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class MockResultSet<T> extends AbstractUnsupportedOperationMockResultSet {
    
    private final List<String> columnNamesMetaData;
    
    private final Iterator<Map<String, T>> data;
    
    private Map<String, T> currentValue;
    
    private boolean isClosed;
    
    private final int size;
    
    @SafeVarargs
    public MockResultSet(@SuppressWarnings("unchecked") final T... data) {
        columnNamesMetaData = new ArrayList<>(1);
        columnNamesMetaData.add("name");
        List<Map<String, T>> list = new ArrayList<>(data.length);
        for (T each : data) {
            Map<String, T> map = new LinkedHashMap<>(1);
            map.put("name", each);
            list.add(map);
        }
        size = list.size();
        this.data = list.iterator();
    }
    
    public MockResultSet(final List<Map<String, T>> data) {
        columnNamesMetaData = new ArrayList<>();
        if (!data.isEmpty()) {
            columnNamesMetaData.addAll(data.get(0).keySet());
        }
        size = data.size();
        this.data = data.iterator();
    }
    
    public MockResultSet() {
        this(Collections.<Map<String, T>>emptyList());
    }
    
    @Override
    public boolean next() throws SQLException {
        boolean result = data.hasNext();
        if (result) {
            currentValue = data.next();
        }
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        isClosed = true;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return isClosed;
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (Integer) find(columnIndex);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        validateColumn(columnLabel);
        return (Integer) currentValue.get(columnLabel);
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) find(columnIndex);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        validateColumn(columnLabel);
        return (String) currentValue.get(columnLabel);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return find(columnIndex);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        validateColumn(columnLabel);
        return currentValue.get(columnLabel);
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return columnNamesMetaData.indexOf(columnLabel) + 1;
    }
    
    private void validateColumn(final String columnLabel) throws SQLException {
        if (!columnNamesMetaData.contains(columnLabel)) {
            throw new SQLException(String.format("can not inRange column %s, column is %s", columnLabel, columnNamesMetaData));
        }
    }
    
    private T find(final int columnIndex) {
        int count = 1;
        for (Entry<String, T> entry : currentValue.entrySet()) {
            if (count == columnIndex) {
                return entry.getValue();
            }
            count++;
        }
        return null;
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return size;
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new MockResultSetMetaData();
    }
    
    public class MockResultSetMetaData implements ResultSetMetaData {
        
        @Override
        public int getColumnCount() throws SQLException {
            return columnNamesMetaData.size();
        }
        
        @Override
        public String getColumnLabel(final int column) throws SQLException {
            return columnNamesMetaData.get(column - 1);
        }
        
        @Override
        public boolean isAutoIncrement(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isCaseSensitive(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isSearchable(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isCurrency(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public int isNullable(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isSigned(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public int getColumnDisplaySize(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public String getColumnName(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public String getSchemaName(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public int getPrecision(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public int getScale(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public String getTableName(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public String getCatalogName(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public int getColumnType(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public String getColumnTypeName(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isReadOnly(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isWritable(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isDefinitelyWritable(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public String getColumnClassName(final int column) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public <I> I unwrap(final Class<I> iface) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
    }
}
