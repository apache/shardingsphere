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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class MatchExpressionConverterTest {
    
    @Test
    void assertConvertWithOwnerHierarchyAndExpression() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("column"));
        OwnerSegment tableOwner = new OwnerSegment(0, 0, new IdentifierValue("table"));
        OwnerSegment schemaOwner = new OwnerSegment(0, 0, new IdentifierValue("schema"));
        tableOwner.setOwner(schemaOwner);
        columnSegment.setOwner(tableOwner);
        LiteralExpressionSegment expr = new LiteralExpressionSegment(0, 0, "expr");
        SqlNode exprNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(expr)).thenReturn(Optional.of(exprNode));
        MatchAgainstExpression segment = new MatchAgainstExpression(0, 0, expr, "modifier", "");
        segment.getColumns().add(columnSegment);
        SqlBasicCall actual = (SqlBasicCall) MatchExpressionConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SQLExtensionOperatorTable.MATCH_AGAINST));
        SqlIdentifier identifier = (SqlIdentifier) actual.getOperandList().get(0);
        assertThat(identifier.names, is(Arrays.asList("schema", "table", "column")));
        assertThat(actual.getOperandList().get(1), is(exprNode));
        SqlLiteral modifier = (SqlLiteral) actual.getOperandList().get(2);
        assertThat(modifier.getValueAs(String.class), is("modifier"));
    }
    
    @Test
    void assertConvertWithoutExpressionLeavesSearchModifierOnly() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("column"));
        LiteralExpressionSegment expr = new LiteralExpressionSegment(0, 0, "expr");
        when(ExpressionConverter.convert(expr)).thenReturn(Optional.empty());
        MatchAgainstExpression segment = new MatchAgainstExpression(0, 0, expr, "modifier", "");
        segment.getColumns().add(columnSegment);
        SqlBasicCall actual = (SqlBasicCall) MatchExpressionConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperandList().size(), is(2));
        assertThat(actual.getOperandList().get(0), instanceOf(SqlIdentifier.class));
        SqlLiteral modifier = (SqlLiteral) actual.getOperandList().get(1);
        assertThat(modifier.getValueAs(String.class), is("modifier"));
    }
}
