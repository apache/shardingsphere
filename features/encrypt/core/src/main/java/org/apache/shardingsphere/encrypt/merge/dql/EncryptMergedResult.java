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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Merged result for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptMergedResult implements MergedResult {
    
    private final ShardingSphereDatabase database;
    
    private final EncryptRule encryptRule;
    
    private final SelectStatementContext selectStatementContext;
    
    private final MergedResult mergedResult;
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Optional<ColumnProjection> columnProjection = selectStatementContext.findColumnProjection(columnIndex);
        if (!columnProjection.isPresent()) {
            return mergedResult.getValue(columnIndex, type);
        }
        TablesContext tablesContext = selectStatementContext.getTablesContext();
        String schemaName = tablesContext.getSchemaName()
                .orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(selectStatementContext.getDatabaseType(), database.getName()));
        ColumnProjection originalColumn = new ColumnProjection(columnProjection.get().getOriginalOwner(), columnProjection.get().getOriginalName(), null);
        Map<String, String> expressionTableNames = tablesContext.findTableNamesByColumnProjection(Collections.singletonList(originalColumn), database.getSchema(schemaName));
        Optional<String> tableName = findTableName(originalColumn, expressionTableNames);
        if (!tableName.isPresent()) {
            return mergedResult.getValue(columnIndex, type);
        }
        if (!encryptRule.findEncryptTable(tableName.get()).map(optional -> optional.isEncryptColumn(originalColumn.getName().getValue())).orElse(false)) {
            return mergedResult.getValue(columnIndex, type);
        }
        Object cipherValue = mergedResult.getValue(columnIndex, Object.class);
        EncryptColumn encryptColumn = encryptRule.getEncryptTable(tableName.get()).getEncryptColumn(originalColumn.getName().getValue());
        return encryptColumn.getCipher().decrypt(database.getName(), schemaName, tableName.get(), originalColumn.getName().getValue(), cipherValue);
    }
    
    private Optional<String> findTableName(final ColumnProjection columnProjection, final Map<String, String> columnTableNames) {
        String tableName = columnTableNames.get(columnProjection.getColumnName());
        if (null != tableName) {
            return Optional.of(tableName);
        }
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            if (encryptRule.findEncryptTable(each).map(optional -> optional.isEncryptColumn(columnProjection.getName().getValue())).orElse(false)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
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
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return mergedResult.getCharacterStream(columnIndex);
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return mergedResult.wasNull();
    }
}
