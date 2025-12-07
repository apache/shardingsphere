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
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.lookup.Lookup;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLFederationResultSetMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetColumnCount() {
        Map<Integer, String> labels = new HashMap<>(2, 1F);
        labels.put(1, "c1");
        labels.put(2, "c2");
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"c1", "c2"}, mock(RelDataType.class), mock(RelDataType.class)), labels, mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnCount(), is(2));
    }
    
    @Test
    void assertIsAutoIncrement() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"c1"}, mock(RelDataType.class)), Collections.singletonMap(1, "c1"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isAutoIncrement(1), is(false));
    }
    
    @Test
    void assertIsCaseSensitive() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"c1"}, mock(RelDataType.class)), Collections.singletonMap(1, "c1"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isCaseSensitive(1), is(false));
    }
    
    @Test
    void assertIsSearchable() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"c1"}, mock(RelDataType.class)), Collections.singletonMap(1, "c1"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isSearchable(1), is(false));
    }
    
    @Test
    void assertIsCurrency() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"c1"}, mock(RelDataType.class)), Collections.singletonMap(1, "c1"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isCurrency(1), is(false));
    }
    
    @Test
    void assertIsNullableWhenTableNotNullable() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(false, 0, 0));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isNullable(1), is(ResultSetMetaData.columnNoNulls));
    }
    
    @Test
    void assertIsNullableWhenTableNullable() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(true, 0, 0));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isNullable(1), is(ResultSetMetaData.columnNullable));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertIsNullableWhenTableMissing() {
        Lookup<Table> tables = mock(Lookup.class);
        Schema schema = mock(Schema.class);
        when(schema.tables()).thenReturn(tables);
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isNullable(1), is(ResultSetMetaData.columnNullable));
    }
    
    @Test
    void assertIsSigned() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"c1"}, mock(RelDataType.class)), Collections.singletonMap(1, "c1"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isSigned(1), is(true));
    }
    
    @Test
    void assertGetColumnDisplaySizeWithTable() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(false, 8, 0));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnDisplaySize(1), is(8));
    }
    
    @Test
    void assertGetColumnDisplaySizeWithoutTable() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnDisplaySize(1), is(0));
    }
    
    @Test
    void assertGetColumnLabelFromIndexLabel() {
        Map<Integer, String> labels = Collections.singletonMap(1, "foo_label");
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), labels, mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnLabel(1), is("foo_label"));
    }
    
    @Test
    void assertGetColumnLabelFallbackToResultField() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.emptyMap(), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnLabel(1), is("foo_col"));
    }
    
    @Test
    void assertGetTableNameWithAutoProjection() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getTableName(1), is(""));
    }
    
    @Test
    void assertGetColumnLabelFallbackForSecondColumn() {
        Map<Integer, String> labels = Collections.singletonMap(1, "foo_label");
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col", "col2"}, mock(RelDataType.class), mock(RelDataType.class)), labels, mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnLabel(2), is("col2"));
    }
    
    @Test
    void assertGetColumnNameFallbackForSecondColumn() {
        Projection projection = mock(Projection.class);
        when(projection.getColumnName()).thenReturn("projection_col1");
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(projection),
                databaseType, createResultType(new String[]{"foo_col", "col2"}, mock(RelDataType.class), mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"),
                mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnName(2), is("col2"));
    }
    
    @Test
    void assertGetColumnNameFromProjection() {
        Projection projection = mock(Projection.class);
        when(projection.getColumnName()).thenReturn("projection_col");
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(projection),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnName(1), is("projection_col"));
    }
    
    @Test
    void assertGetColumnNameFromResultType() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"fallback_col"}, mock(RelDataType.class)), Collections.emptyMap(), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnName(1), is("fallback_col"));
    }
    
    @Test
    void assertGetSchemaName() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getSchemaName(1), is(DefaultDatabase.LOGIC_NAME));
    }
    
    @Test
    void assertGetPrecisionWithTable() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(false, 12, 0));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getPrecision(1), is(12));
    }
    
    @Test
    void assertGetPrecisionNotSpecified() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(false, RelDataType.PRECISION_NOT_SPECIFIED, 0));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getPrecision(1), is(0));
    }
    
    @Test
    void assertGetPrecisionWithoutTable() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getPrecision(1), is(0));
    }
    
    @Test
    void assertGetPrecisionWithAutoProjectionAndMatchedTable() {
        Schema schema = createSchemaWithTable("", createRowType(false, 7, 0));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getPrecision(1), is(7));
    }
    
    @Test
    void assertGetScaleWithTable() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(false, 0, 4));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getScale(1), is(4));
    }
    
    @Test
    void assertGetScaleNotSpecified() {
        Schema schema = createSchemaWithTable("foo_tbl", createRowType(false, 0, RelDataType.SCALE_NOT_SPECIFIED));
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(schema, Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getScale(1), is(0));
    }
    
    @Test
    void assertGetScaleWithoutTable() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getScale(1), is(0));
    }
    
    @Test
    void assertGetTableNameFromColumnProjection() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(new ColumnProjection("foo_tbl", "foo_col", null, databaseType)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getTableName(1), is("foo_tbl"));
    }
    
    @Test
    void assertGetTableNameFromNonColumnProjection() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.singletonList(mock(Projection.class)),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getTableName(1), is(""));
    }
    
    @Test
    void assertGetCatalogName() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getCatalogName(1), is(DefaultDatabase.LOGIC_NAME));
    }
    
    @Test
    void assertGetColumnTypeForBigInteger() {
        JavaType javaBigIntegerType = mock(JavaType.class);
        when(javaBigIntegerType.getJavaClass()).thenReturn(BigInteger.class);
        when(javaBigIntegerType.getSqlTypeName()).thenReturn(SqlTypeName.DECIMAL);
        RelDataType resultType = createResultType(new String[]{"foo_col"}, javaBigIntegerType);
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(), Collections.emptyList(), databaseType, resultType, Collections.singletonMap(1, "foo_label"), mock());
        assertThat(metaData.getColumnType(1), is(SqlType.BIGINT.id));
    }
    
    @Test
    void assertGetColumnTypeForRegularType() {
        RelDataType intType = mock(RelDataType.class);
        when(intType.getSqlTypeName()).thenReturn(SqlTypeName.INTEGER);
        SQLFederationColumnTypeConverter converter = mock(SQLFederationColumnTypeConverter.class);
        when(converter.convertColumnType(intType.getSqlTypeName())).thenReturn(Types.INTEGER);
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, intType), Collections.singletonMap(1, "foo_label"), converter);
        assertThat(metaData.getColumnType(1), is(Types.INTEGER));
    }
    
    @Test
    void assertGetColumnTypeNameWithMapping() {
        RelDataType intType = mock(RelDataType.class);
        when(intType.getSqlTypeName()).thenReturn(SqlTypeName.INTEGER);
        SQLFederationColumnTypeConverter converter = mock(SQLFederationColumnTypeConverter.class);
        when(converter.convertColumnType(intType.getSqlTypeName())).thenReturn(Types.INTEGER);
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, intType), Collections.singletonMap(1, "foo_label"), converter);
        assertThat(metaData.getColumnTypeName(1), is(SqlTypeName.INTEGER.getName()));
    }
    
    @Test
    void assertGetColumnTypeNameWithoutMapping() {
        RelDataType varcharType = mock(RelDataType.class);
        when(varcharType.getSqlTypeName()).thenReturn(SqlTypeName.VARCHAR);
        SQLFederationColumnTypeConverter converter = mock(SQLFederationColumnTypeConverter.class);
        when(converter.convertColumnType(varcharType.getSqlTypeName())).thenReturn(999);
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, varcharType), Collections.singletonMap(1, "foo_label"), converter);
        assertThat(metaData.getColumnTypeName(1), is(SqlTypeName.VARCHAR.getName()));
    }
    
    @Test
    void assertIsReadOnly() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isReadOnly(1), is(false));
    }
    
    @Test
    void assertIsWritable() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isWritable(1), is(false));
    }
    
    @Test
    void assertIsDefinitelyWritable() {
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(Schema.class), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, mock(RelDataType.class)), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.isDefinitelyWritable(1), is(false));
    }
    
    @Test
    void assertGetColumnClassName() {
        RelDataType varcharType = mock(RelDataType.class);
        when(varcharType.getSqlTypeName()).thenReturn(SqlTypeName.VARCHAR);
        SQLFederationResultSetMetaData metaData = new SQLFederationResultSetMetaData(mock(), Collections.emptyList(),
                databaseType, createResultType(new String[]{"foo_col"}, varcharType), Collections.singletonMap(1, "foo_label"), mock(SQLFederationColumnTypeConverter.class));
        assertThat(metaData.getColumnClassName(1), is(SqlTypeName.VARCHAR.getClass().getName()));
    }
    
    private RelDataType createRowType(final boolean nullable, final int precision, final int scale) {
        RelDataType result = mock(RelDataType.class);
        when(result.isNullable()).thenReturn(nullable);
        when(result.getPrecision()).thenReturn(precision);
        when(result.getScale()).thenReturn(scale);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Schema createSchemaWithTable(final String tableName, final RelDataType rowType) {
        Table table = mock(Table.class);
        when(table.getRowType(any(RelDataTypeFactory.class))).thenReturn(rowType);
        Lookup<Table> tables = mock(Lookup.class);
        when(tables.get(tableName)).thenReturn(table);
        Schema result = mock(Schema.class);
        when(result.tables()).thenReturn(tables);
        return result;
    }
    
    private RelDataType createResultType(final String[] names, final RelDataType... types) {
        List<RelDataTypeField> fields = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            RelDataTypeField field = mock(RelDataTypeField.class);
            when(field.getName()).thenReturn(names[i]);
            when(field.getType()).thenReturn(types[i]);
            fields.add(field);
        }
        RelDataType result = mock(RelDataType.class);
        when(result.getFieldList()).thenReturn(fields);
        return result;
    }
}
