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

package org.apache.shardingsphere.sqlfederation.advanced.resultset;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Federation result set meta data.
 */
@RequiredArgsConstructor
public final class FederationResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final ShardingSphereSchema schema;
    
    private final AbstractSchema filterableSchema;
    
    private final RelDataTypeFactory relDataTypeFactory;
    
    private final SelectStatementContext selectStatementContext;
    
    @Override
    public int getColumnCount() throws SQLException {
        return selectStatementContext.getProjectionsContext().getExpandProjections().size();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) {
        return true;
    }
    
    @Override
    public boolean isSearchable(final int column) {
        return false;
    }
    
    @Override
    public boolean isCurrency(final int column) {
        return false;
    }
    
    @Override
    public int isNullable(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(filterableSchema.getTable(optional)));
        return !table.isPresent() || table.get().getRowType(relDataTypeFactory).isNullable() ? ResultSetMetaData.columnNullable : ResultSetMetaData.columnNoNulls;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return true;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) {
        return findTableName(column).flatMap(optional -> Optional.ofNullable(filterableSchema.getTable(optional))).map(optional -> optional.getRowType(relDataTypeFactory).getPrecision()).orElse(0);
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        Projection projection = selectStatementContext.getProjectionsContext().getExpandProjections().get(column - 1);
        if (projection instanceof ColumnProjection) {
            return ((ColumnProjection) projection).getName();
        }
        return projection.getColumnLabel();
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        Projection projection = selectStatementContext.getProjectionsContext().getExpandProjections().get(column - 1);
        if (projection instanceof ColumnProjection) {
            return ((ColumnProjection) projection).getName();
        }
        return projection.getColumnLabel();
    }
    
    @Override
    public String getSchemaName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getPrecision(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(filterableSchema.getTable(optional)));
        return !table.isPresent() || RelDataType.PRECISION_NOT_SPECIFIED == table.get().getRowType(relDataTypeFactory).getPrecision() ? 0 : table.get().getRowType(relDataTypeFactory).getPrecision();
    }
    
    @Override
    public int getScale(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(filterableSchema.getTable(optional)));
        return !table.isPresent() || RelDataType.SCALE_NOT_SPECIFIED == table.get().getRowType(relDataTypeFactory).getScale() ? 0 : table.get().getRowType(relDataTypeFactory).getScale();
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        return findTableName(column).orElse("");
    }
    
    @Override
    public String getCatalogName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return 0;
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        return "";
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isWritable(final int column) {
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) {
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) {
        return "";
    }
    
    private Optional<String> findTableName(final int column) {
        Projection projection = selectStatementContext.getProjectionsContext().getExpandProjections().get(column - 1);
        if (projection instanceof ColumnProjection) {
            Map<String, String> tableNamesByColumnProjection =
                    selectStatementContext.getTablesContext().findTableNamesByColumnProjection(Collections.singletonList((ColumnProjection) projection), schema);
            return Optional.of(tableNamesByColumnProjection.get(projection.getExpression()));
        }
        return Optional.empty();
    }
}
