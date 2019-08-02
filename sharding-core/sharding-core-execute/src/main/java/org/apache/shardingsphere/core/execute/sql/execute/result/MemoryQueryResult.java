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
    
    private final QueryResultMetaData metaData;
    
    @SneakyThrows
    public MemoryQueryResult(final ResultSet resultSet, final ShardingRule shardingRule) {
        resultData = getResultData(resultSet);
        metaData = new QueryResultMetaData(resultSet.getMetaData(), shardingRule);
    }
    
    @SneakyThrows
    public MemoryQueryResult(final ResultSet resultSet, final EncryptRule encryptRule) {
        resultData = getResultData(resultSet);
        metaData = new QueryResultMetaData(resultSet.getMetaData(), encryptRule);
    }
    
    @SneakyThrows
    public MemoryQueryResult(final ResultSet resultSet) {
        resultData = getResultData(resultSet);
        metaData = new QueryResultMetaData(resultSet.getMetaData());
    }
        
    @SneakyThrows
    private Iterator<QueryRow> getResultData(final ResultSet resultSet) {
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
    public Object getValue(final int columnIndex, final Class<?> type) {
        return decrypt(columnIndex, currentRow.getColumnValue(columnIndex));
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return decrypt(columnLabel, currentRow.getColumnValue(metaData.getColumnIndex(columnLabel)));
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.getColumnValue(columnIndex);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) {
        return currentRow.getColumnValue(metaData.getColumnIndex(columnLabel));
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return getInputStream(currentRow.getColumnValue(columnIndex));
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return getInputStream(currentRow.getColumnValue(metaData.getColumnIndex(columnLabel)));
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
    public boolean isCaseSensitive(final int columnIndex) {
        return metaData.isCaseSensitive(columnIndex);
    }
    
    @Override
    public int getColumnCount() {
        return metaData.getColumnCount();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) {
        return metaData.getColumnLabel(columnIndex);
    }
    
    @SneakyThrows
    private Object decrypt(final String columnLabel, final Object value) {
        return decrypt(metaData.getColumnIndex(columnLabel), value);
    }
    
    @SneakyThrows
    private Object decrypt(final int columnIndex, final Object value) {
        Optional<ShardingEncryptor> shardingEncryptor = metaData.getShardingEncryptor(columnIndex);
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decrypt(getCiphertext(value)) : value;
    }
    
    private String getCiphertext(final Object value) {
        return null == value ? null : value.toString();
    }
}
