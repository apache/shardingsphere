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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExtractArgExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.BetweenExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.BinaryOperationExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.CaseWhenExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.CollateExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.DataTypeExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ExistsSubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ExtractArgExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.FunctionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.InExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.IntervalExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ListExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.LiteralExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.MatchExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.NotExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.QuantifySubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.RowExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.SubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.TypeCastExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.UnaryOperationExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.VariableSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.AggregationProjectionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        LiteralExpressionConverter.class, ListExpressionConverter.class, BinaryOperationExpressionConverter.class,
        ColumnConverter.class, ExistsSubqueryExpressionConverter.class, SubqueryExpressionConverter.class, InExpressionConverter.class,
        BetweenExpressionConverter.class, ParameterMarkerExpressionConverter.class, FunctionConverter.class, AggregationProjectionConverter.class,
        DataTypeExpressionConverter.class, CaseWhenExpressionConverter.class, NotExpressionConverter.class, TypeCastExpressionConverter.class,
        ExtractArgExpressionConverter.class, MatchExpressionConverter.class, CollateExpressionConverter.class, RowExpressionConverter.class,
        VariableSegmentConverter.class, UnaryOperationExpressionConverter.class, IntervalExpressionConverter.class, QuantifySubqueryExpressionConverter.class
})
class ExpressionConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertDelegatesToAllSupportedConverters() {
        SqlNode expectedLiteralNode = mock(SqlNode.class);
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(0, 0, "literal");
        when(LiteralExpressionConverter.convert(literalSegment)).thenReturn(Optional.of(expectedLiteralNode));
        SqlNode expectedListNode = mock(SqlNode.class);
        ListExpression listSegment = new ListExpression(0, 0);
        when(ListExpressionConverter.convert(listSegment)).thenReturn(Optional.of(expectedListNode));
        SqlNode expectedBinaryNode = mock(SqlNode.class);
        BinaryOperationExpression binarySegment = new BinaryOperationExpression(0, 0, literalSegment, literalSegment, "+", "text");
        when(BinaryOperationExpressionConverter.convert(binarySegment)).thenReturn(Optional.of(expectedBinaryNode));
        SqlNode expectedColumnNode = mock(SqlNode.class);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        when(ColumnConverter.convert(columnSegment)).thenReturn(Optional.of(expectedColumnNode));
        SqlNode expectedExistsSubqueryNode = mock(SqlNode.class);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, new SelectStatement(databaseType), "sub");
        ExistsSubqueryExpression existsSubqueryExpression = new ExistsSubqueryExpression(0, 0, subquerySegment);
        when(ExistsSubqueryExpressionConverter.convert(existsSubqueryExpression)).thenReturn(Optional.of(expectedExistsSubqueryNode));
        SqlNode expectedSubqueryNode = mock(SqlNode.class);
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(subquerySegment);
        when(SubqueryExpressionConverter.convert(subqueryExpressionSegment)).thenReturn(Optional.of(expectedSubqueryNode));
        SqlNode expectedInNode = mock(SqlNode.class);
        InExpression inExpression = new InExpression(0, 0, literalSegment, literalSegment, false);
        when(InExpressionConverter.convert(inExpression)).thenReturn(Optional.of(expectedInNode));
        SqlNode expectedBetweenNode = mock(SqlNode.class);
        BetweenExpression betweenExpression = new BetweenExpression(0, 0, literalSegment, literalSegment, literalSegment, false);
        when(BetweenExpressionConverter.convert(betweenExpression)).thenReturn(Optional.of(expectedBetweenNode));
        SqlNode expectedParameterNode = mock(SqlNode.class);
        ParameterMarkerExpressionSegment parameterSegment = new ParameterMarkerExpressionSegment(0, 0, 0);
        when(ParameterMarkerExpressionConverter.convert(parameterSegment)).thenReturn(Optional.of(expectedParameterNode));
        SqlNode expectedFunctionNode = mock(SqlNode.class);
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "func", "func_text");
        when(FunctionConverter.convert(functionSegment)).thenReturn(Optional.of(expectedFunctionNode));
        SqlNode expectedAggregationNode = mock(SqlNode.class);
        AggregationProjectionSegment aggregationSegment = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "count(expr)");
        when(AggregationProjectionConverter.convert(aggregationSegment)).thenReturn(Optional.of(expectedAggregationNode));
        SqlNode expectedDataTypeNode = mock(SqlNode.class);
        DataTypeSegment dataTypeSegment = new DataTypeSegment();
        dataTypeSegment.setStartIndex(0);
        dataTypeSegment.setStopIndex(0);
        dataTypeSegment.setDataTypeName("int");
        when(DataTypeExpressionConverter.convert(dataTypeSegment)).thenReturn(Optional.of(expectedDataTypeNode));
        SqlNode expectedCaseWhenNode = mock(SqlNode.class);
        CaseWhenExpression caseWhenExpression = new CaseWhenExpression(0, 0, literalSegment, Collections.singleton(literalSegment), Collections.singleton(literalSegment), literalSegment);
        when(CaseWhenExpressionConverter.convert(caseWhenExpression)).thenReturn(Optional.of(expectedCaseWhenNode));
        SqlNode expectedNotNode = mock(SqlNode.class);
        NotExpression notExpression = new NotExpression(0, 0, literalSegment, true);
        when(NotExpressionConverter.convert(notExpression)).thenReturn(Optional.of(expectedNotNode));
        SqlNode expectedTypeCastNode = mock(SqlNode.class);
        TypeCastExpression typeCastExpression = new TypeCastExpression(0, 0, "text", literalSegment, "int");
        when(TypeCastExpressionConverter.convert(typeCastExpression)).thenReturn(Optional.of(expectedTypeCastNode));
        SqlNode expectedExtractNode = mock(SqlNode.class);
        ExtractArgExpression extractArgExpression = new ExtractArgExpression(0, 0, "extract");
        when(ExtractArgExpressionConverter.convert(extractArgExpression)).thenReturn(Optional.of(expectedExtractNode));
        SqlNode expectedMatchNode = mock(SqlNode.class);
        MatchAgainstExpression matchAgainstExpression = new MatchAgainstExpression(0, 0, literalSegment, "search", "text");
        when(MatchExpressionConverter.convert(matchAgainstExpression)).thenReturn(Optional.of(expectedMatchNode));
        SqlNode expectedCollateNode = mock(SqlNode.class);
        CollateExpression collateExpression = new CollateExpression(0, 0, literalSegment, literalSegment);
        when(CollateExpressionConverter.convert(collateExpression)).thenReturn(Optional.of(expectedCollateNode));
        SqlNode expectedRowNode = mock(SqlNode.class);
        RowExpression rowExpression = new RowExpression(0, 0, "row");
        when(RowExpressionConverter.convert(rowExpression)).thenReturn(Optional.of(expectedRowNode));
        SqlNode expectedVariableNode = mock(SqlNode.class);
        VariableSegment variableSegment = new VariableSegment(0, 0, "@@session");
        when(VariableSegmentConverter.convert(variableSegment)).thenReturn(Optional.of(expectedVariableNode));
        SqlNode expectedUnaryNode = mock(SqlNode.class);
        UnaryOperationExpression unaryOperationExpression = new UnaryOperationExpression(0, 0, literalSegment, "+", "text");
        when(UnaryOperationExpressionConverter.convert(unaryOperationExpression)).thenReturn(Optional.of(expectedUnaryNode));
        SqlNode expectedIntervalNode = mock(SqlNode.class);
        IntervalExpression intervalExpression = new IntervalExpression(0, 0, literalSegment, IntervalUnit.DAY, "interval");
        when(IntervalExpressionConverter.convert(intervalExpression)).thenReturn(Optional.of(expectedIntervalNode));
        SqlNode expectedQuantifyNode = mock(SqlNode.class);
        QuantifySubqueryExpression quantifySubqueryExpression = new QuantifySubqueryExpression(0, 0, new SubquerySegment(0, 0, new SelectStatement(databaseType), "sub"), "ALL");
        when(QuantifySubqueryExpressionConverter.convert(quantifySubqueryExpression)).thenReturn(Optional.of(expectedQuantifyNode));
        Map<ExpressionSegment, SqlNode> expectations = new LinkedHashMap<>(23, 1F);
        expectations.put(literalSegment, expectedLiteralNode);
        expectations.put(listSegment, expectedListNode);
        expectations.put(binarySegment, expectedBinaryNode);
        expectations.put(columnSegment, expectedColumnNode);
        expectations.put(existsSubqueryExpression, expectedExistsSubqueryNode);
        expectations.put(subqueryExpressionSegment, expectedSubqueryNode);
        expectations.put(inExpression, expectedInNode);
        expectations.put(betweenExpression, expectedBetweenNode);
        expectations.put(parameterSegment, expectedParameterNode);
        expectations.put(functionSegment, expectedFunctionNode);
        expectations.put(aggregationSegment, expectedAggregationNode);
        expectations.put(dataTypeSegment, expectedDataTypeNode);
        expectations.put(caseWhenExpression, expectedCaseWhenNode);
        expectations.put(notExpression, expectedNotNode);
        expectations.put(typeCastExpression, expectedTypeCastNode);
        expectations.put(extractArgExpression, expectedExtractNode);
        expectations.put(matchAgainstExpression, expectedMatchNode);
        expectations.put(collateExpression, expectedCollateNode);
        expectations.put(rowExpression, expectedRowNode);
        expectations.put(variableSegment, expectedVariableNode);
        expectations.put(unaryOperationExpression, expectedUnaryNode);
        expectations.put(intervalExpression, expectedIntervalNode);
        expectations.put(quantifySubqueryExpression, expectedQuantifyNode);
        for (Entry<ExpressionSegment, SqlNode> entry : expectations.entrySet()) {
            Optional<SqlNode> actualOptional = ExpressionConverter.convert(entry.getKey());
            assertTrue(actualOptional.isPresent());
            assertThat(actualOptional.orElse(null), is(entry.getValue()));
        }
    }
    
    @Test
    void assertConvertThrowsUnsupportedForCommonExpression() {
        CommonExpressionSegment expressionSegment = new CommonExpressionSegment(0, 0, "text");
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> ExpressionConverter.convert(expressionSegment));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: unsupported CommonExpressionSegment."));
    }
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(ExpressionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertThrowsUnsupportedForUnknownExpression() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> ExpressionConverter.convert(expressionSegment));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: unsupported TableSegment type: " + expressionSegment.getClass() + "."));
    }
}
