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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.string;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdUpperLowerReturnTypeHandlerTest {
    
    private final FirebirdUpperLowerReturnTypeHandler handler = new FirebirdUpperLowerReturnTypeHandler();
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private ColumnSegment columnSegment;
    
    @Test
    void assertGetReturnTypeWithVarcharColumn() {
        stubColumn(Types.VARCHAR);
        assertThat(handler.getReturnType(schema, Collections.singletonList(columnSegment)), is(FirebirdBinaryColumnType.VARYING));
    }
    
    @Test
    void assertGetReturnTypeWithLongVarcharColumn() {
        stubColumn(Types.LONGVARCHAR);
        assertThat(handler.getReturnType(schema, Collections.singletonList(columnSegment)), is(FirebirdBinaryColumnType.BLOB_SUBTYPE_TEXT));
    }
    
    @Test
    void assertGetReturnTypeWithUnmappedColumn() {
        stubColumn(Types.INTEGER);
        assertNull(handler.getReturnType(schema, Collections.singletonList(columnSegment)));
    }
    
    @Test
    void assertGetReturnTypeWithStringLiteral() {
        assertThat(handler.getReturnType(schema, Collections.singletonList(new LiteralExpressionSegment(0, 0, "text"))), is(FirebirdBinaryColumnType.VARYING));
    }
    
    @Test
    void assertGetReturnTypeWithNonStringLiteral() {
        assertThat(handler.getReturnType(schema, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))), is(FirebirdBinaryColumnType.VARYING));
    }
    
    @Test
    void assertGetReturnTypeWithParameterMarker() {
        assertThat(handler.getReturnType(schema, Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0))), is(FirebirdBinaryColumnType.VARYING));
    }
    
    @Test
    void assertGetReturnTypeWithFunctionSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "LOWER", "LOWER");
        functionSegment.getParameters().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        assertThat(handler.getReturnType(schema, Collections.singletonList(functionSegment)), is(FirebirdBinaryColumnType.VARYING));
    }
    
    private void stubColumn(final int dataType) {
        IdentifierValue table = new IdentifierValue("t_order");
        IdentifierValue column = new IdentifierValue("content");
        when(columnSegment.getColumnBoundInfo()).thenReturn(new ColumnSegmentBoundInfo(null, table, column, TableSourceType.PHYSICAL_TABLE));
        ShardingSphereTable tableSegment = mock(ShardingSphereTable.class);
        ShardingSphereColumn columnInfo = new ShardingSphereColumn("content", dataType, false, false, false, true, false, false);
        when(tableSegment.getColumn(column)).thenReturn(columnInfo);
        when(schema.getTable(table)).thenReturn(tableSegment);
    }
}
