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

package org.apache.shardingsphere.encrypt.merge.dql;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.merge.MergedResult;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Merged result for encrypt.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptMergedResult implements MergedResult {
    
    private final EncryptorMetaData metaData;
    
    private final MergedResult mergedResult;
    
    private final boolean queryWithCipherColumn;
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Object value = mergedResult.getValue(columnIndex, type);
        if (null == value || !queryWithCipherColumn) {
            return value;
        }
        Optional<ShardingEncryptor> encryptor = metaData.findEncryptor(columnIndex);
        return encryptor.isPresent() ? encryptor.get().decrypt(value.toString()) : value;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return mergedResult.getCalendarValue(columnIndex, type, calendar);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        return mergedResult.getInputStream(columnIndex, type);
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return mergedResult.wasNull();
    }
}
