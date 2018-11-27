/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.merger.dql.common;

import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import lombok.Setter;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Stream merged result.
 *
 * @author zhangliang
 */
@Setter
public abstract class StreamMergedResult implements MergedResult {
    
    private QueryResult currentQueryResult;
    
    private boolean wasNull;
    
    protected final QueryResult getCurrentQueryResult() throws SQLException {
        if (null == currentQueryResult) {
            throw new SQLException("Current ResultSet is null, ResultSet perhaps end of next.");
        }
        return currentQueryResult;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Object result = getCurrentQueryResult().getValue(columnIndex, type);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        Object result = getCurrentQueryResult().getValue(columnLabel, type);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        Object result = getCurrentQueryResult().getCalendarValue(columnIndex, type, calendar);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        Object result = getCurrentQueryResult().getCalendarValue(columnLabel, type, calendar);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        InputStream result = getCurrentQueryResult().getInputStream(columnIndex, type);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        InputStream result = getCurrentQueryResult().getInputStream(columnLabel, type);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public final boolean wasNull() {
        return wasNull;
    }
}
