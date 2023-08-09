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

package org.apache.shardingsphere.encrypt.merge.dal.show;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Optional;

/**
 * Encrypt show columns merged result.
 */
public abstract class EncryptShowColumnsMergedResult implements MergedResult {
    
    private static final int COLUMN_FIELD_INDEX = 1;
    
    private final String tableName;
    
    private final EncryptRule encryptRule;
    
    protected EncryptShowColumnsMergedResult(final SQLStatementContext sqlStatementContext, final EncryptRule encryptRule) {
        ShardingSpherePreconditions.checkState(sqlStatementContext instanceof TableAvailable && 1 == ((TableAvailable) sqlStatementContext).getAllTables().size(),
                () -> new UnsupportedEncryptSQLException("SHOW COLUMNS FOR MULTI TABLE"));
        tableName = ((TableAvailable) sqlStatementContext).getAllTables().iterator().next().getTableName().getIdentifier().getValue();
        this.encryptRule = encryptRule;
    }
    
    @Override
    public final boolean next() throws SQLException {
        boolean hasNext = nextValue();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (hasNext && !encryptTable.isPresent()) {
            return true;
        }
        if (!hasNext) {
            return false;
        }
        String columnName = getOriginalValue(COLUMN_FIELD_INDEX, String.class).toString();
        while (isDerivedColumn(encryptTable.get(), columnName)) {
            hasNext = nextValue();
            if (!hasNext) {
                return false;
            }
            columnName = getOriginalValue(COLUMN_FIELD_INDEX, String.class).toString();
        }
        return true;
    }
    
    private boolean isDerivedColumn(final EncryptTable encryptTable, final String columnName) {
        return encryptTable.isAssistedQueryColumn(columnName) || encryptTable.isLikeQueryColumn(columnName);
    }
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (COLUMN_FIELD_INDEX == columnIndex) {
            String columnName = getOriginalValue(COLUMN_FIELD_INDEX, type).toString();
            Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
            if (!encryptTable.isPresent()) {
                return columnName;
            }
            Optional<String> logicColumn = encryptTable.get().isCipherColumn(columnName) ? Optional.of(encryptTable.get().getLogicColumnByCipherColumn(columnName)) : Optional.empty();
            return logicColumn.orElse(columnName);
        }
        return getOriginalValue(columnIndex, type);
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    protected abstract boolean nextValue() throws SQLException;
    
    protected abstract Object getOriginalValue(int columnIndex, Class<?> type) throws SQLException;
}
