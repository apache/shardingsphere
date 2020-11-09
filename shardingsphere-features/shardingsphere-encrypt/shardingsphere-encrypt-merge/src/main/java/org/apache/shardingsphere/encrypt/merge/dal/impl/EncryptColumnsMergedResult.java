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

package org.apache.shardingsphere.encrypt.merge.dal.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.metadata.EncryptColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encrypt column merged result.
 */
public abstract class EncryptColumnsMergedResult implements MergedResult {
    
    private final ShardingSphereSchema schema;
    
    private final String tableName;
    
    protected EncryptColumnsMergedResult(final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema) {
        this.schema = schema;
        Preconditions.checkState(sqlStatementContext instanceof TableAvailable && 1 == ((TableAvailable) sqlStatementContext).getAllTables().size());
        tableName = ((TableAvailable) sqlStatementContext).getAllTables().iterator().next().getTableName().getIdentifier().getValue();
    }
    
    @Override
    public final boolean next() throws SQLException { 
        boolean hasNext = nextValue();
        if (hasNext && getTableEncryptColumnMetaDataList().isEmpty()) {
            return true;
        }
        if (!hasNext) {
            return false;
        }
        String columnName = getOriginalValue(1, String.class).toString();
        while (getAssistedQueryColumns().contains(columnName) || getPlainColumns().contains(columnName)) {
            hasNext = nextValue();
            if (!hasNext) {
                return false;
            }
            columnName = getOriginalValue(1, String.class).toString();
        }
        return true;
    }
    
    private Collection<String> getAssistedQueryColumns() {
        return getTableEncryptColumnMetaDataList().stream().map(EncryptColumnMetaData::getAssistedQueryColumnName)
                .collect(Collectors.toList());
    }
    
    private Collection<String> getPlainColumns() {
        return getTableEncryptColumnMetaDataList().stream().map(EncryptColumnMetaData::getPlainColumnName)
                .collect(Collectors.toList());
    }
    
    private Collection<EncryptColumnMetaData> getTableEncryptColumnMetaDataList() {
        Collection<EncryptColumnMetaData> result = new LinkedList<>();
        for (Entry<String, PhysicalColumnMetaData> entry : schema.get(tableName).getColumns().entrySet()) {
            if (entry.getValue() instanceof EncryptColumnMetaData) {
                result.add((EncryptColumnMetaData) entry.getValue());
            }
        }
        return result;
    }
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (1 == columnIndex) {
            String columnName = getOriginalValue(1, type).toString();
            Optional<String> logicColumn = getLogicColumnOfCipher(columnName);
            return logicColumn.orElse(columnName);
        }
        return getOriginalValue(columnIndex, type);
    }
    
    private Optional<String> getLogicColumnOfCipher(final String cipherColumn) {
        for (Entry<String, PhysicalColumnMetaData> entry : schema.get(tableName).getColumns().entrySet()) {
            if (entry.getValue() instanceof EncryptColumnMetaData) {
                EncryptColumnMetaData encryptColumnMetaData = (EncryptColumnMetaData) entry.getValue();
                if (encryptColumnMetaData.getCipherColumnName().equalsIgnoreCase(cipherColumn)) {
                    return Optional.of(entry.getKey());
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    protected abstract boolean nextValue() throws SQLException;
    
    protected abstract Object getOriginalValue(int columnIndex, Class<?> type) throws SQLException;
}
