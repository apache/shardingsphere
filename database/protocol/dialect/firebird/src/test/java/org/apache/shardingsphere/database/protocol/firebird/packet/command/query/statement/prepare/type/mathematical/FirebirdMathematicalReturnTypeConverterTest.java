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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.mathematical;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdMathematicalReturnTypeConverterTest {
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private ColumnSegment columnSegment;
    
    @Mock
    private ExpressionSegment otherSegment;
    
    @Test
    void assertConvertWithColumnSegment() {
        IdentifierValue table = new IdentifierValue("t_order");
        IdentifierValue column = new IdentifierValue("order_id");
        when(columnSegment.getColumnBoundInfo()).thenReturn(new ColumnSegmentBoundInfo(null, table, column, TableSourceType.PHYSICAL_TABLE));
        ShardingSphereTable tableSegment = mock(ShardingSphereTable.class);
        ShardingSphereColumn columnInfo = new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false, false);
        when(tableSegment.getColumn(column)).thenReturn(columnInfo);
        when(schema.getTable(table)).thenReturn(tableSegment);
        Map<Integer, FirebirdBinaryColumnType> columnTypeMap = new HashMap<>();
        columnTypeMap.put(Types.INTEGER, FirebirdBinaryColumnType.LONG);
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(columnSegment), FirebirdBinaryColumnType.DOUBLE, columnTypeMap),
                is(FirebirdBinaryColumnType.LONG));
    }
    
    @Test
    void assertConvertWithBigIntegerLiteral() {
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(new LiteralExpressionSegment(0, 0, BigInteger.ONE)),
                FirebirdBinaryColumnType.DOUBLE, Collections.emptyMap()), is(FirebirdBinaryColumnType.INT128));
    }
    
    @Test
    void assertConvertWithBigDecimalLiteral() {
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(new LiteralExpressionSegment(0, 0, BigDecimal.ONE)),
                FirebirdBinaryColumnType.DOUBLE, Collections.emptyMap()), is(FirebirdBinaryColumnType.NUMERIC));
    }
    
    @Test
    void assertConvertWithOtherLiteral() {
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1L)),
                FirebirdBinaryColumnType.DOUBLE, Collections.emptyMap()), is(FirebirdBinaryColumnType.LONG));
    }
    
    @Test
    void assertConvertWithParameterMarker() {
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0)),
                FirebirdBinaryColumnType.DOUBLE, Collections.emptyMap()), is(FirebirdBinaryColumnType.DOUBLE));
    }
    
    @Test
    void assertConvertWithOtherSegment() {
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(otherSegment),
                FirebirdBinaryColumnType.DOUBLE, Collections.emptyMap()), is(FirebirdBinaryColumnType.LONG));
    }
    
    @Test
    void assertConvertWithFunctionSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "MAX", "MAX");
        functionSegment.getParameters().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        assertThat(FirebirdMathematicalReturnTypeConverter.convert(schema, Collections.singletonList(functionSegment),
                FirebirdBinaryColumnType.DOUBLE, Collections.emptyMap()), is(FirebirdBinaryColumnType.LONG));
    }
}
