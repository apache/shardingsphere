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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.adapter.WrapperAdapter;
import org.apache.shardingsphere.driver.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * ShardingSphere result set meta data.
 */
@RequiredArgsConstructor
public final class ShardingSphereResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final Collection<ShardingSphereRule> rules;
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public int getColumnCount() throws SQLException {
        if (sqlStatementContext instanceof SelectStatementContext) {
            List<Projection> expandProjections = ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections();
            if (expandProjections.isEmpty()) {
                return resultSetMetaData.getColumnCount();
            }
            return expandProjections.size();
        }
        return resultSetMetaData.getColumnCount();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return resultSetMetaData.isAutoIncrement(column);
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        return resultSetMetaData.isCaseSensitive(column);
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        return resultSetMetaData.isSearchable(column);
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        return resultSetMetaData.isCurrency(column);
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        return resultSetMetaData.isNullable(column);
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return resultSetMetaData.isSigned(column);
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(column);
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        return resultSetMetaData.getColumnLabel(column);
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        if (isHasSelectExpandProjections()) {
            List<Projection> actualProjections = ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections();
            if (column > actualProjections.size()) {
                throw new SQLException(SQLExceptionConstant.COLUMN_INDEX_OUT_OF_RANGE, SQLExceptionConstant.OUT_OF_INDEX_SQL_STATE, 0);
            }
            Projection projection = ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().get(column - 1);
            if (projection instanceof ColumnProjection) {
                return ((ColumnProjection) projection).getName();
            }
        }
        return resultSetMetaData.getColumnName(column);
    }
    
    private boolean isHasSelectExpandProjections() {
        return sqlStatementContext instanceof SelectStatementContext
                && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    @Override
    public String getSchemaName(final int column) {
        return DefaultSchema.LOGIC_NAME;
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        return resultSetMetaData.getPrecision(column);
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        return resultSetMetaData.getScale(column);
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        String actualTableName = resultSetMetaData.getTableName(column);
        Optional<ShardingSphereRule> rule = rules.stream().filter(each -> each instanceof DataNodeRoutedRule).findFirst();
        return rule.isPresent() ? ((DataNodeRoutedRule) rule.get()).findLogicTableByActualTable(actualTableName).orElse(actualTableName) : actualTableName;
    }
    
    @Override
    public String getCatalogName(final int column) {
        return DefaultSchema.LOGIC_NAME;
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return resultSetMetaData.getColumnType(column);
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        return resultSetMetaData.getColumnTypeName(column);
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return resultSetMetaData.isReadOnly(column);
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return resultSetMetaData.isWritable(column);
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return resultSetMetaData.isDefinitelyWritable(column);
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        return resultSetMetaData.getColumnClassName(column);
    }
}
