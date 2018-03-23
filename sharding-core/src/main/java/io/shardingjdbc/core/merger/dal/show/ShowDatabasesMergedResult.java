/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.merger.dal.show;

import io.shardingjdbc.core.constant.ShardingConstant;
import io.shardingjdbc.core.merger.MergedResult;

import java.io.InputStream;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

/**
 * Merged result for show databases.
 *
 * @author zhangliang
 */
public final class ShowDatabasesMergedResult implements MergedResult {
    
    private boolean firstNext = true;
    
    @Override
    public boolean next() {
        if (firstNext) {
            firstNext = false;
            return true;
        }
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return ShardingConstant.LOGIC_SCHEMA_NAME;
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return ShardingConstant.LOGIC_SCHEMA_NAME;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean wasNull() {
        return false;
    }
}
