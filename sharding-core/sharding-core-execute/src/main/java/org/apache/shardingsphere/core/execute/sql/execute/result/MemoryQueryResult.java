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

package org.apache.shardingsphere.core.execute.sql.execute.result;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.execute.sql.execute.row.QueryRow;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Query result for memory loading.
 *
 * @author zhangliang
 * @author panjuan
 * @author yangyi
 */
public final class MemoryQueryResult implements QueryResult {
    
    private final Iterator<QueryRow> resultData;
    
    private QueryRow currentRow;

    @Getter
    private final QueryResultMetaData queryResultMetaData;
    
    public MemoryQueryResult(final ResultSet resultSet, final ShardingRule shardingRule) throws SQLException {
        resultData = getResultData(resultSet);
        queryResultMetaData = new QueryResultMetaData(resultSet.getMetaData(), shardingRule);
    }
    
    public MemoryQueryResult(final ResultSet resultSet, final EncryptRule encryptRule) throws SQLException {
        resultData = getResultData(resultSet);
        queryResultMetaData = new QueryResultMetaData(resultSet.getMetaData(), encryptRule);
    }
    
    public MemoryQueryResult(final ResultSet resultSet) throws SQLException {
        resultData = getResultData(resultSet);
        queryResultMetaData = new QueryResultMetaData(resultSet.getMetaData());
    }
        
    private Iterator<QueryRow> getResultData(final ResultSet resultSet) throws SQLException {
        Collection<QueryRow> result = new LinkedList<>();
        while (resultSet.next()) {
            List<Object> rowData = new ArrayList<>(resultSet.getMetaData().getColumnCount());
            for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
                rowData.add(QueryResultUtil.getValue(resultSet, columnIndex));
            }
            result.add(new QueryRow(rowData));
        }
        return result.iterator();
    }
    
    @Override
    public boolean next() {
        if (resultData.hasNext()) {
            currentRow = resultData.next();
            return true;
        }
        currentRow = null;
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return decrypt(columnIndex, currentRow.getColumnValue(columnIndex));
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return decrypt(columnLabel, currentRow.getColumnValue(queryResultMetaData.getColumnIndex(columnLabel)));
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.getColumnValue(columnIndex);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) {
        return currentRow.getColumnValue(queryResultMetaData.getColumnIndex(columnLabel));
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return getInputStream(currentRow.getColumnValue(columnIndex));
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return getInputStream(currentRow.getColumnValue(queryResultMetaData.getColumnIndex(columnLabel)));
    }
    
    @SneakyThrows
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
    
    @Override
    public boolean isCaseSensitive(final int columnIndex) throws SQLException {
        return queryResultMetaData.isCaseSensitive(columnIndex);
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return queryResultMetaData.getColumnCount();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        return queryResultMetaData.getColumnLabel(columnIndex);
    }
    
    private Object decrypt(final String columnLabel, final Object value) throws SQLException {
        return decrypt(queryResultMetaData.getColumnIndex(columnLabel), value);
    }
    
    private Object decrypt(final int columnIndex, final Object value) throws SQLException {
        Optional<ShardingEncryptor> shardingEncryptor = queryResultMetaData.getShardingEncryptor(columnIndex);
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decrypt(getCiphertext(value)) : value;
    }
    
    private String getCiphertext(final Object value) {
        return null == value ? null : value.toString();
    }
}
