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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encrypt show create table merged result.
 */
public abstract class EncryptShowCreateTableMergedResult implements MergedResult {
    
    private static final String COMMA = ",";
    
    private static final int CREATE_TABLE_DEFINITION_INDEX = 2;
    
    private final String tableName;
    
    private final EncryptRule encryptRule;
    
    protected EncryptShowCreateTableMergedResult(final SQLStatementContext<?> sqlStatementContext, final EncryptRule encryptRule) {
        Preconditions.checkState(sqlStatementContext instanceof TableAvailable && 1 == ((TableAvailable) sqlStatementContext).getAllTables().size());
        tableName = ((TableAvailable) sqlStatementContext).getAllTables().iterator().next().getTableName().getIdentifier().getValue();
        this.encryptRule = encryptRule;
    }
    
    @Override
    public final boolean next() throws SQLException {
        return nextValue();
    }
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (CREATE_TABLE_DEFINITION_INDEX == columnIndex) {
            String result = getOriginalValue(CREATE_TABLE_DEFINITION_INDEX, type).toString();
            Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
            if (!encryptTable.isPresent()) {
                return result;
            }
            StringBuilder builder = new StringBuilder(result.substring(0, result.indexOf("(") + 1));
            List<String> columnDefinitions = Splitter.on(COMMA).splitToList(result.substring(result.indexOf("(") + 1, result.lastIndexOf(")")));
            for (String each : columnDefinitions) {
                findLogicColumnDefinition(each, encryptTable.get()).ifPresent(optional -> builder.append(optional).append(COMMA));
            }
            builder.deleteCharAt(builder.length() - 1).append(result.substring(result.lastIndexOf(")")));
            return builder.toString();
        }
        return getOriginalValue(columnIndex, type);
    }
    
    private Optional<String> findLogicColumnDefinition(final String columnDefinition, final EncryptTable encryptTable) {
        Collection<String> cipherColumns = encryptTable.getLogicColumns().stream().map(encryptTable::getCipherColumn).collect(Collectors.toList());
        for (String each : cipherColumns) {
            if (columnDefinition.contains(each)) {
                return Optional.of(columnDefinition.replace(each, encryptTable.getLogicColumnByCipherColumn(each)));
            }
        }
        if (encryptTable.getPlainColumns().stream().anyMatch(columnDefinition::contains)) {
            return Optional.empty();
        }
        if (encryptTable.getAssistedQueryColumns().stream().anyMatch(columnDefinition::contains)) {
            return Optional.empty();
        }
        if (encryptTable.getLikeQueryColumns().stream().anyMatch(columnDefinition::contains)) {
            return Optional.empty();
        }
        return Optional.of(columnDefinition);
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
