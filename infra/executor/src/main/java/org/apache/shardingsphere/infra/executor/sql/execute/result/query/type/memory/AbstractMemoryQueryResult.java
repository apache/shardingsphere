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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract memory query result.
 */
public abstract class AbstractMemoryQueryResult implements QueryResult {
    
    @Getter
    private final QueryResultMetaData metaData;
    
    private final Iterator<MemoryQueryResultDataRow> rows;
    
    @Getter
    private MemoryQueryResultDataRow currentRow;
    
    @Getter
    private long rowCount;
    
    private boolean wasNull;
    
    protected AbstractMemoryQueryResult(final QueryResultMetaData metaData, final Collection<MemoryQueryResultDataRow> rows) {
        this.metaData = metaData;
        this.rows = rows.iterator();
        rowCount = rows.size();
    }
    
    @Override
    public final boolean next() {
        if (rows.hasNext()) {
            currentRow = rows.next();
            rowCount--;
            return true;
        }
        currentRow = null;
        return false;
    }
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) {
        Object result = currentRow.getValue().get(columnIndex - 1);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, @SuppressWarnings("UseOfObsoleteDateTimeApi") final Calendar calendar) {
        Object result = currentRow.getValue().get(columnIndex - 1);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) {
        Object value = currentRow.getValue().get(columnIndex - 1);
        wasNull = null == value;
        return getInputStream(value);
    }
    
    @SneakyThrows(IOException.class)
    private InputStream getInputStream(final Object value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) {
        // TODO Support connection property character encoding
        return new BufferedReader(new InputStreamReader(getInputStream(columnIndex)));
    }
    
    @Override
    public final boolean wasNull() {
        return wasNull;
    }
    
    @Override
    public final void close() {
    }
}
