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

package org.apache.shardingsphere.infra.merge.result.impl.stream;

import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Stream merged result.
 */
@Setter
public abstract class StreamMergedResult implements MergedResult {
    
    private QueryResult currentQueryResult;
    
    private boolean wasNull;
    
    protected final QueryResult getCurrentQueryResult() throws SQLException {
        ShardingSpherePreconditions.checkNotNull(currentQueryResult, () -> new SQLException("Current ResultSet is null, ResultSet perhaps end of next"));
        return currentQueryResult;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Object result = getCurrentQueryResult().getValue(columnIndex, type);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, @SuppressWarnings("UseOfObsoleteDateTimeApi") final Calendar calendar) throws SQLException {
        Object result = getCurrentQueryResult().getCalendarValue(columnIndex, type, calendar);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        InputStream result = getCurrentQueryResult().getInputStream(columnIndex, type);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        Reader result = getCurrentQueryResult().getCharacterStream(columnIndex);
        wasNull = getCurrentQueryResult().wasNull();
        return result;
    }
    
    @Override
    public final boolean wasNull() {
        return wasNull;
    }
}
