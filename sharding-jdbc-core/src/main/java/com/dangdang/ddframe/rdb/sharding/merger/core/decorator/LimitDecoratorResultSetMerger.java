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

package com.dangdang.ddframe.rdb.sharding.merger.core.decorator;

import com.dangdang.ddframe.rdb.sharding.merger.core.ResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * 分页结果集归并.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class LimitDecoratorResultSetMerger implements ResultSetMerger {
    
    private final ResultSetMerger resultSetMerger;
    
    private final Limit limit;
    
    private final boolean skipAll;
    
    private int rowNumber;
    
    public LimitDecoratorResultSetMerger(final ResultSetMerger resultSetMerger, final Limit limit) throws SQLException {
        this.resultSetMerger = resultSetMerger;
        this.limit = limit;
        skipAll = skipOffset();
    }
    
    private boolean skipOffset() throws SQLException {
        for (int i = 0; i < limit.getOffsetValue(); i++) {
            if (!resultSetMerger.next()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (skipAll) {
            return false;
        }
        if (limit.getRowCountValue() > 0) {
            return ++rowNumber <= limit.getRowCountValue() && resultSetMerger.next();
        }
        return resultSetMerger.next();
    }
    
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
