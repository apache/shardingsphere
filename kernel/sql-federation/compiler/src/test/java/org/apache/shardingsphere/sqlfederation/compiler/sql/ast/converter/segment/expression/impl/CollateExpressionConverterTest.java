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
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class CollateExpressionConverterTest {
    
    @Test
    void assertConvertCollateExpression() {
        ExpressionSegment expr = new LiteralExpressionSegment(0, 0, "value");
        LiteralExpressionSegment collateName = new LiteralExpressionSegment(0, 0, "collation");
        CollateExpression collateExpression = new CollateExpression(0, 0, collateName, expr);
        SqlNode exprNode = mock(SqlNode.class);
        SqlNode collateNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(expr)).thenReturn(Optional.of(exprNode));
        when(ExpressionConverter.convert(collateName)).thenReturn(Optional.of(collateNode));
        SqlBasicCall actual = CollateExpressionConverter.convert(collateExpression);
        assertThat(actual.getOperator(), is(SQLExtensionOperatorTable.COLLATE));
        assertThat(actual.getOperandList().get(0), is(exprNode));
        assertThat(actual.getOperandList().get(1), is(collateNode));
    }
    
    @Test
    void assertConvertCollateExpressionWithEmptyParts() {
        LiteralExpressionSegment collateName = new LiteralExpressionSegment(0, 0, "collation");
        CollateExpression collateExpression = new CollateExpression(0, 0, collateName, null);
        when(ExpressionConverter.convert(collateName)).thenReturn(Optional.empty());
        SqlBasicCall actual = CollateExpressionConverter.convert(collateExpression);
        assertThat(actual.getOperandList().get(0), isA(SqlNodeList.class));
        assertTrue(((SqlNodeList) actual.getOperandList().get(0)).isEmpty());
        assertThat(actual.getOperandList().get(1), isA(SqlNodeList.class));
        assertTrue(((SqlNodeList) actual.getOperandList().get(1)).isEmpty());
    }
}
