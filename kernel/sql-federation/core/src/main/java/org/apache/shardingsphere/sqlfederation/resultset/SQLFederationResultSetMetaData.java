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

package org.apache.shardingsphere.sqlfederation.resultset;

import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;

import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SQL federation result set meta data.
 */
public final class SQLFederationResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final Schema sqlFederationSchema;
    
    private final RelDataTypeFactory relDataTypeFactory;
    
    private final SelectStatementContext selectStatementContext;
    
    private final RelDataType resultColumnType;
    
    private final Map<Integer, String> indexAndColumnLabels;
    
    private final SQLFederationColumnTypeConverter columnTypeConverter;
    
    public SQLFederationResultSetMetaData(final Schema sqlFederationSchema, final SelectStatementContext selectStatementContext, final RelDataType resultColumnType,
                                          final Map<Integer, String> indexAndColumnLabels, final SQLFederationColumnTypeConverter columnTypeConverter) {
        this.sqlFederationSchema = sqlFederationSchema;
        this.relDataTypeFactory = new JavaTypeFactoryImpl();
        this.selectStatementContext = selectStatementContext;
        this.resultColumnType = resultColumnType;
        this.indexAndColumnLabels = indexAndColumnLabels;
        this.columnTypeConverter = columnTypeConverter;
    }
    
    @Override
    public int getColumnCount() {
        return resultColumnType.getFieldCount();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) {
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
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.getTable(optional)));
        return !table.isPresent() || table.get().getRowType(relDataTypeFactory).isNullable() ? ResultSetMetaData.columnNullable : ResultSetMetaData.columnNoNulls;
    }
    
    @Override
    public boolean isSigned(final int column) {
        return true;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) {
        return findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.getTable(optional))).map(optional -> optional.getRowType(relDataTypeFactory).getPrecision()).orElse(0);
    }
    
    @Override
    public String getColumnLabel(final int column) {
        if (indexAndColumnLabels.size() < column) {
            return resultColumnType.getFieldList().get(column - 1).getName();
        }
        return indexAndColumnLabels.get(column);
    }
    
    @Override
    public String getColumnName(final int column) {
        List<Projection> expandProjections = selectStatementContext.getProjectionsContext().getExpandProjections();
        if (expandProjections.size() < column) {
            return resultColumnType.getFieldList().get(column - 1).getName();
        }
        return expandProjections.get(column - 1).getColumnName();
    }
    
    @Override
    public String getSchemaName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getPrecision(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.getTable(optional)));
        return !table.isPresent() || RelDataType.PRECISION_NOT_SPECIFIED == table.get().getRowType(relDataTypeFactory).getPrecision() ? 0 : table.get().getRowType(relDataTypeFactory).getPrecision();
    }
    
    @Override
    public int getScale(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.getTable(optional)));
        return !table.isPresent() || RelDataType.SCALE_NOT_SPECIFIED == table.get().getRowType(relDataTypeFactory).getScale() ? 0 : table.get().getRowType(relDataTypeFactory).getScale();
    }
    
    @Override
    public String getTableName(final int column) {
        return findTableName(column).orElse("");
    }
    
    @Override
    public String getCatalogName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getColumnType(final int column) {
        int jdbcType = resultColumnType.getFieldList().get(column - 1).getType().getSqlTypeName().getJdbcOrdinal();
        return columnTypeConverter.convertColumnType(jdbcType);
    }
    
    @Override
    public String getColumnTypeName(final int column) {
        SqlTypeName originalSqlTypeName = resultColumnType.getFieldList().get(column - 1).getType().getSqlTypeName();
        SqlTypeName convertSqlTypeName = SqlTypeName.getNameForJdbcType(columnTypeConverter.convertColumnType(originalSqlTypeName.getJdbcOrdinal()));
        return null == convertSqlTypeName ? originalSqlTypeName.getName() : convertSqlTypeName.getName();
    }
    
    @Override
    public boolean isReadOnly(final int column) {
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
        return resultColumnType.getFieldList().get(column - 1).getType().getSqlTypeName().getClass().getName();
    }
    
    private Optional<String> findTableName(final int column) {
        List<Projection> expandProjections = selectStatementContext.getProjectionsContext().getExpandProjections();
        Projection projection =
                expandProjections.size() < column ? new ColumnProjection(null, resultColumnType.getFieldList().get(column - 1).getName(), null, selectStatementContext.getDatabaseType())
                        : expandProjections.get(column - 1);
        if (projection instanceof ColumnProjection) {
            return Optional.ofNullable(((ColumnProjection) projection).getOriginalTable().getValue());
        }
        return Optional.empty();
    }
}
