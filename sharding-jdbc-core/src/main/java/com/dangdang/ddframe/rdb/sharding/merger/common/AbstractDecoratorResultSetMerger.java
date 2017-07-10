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

package com.dangdang.ddframe.rdb.sharding.merger.common;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMerger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * 装饰结果集归并.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractDecoratorResultSetMerger implements ResultSetMerger {
    
    private final ResultSetMerger resultSetMerger;
        
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return resultSetMerger.getValue(columnIndex, type);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return resultSetMerger.getValue(columnLabel, type);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return resultSetMerger.getCalendarValue(columnIndex, type, calendar);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        return resultSetMerger.getCalendarValue(columnLabel, type, calendar);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        return resultSetMerger.getInputStream(columnIndex, type);
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        return resultSetMerger.getInputStream(columnLabel, type);
    }
}
