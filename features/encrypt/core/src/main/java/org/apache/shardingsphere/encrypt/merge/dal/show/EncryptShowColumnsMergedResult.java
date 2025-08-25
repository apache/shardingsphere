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
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ColumnInResultSetSQLStatementAttribute;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Encrypt show columns merged result.
 */
public final class EncryptShowColumnsMergedResult extends DecoratorMergedResult {
    
    private final EncryptRule rule;
    
    private final String tableName;
    
    private final int columnNameResultSetIndex;
    
    public EncryptShowColumnsMergedResult(final MergedResult mergedResult, final SQLStatementContext sqlStatementContext, final EncryptRule rule) {
        super(mergedResult);
        ShardingSpherePreconditions.checkState(1 == sqlStatementContext.getTablesContext().getSimpleTables().size(),
                () -> new UnsupportedEncryptSQLException("SHOW COLUMNS FOR MULTI TABLES"));
        this.rule = rule;
        tableName = sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
        ColumnInResultSetSQLStatementAttribute attribute = sqlStatementContext.getSqlStatement().getAttributes().getAttribute(ColumnInResultSetSQLStatementAttribute.class);
        columnNameResultSetIndex = attribute.getNameResultSetIndex();
    }
    
    @Override
    public boolean next() throws SQLException {
        boolean hasNext = getMergedResult().next();
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(tableName);
        if (hasNext && !encryptTable.isPresent()) {
            return true;
        }
        if (!hasNext) {
            return false;
        }
        return next(encryptTable.get());
    }
    
    private boolean next(final EncryptTable encryptTable) throws SQLException {
        while (encryptTable.isDerivedColumn(getMergedResult().getValue(columnNameResultSetIndex, String.class).toString())) {
            boolean isFinished = !getMergedResult().next();
            if (isFinished) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (columnNameResultSetIndex == columnIndex) {
            return getColumnNameValue(type);
        }
        return getMergedResult().getValue(columnIndex, type);
    }
    
    private String getColumnNameValue(final Class<?> type) throws SQLException {
        String columnName = getMergedResult().getValue(columnNameResultSetIndex, type).toString();
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(tableName);
        return encryptTable.isPresent() && encryptTable.get().isCipherColumn(columnName) ? encryptTable.get().getLogicColumnByCipherColumn(columnName) : columnName;
    }
}
