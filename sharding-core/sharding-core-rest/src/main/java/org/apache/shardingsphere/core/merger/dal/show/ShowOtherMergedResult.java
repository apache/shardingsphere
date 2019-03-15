/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.merger.dal.show;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.executor.sql.execute.result.QueryResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

/**
 * Merged result for show others.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShowOtherMergedResult implements MergedResult {
    
    private final QueryResult queryResult;
    
    @Override
    public boolean next() throws SQLException {
        return queryResult.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return queryResult.getValue(columnIndex, type);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return queryResult.getValue(columnLabel, type);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean wasNull() {
        return false;
    }
}
