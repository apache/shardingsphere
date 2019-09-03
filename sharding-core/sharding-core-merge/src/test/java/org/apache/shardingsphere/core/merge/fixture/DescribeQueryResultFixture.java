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

package org.apache.shardingsphere.core.merge.fixture;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResultMetaData;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;


/**
 * fixture for test DescribeTableMergedResult.
 *
 * @author liya
 */
@RequiredArgsConstructor
public final class DescribeQueryResultFixture implements QueryResult {

    private final Iterator<QueryResult> queryResults;

    private QueryResult currQueryResult;

    @Override
    public boolean next() throws SQLException {
        boolean hasNext = queryResults.hasNext();
        if (hasNext) {
            currQueryResult = queryResults.next();
        }
        return hasNext;
    }

    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return currQueryResult.getValue(columnIndex, type);
    }

    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return null;
    }

    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return null;
    }

    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        return null;
    }

    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        return null;
    }

    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        return null;
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(final int columnIndex) throws SQLException {
        return false;
    }

    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        return null;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return currQueryResult.getColumnCount();
    }

    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        return null;
    }
}
