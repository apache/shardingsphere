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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.vertx;

import io.vertx.sqlclient.Row;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Vert.x query result.
 */
@RequiredArgsConstructor
public final class VertxQueryResult implements QueryResult {
    
    private final QueryResultMetaData queryResultMetaData;
    
    private final Iterator<Row> rowIterator;
    
    private Row current;
    
    @Override
    public boolean next() throws SQLException {
        boolean hasNext = rowIterator.hasNext();
        if (hasNext) {
            current = rowIterator.next();
        }
        return hasNext;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return Object.class == type ? current.getValue(columnIndex - 1) : current.get(type, columnIndex - 1);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        throw new UnsupportedSQLOperationException("");
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        throw new UnsupportedSQLOperationException("getInputStream");
    }
    
    @Override
    public boolean wasNull() {
        return false;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return queryResultMetaData;
    }
    
    @Override
    public void close() throws SQLException {
    }
}
