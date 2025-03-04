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
import org.apache.shardingsphere.encrypt.exception.data.DecryptFailedException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Optional;

/**
 * Merged result for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptMergedResult implements MergedResult {
    
    private final ShardingSphereDatabase database;
    
    private final ShardingSphereMetaData metaData;
    
    private final SelectStatementContext selectStatementContext;
    
    private final MergedResult mergedResult;
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Optional<ColumnSegmentBoundInfo> columnSegmentBoundInfo = selectStatementContext.findColumnBoundInfo(columnIndex);
        if (!columnSegmentBoundInfo.isPresent()) {
            return mergedResult.getValue(columnIndex, type);
        }
        String originalTableName = columnSegmentBoundInfo.get().getOriginalTable().getValue();
        String originalColumnName = columnSegmentBoundInfo.get().getOriginalColumn().getValue();
        ShardingSphereDatabase database = metaData.containsDatabase(columnSegmentBoundInfo.get().getOriginalDatabase().getValue())
                ? metaData.getDatabase(columnSegmentBoundInfo.get().getOriginalDatabase().getValue())
                : this.database;
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        if (!rule.isPresent() || !rule.get().findEncryptTable(originalTableName).map(optional -> optional.isEncryptColumn(originalColumnName)).orElse(false)) {
            return mergedResult.getValue(columnIndex, type);
        }
        Object cipherValue = mergedResult.getValue(columnIndex, Object.class);
        EncryptColumn encryptColumn = rule.get().getEncryptTable(originalTableName).getEncryptColumn(originalColumnName);
        String schemaName = selectStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(selectStatementContext.getDatabaseType()).getDefaultSchemaName(database.getName()));
        try {
            return encryptColumn.getCipher().decrypt(database.getName(), schemaName, originalTableName, originalColumnName, cipherValue);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new DecryptFailedException(String.valueOf(cipherValue), new SQLExceptionIdentifier(database.getName(), originalTableName, originalColumnName), ex);
        }
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
