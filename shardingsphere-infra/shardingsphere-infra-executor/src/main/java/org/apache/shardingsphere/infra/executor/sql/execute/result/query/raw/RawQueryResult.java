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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.row.QueryResultDataRow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Raw query result.
 */
public final class RawQueryResult implements QueryResult {
    
    @Getter
    private final RawQueryResultMetaData metaData;
    
    private final Iterator<QueryResultDataRow> rows;
    
    private QueryResultDataRow currentRow;
    
    public RawQueryResult(final RawQueryResultMetaData metaData, final List<QueryResultDataRow> rows) {
        this.metaData = metaData;
        this.rows = rows.iterator();
    }
    
    @Override
    public boolean next() {
        if (rows.hasNext()) {
            currentRow = rows.next();
            return true;
        }
        currentRow = null;
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return currentRow.getValue().get(columnIndex - 1);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.getValue().get(columnIndex - 1);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return getInputStream(currentRow.getValue().get(columnIndex - 1));
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
    public boolean wasNull() {
        return null == currentRow;
    }
}
