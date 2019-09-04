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
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.io.InputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Query result for stream loading.
 *
 * @author zhangliang
 * @author panjuan
 * @author yangyi
 */
public final class StreamQueryResult implements QueryResult {

    @Getter
    private final QueryResultMetaData queryResultMetaData;
    
    private final ResultSet resultSet;
    
    public StreamQueryResult(final ResultSet resultSet, final ShardingRule shardingRule) throws SQLException {
        this.resultSet = resultSet;
        queryResultMetaData = new QueryResultMetaData(resultSet.getMetaData(), shardingRule);
    }
    
    public StreamQueryResult(final ResultSet resultSet, final EncryptRule encryptRule) throws SQLException {
        this.resultSet = resultSet;
        queryResultMetaData = new QueryResultMetaData(resultSet.getMetaData(), encryptRule);
    }
    
    public StreamQueryResult(final ResultSet resultSet) throws SQLException {
        this.resultSet = resultSet;
        queryResultMetaData = new QueryResultMetaData(resultSet.getMetaData());
    }
    
    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return decrypt(columnIndex, QueryResultUtil.getValue(resultSet, columnIndex));
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return decrypt(columnLabel, QueryResultUtil.getValue(resultSet, queryResultMetaData.getColumnIndex(columnLabel)));
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return resultSet.getDate(columnIndex, calendar);
        }
        if (Time.class == type) {
            return resultSet.getTime(columnIndex, calendar);
        }
        if (Timestamp.class == type) {
            return resultSet.getTimestamp(columnIndex, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return resultSet.getDate(columnLabel, calendar);
        }
        if (Time.class == type) {
            return resultSet.getTime(columnLabel, calendar);
        }
        if (Timestamp.class == type) {
            return resultSet.getTimestamp(columnLabel, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        switch (type) {
            case "Ascii":
                return resultSet.getAsciiStream(columnIndex);
            case "Unicode":
                return resultSet.getUnicodeStream(columnIndex);
            case "Binary":
                return resultSet.getBinaryStream(columnIndex);
            default:
                throw new SQLException(String.format("Unsupported type: %s", type));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        switch (type) {
            case "Ascii":
                return resultSet.getAsciiStream(columnLabel);
            case "Unicode":
                return resultSet.getUnicodeStream(columnLabel);
            case "Binary":
                return resultSet.getBinaryStream(columnLabel);
            default:
                throw new SQLException(String.format("Unsupported type: %s", type));
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
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
