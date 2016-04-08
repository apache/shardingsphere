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

package com.dangdang.ddframe.rdb.sharding.merger.fixture;

import java.sql.SQLException;
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
    
    @SafeVarargs
    public MockResultSet(@SuppressWarnings("unchecked") final T... data) {
        columnNamesMetaData = new ArrayList<>(1);
        List<Map<String, T>> list = new ArrayList<>(data.length);
        for (T each : data) {
            columnNamesMetaData.add("name");
            Map<String, T> map = new LinkedHashMap<>(1);
            map.put("name", each);
            list.add(map);
        }
        this.data = list.iterator();
    }
    
    public MockResultSet(final List<Map<String, T>> data) {
        columnNamesMetaData = new ArrayList<>();
        if (!data.isEmpty()) {
            columnNamesMetaData.addAll(data.get(0).keySet());
        }
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
            throw new SQLException(String.format("can not contains column %s, column is %s", columnLabel, columnNamesMetaData));
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
}
