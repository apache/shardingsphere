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

package org.apache.shardingsphere.sharding.merge.dal.common;

import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Merged result for multiple local data.
 */
public final class MultipleLocalDataMergedResult implements MergedResult {
    
    private final Iterator<List<Object>> rows;
    
    private List<Object> currentRow;
    
    public MultipleLocalDataMergedResult(final Collection<List<Object>> rows) {
        this.rows = rows.iterator();
    }
    
    @Override
    public boolean next() {
        if (rows.hasNext()) {
            currentRow = rows.next();
            return true;
        }
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return currentRow.get(columnIndex - 1);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.get(columnIndex - 1);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean wasNull() {
        return null == currentRow;
    }
}
