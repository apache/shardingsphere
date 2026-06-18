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

package org.apache.shardingsphere.infra.merge.result.impl.local;

import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * Local data merged result.
 */
public final class LocalDataMergedResult implements MergedResult {
    
    private final Iterator<LocalDataQueryResultRow> rows;
    
    private LocalDataQueryResultRow currentRow;
    
    public LocalDataMergedResult(final Collection<LocalDataQueryResultRow> rows) {
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
        return currentRow.getCell(columnIndex);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, @SuppressWarnings("UseOfObsoleteDateTimeApi") final Calendar calendar) {
        return currentRow.getCell(columnIndex);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException("getInputStream");
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getCharacterStream");
    }
    
    @Override
    public boolean wasNull() {
        return null == currentRow;
    }
}
