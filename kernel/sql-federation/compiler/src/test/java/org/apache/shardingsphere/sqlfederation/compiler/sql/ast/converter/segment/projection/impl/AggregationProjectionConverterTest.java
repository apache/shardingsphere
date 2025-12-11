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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class AggregationProjectionConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(AggregationProjectionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertBuildsDistinctAggregationWithAliasAndStarParameter() {
        AggregationDistinctProjectionSegment segment = new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)", "COUNT(*)");
        segment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<SqlNode> actual = AggregationProjectionConverter.convert(segment);
        assertTrue(actual.isPresent());
        SqlBasicCall asCall = (SqlBasicCall) actual.orElse(null);
        assertThat(asCall.getOperator(), is(SqlStdOperatorTable.AS));
        SqlBasicCall innerCall = (SqlBasicCall) asCall.getOperandList().get(0);
        assertThat(innerCall.getOperator(), is(SqlStdOperatorTable.COUNT));
        assertNotNull(innerCall.getFunctionQuantifier());
        assertThat(innerCall.getOperandList().size(), is(1));
        assertThat(innerCall.getOperandList().get(0), instanceOf(SqlIdentifier.class));
        SqlIdentifier starIdentifier = (SqlIdentifier) innerCall.getOperandList().get(0);
        assertTrue(starIdentifier.isStar());
        SqlIdentifier aliasIdentifier = (SqlIdentifier) asCall.getOperandList().get(1);
        assertThat(aliasIdentifier.names, is(Collections.singletonList("alias")));
    }
    
    @Test
    void assertConvertBuildsAggregationWithParametersAndSeparator() {
        AggregationProjectionSegment segment = new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(expr)", "|");
        ExpressionSegment firstParam = mock(ExpressionSegment.class);
        ExpressionSegment secondParam = mock(ExpressionSegment.class);
        segment.getParameters().addAll(Arrays.asList(firstParam, secondParam));
        SqlNode firstNode = mock(SqlNode.class);
        SqlNode secondNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(firstParam)).thenReturn(Optional.of(firstNode));
        when(ExpressionConverter.convert(secondParam)).thenReturn(Optional.of(secondNode));
        Optional<SqlNode> actual = AggregationProjectionConverter.convert(segment);
        SqlBasicCall sqlBasicCall = (SqlBasicCall) actual.orElse(null);
        assertNotNull(sqlBasicCall);
        assertThat(sqlBasicCall.getOperator(), is(SqlStdOperatorTable.SUM));
        assertThat(sqlBasicCall.getOperandList().size(), is(3));
        assertThat(sqlBasicCall.getOperandList().get(0), is(firstNode));
        assertThat(sqlBasicCall.getOperandList().get(1), is(secondNode));
        assertThat(sqlBasicCall.getOperandList().get(2), instanceOf(SqlLiteral.class));
        assertThat(sqlBasicCall.getFunctionQuantifier(), is(nullValue()));
    }
    
    @Test
    void assertConvertBuildsAggregationWithParametersWithoutSeparator() {
        AggregationProjectionSegment segment = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "MAX(expr)");
        ExpressionSegment param = mock(ExpressionSegment.class);
        segment.getParameters().add(param);
        SqlNode expectedNode = mock(SqlNode.class);
        when(ExpressionConverter.convert(param)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = AggregationProjectionConverter.convert(segment);
        SqlBasicCall sqlBasicCall = (SqlBasicCall) actual.orElse(null);
        assertNotNull(sqlBasicCall);
        assertThat(sqlBasicCall.getOperator(), is(SqlStdOperatorTable.MAX));
        assertThat(sqlBasicCall.getOperandList().size(), is(1));
        assertThat(sqlBasicCall.getOperandList().get(0), is(expectedNode));
        assertThat(sqlBasicCall.getFunctionQuantifier(), is(nullValue()));
    }
    
    @Test
    void assertConvertThrowsExceptionForUnsupportedOperator() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> AggregationProjectionConverter.convert(new AggregationProjectionSegment(0, 0, AggregationType.PRODUCT, "PRODUCT(expr)")));
        assertThat(ex.getMessage(), is("Unsupported SQL operator: `PRODUCT`"));
    }
}
