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

import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFactoryImpl.JavaType;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeFactory;
import org.apache.shardingsphere.sqlfederation.resultset.converter.DialectSQLFederationColumnTypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SQL federation result set meta data.
 */
public final class SQLFederationResultSetMetaData extends SQLFederationWrapperAdapter implements ResultSetMetaData {
    
    private final Schema sqlFederationSchema;
    
    private final RelDataTypeFactory typeFactory;
    
    private final List<Projection> expandProjections;
    
    private final DatabaseType databaseType;
    
    private final RelDataType resultColumnType;
    
    private final Map<Integer, String> indexAndColumnLabels;
    
    private final DialectSQLFederationColumnTypeConverter columnTypeConverter;
    
    public SQLFederationResultSetMetaData(final Schema sqlFederationSchema, final List<Projection> expandProjections, final DatabaseType databaseType, final RelDataType resultColumnType,
                                          final Map<Integer, String> indexAndColumnLabels, final DialectSQLFederationColumnTypeConverter columnTypeConverter) {
        this.sqlFederationSchema = sqlFederationSchema;
        typeFactory = SQLFederationDataTypeFactory.getInstance();
        this.expandProjections = expandProjections;
        this.databaseType = databaseType;
        this.resultColumnType = resultColumnType;
        this.indexAndColumnLabels = indexAndColumnLabels;
        this.columnTypeConverter = columnTypeConverter;
    }
    
    @Override
    public int getColumnCount() {
        return indexAndColumnLabels.size();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) {
        return false;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) {
        return false;
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
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.tables().get(optional)));
        return !table.isPresent() || table.get().getRowType(typeFactory).isNullable() ? columnNullable : columnNoNulls;
    }
    
    @Override
    public boolean isSigned(final int column) {
        return true;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) {
        return findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.tables().get(optional))).map(optional -> optional.getRowType(typeFactory).getPrecision()).orElse(0);
    }
    
    @Override
    public String getColumnLabel(final int column) {
        return indexAndColumnLabels.size() < column ? resultColumnType.getFieldList().get(column - 1).getName() : indexAndColumnLabels.get(column);
    }
    
    @Override
    public String getColumnName(final int column) {
        return expandProjections.size() < column ? resultColumnType.getFieldList().get(column - 1).getName() : expandProjections.get(column - 1).getColumnName();
    }
    
    @Override
    public String getSchemaName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getPrecision(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.tables().get(optional)));
        return !table.isPresent() || RelDataType.PRECISION_NOT_SPECIFIED == table.get().getRowType(typeFactory).getPrecision() ? 0 : table.get().getRowType(typeFactory).getPrecision();
    }
    
    @Override
    public int getScale(final int column) {
        Optional<Table> table = findTableName(column).flatMap(optional -> Optional.ofNullable(sqlFederationSchema.tables().get(optional)));
        return !table.isPresent() || RelDataType.SCALE_NOT_SPECIFIED == table.get().getRowType(typeFactory).getScale() ? 0 : table.get().getRowType(typeFactory).getScale();
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
        RelDataType relDataType = resultColumnType.getFieldList().get(column - 1).getType();
        // TODO remove this logic when calcite supports BigInteger type
        if (relDataType instanceof JavaType && BigInteger.class.isAssignableFrom(((JavaType) relDataType).getJavaClass())) {
            return SqlType.BIGINT.id;
        }
        return null == columnTypeConverter ? relDataType.getSqlTypeName().getJdbcOrdinal() : columnTypeConverter.convertColumnType(relDataType.getSqlTypeName());
    }
    
    @Override
    public String getColumnTypeName(final int column) {
        SqlTypeName originalSqlTypeName = resultColumnType.getFieldList().get(column - 1).getType().getSqlTypeName();
        SqlTypeName convertSqlTypeName = SqlTypeName.getNameForJdbcType(
                null == columnTypeConverter ? originalSqlTypeName.getJdbcOrdinal() : columnTypeConverter.convertColumnType(originalSqlTypeName));
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
        return getColumnClassNameByType(getColumnType(column));
    }
    
    private String getColumnClassNameByType(final int columnType) {
        switch (columnType) {
            case Types.BOOLEAN:
            case Types.BIT:
                return Boolean.class.getName();
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return Integer.class.getName();
            case Types.BIGINT:
                return Long.class.getName();
            case Types.FLOAT:
            case Types.REAL:
                return Float.class.getName();
            case Types.DOUBLE:
                return Double.class.getName();
            case Types.NUMERIC:
            case Types.DECIMAL:
                return BigDecimal.class.getName();
            case Types.DATE:
                return Date.class.getName();
            case Types.TIME:
                return Time.class.getName();
            case Types.TIMESTAMP:
                return Timestamp.class.getName();
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return byte[].class.getName();
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return String.class.getName();
            default:
                return Object.class.getName();
        }
    }

    private Optional<String> findTableName(final int column) {
        Projection projection = expandProjections.size() < column
                ? new ColumnProjection(null, resultColumnType.getFieldList().get(column - 1).getName(), null, databaseType)
                : expandProjections.get(column - 1);
        return projection instanceof ColumnProjection ? Optional.ofNullable(((ColumnProjection) projection).getOriginalTable().getValue()) : Optional.empty();
    }
}
