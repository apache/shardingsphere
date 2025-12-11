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
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class ExpressionProjectionConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(ExpressionProjectionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertReturnsEmptyWhenExpressionConversionAbsent() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.empty());
        assertFalse(ExpressionProjectionConverter.convert(new ExpressionProjectionSegment(0, 0, "text", expressionSegment)).isPresent());
    }
    
    @Test
    void assertConvertWrapsResultWithAlias() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        ExpressionProjectionSegment projectionSegment = new ExpressionProjectionSegment(0, 0, "text", expressionSegment);
        projectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        SqlNode expected = mock(SqlNode.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.of(expected));
        SqlBasicCall actual = (SqlBasicCall) ExpressionProjectionConverter.convert(projectionSegment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.AS));
        assertThat(actual.getOperandList().get(0), is(expected));
        SqlIdentifier aliasIdentifier = (SqlIdentifier) actual.getOperandList().get(1);
        assertThat(aliasIdentifier.names, is(Collections.singletonList("alias")));
    }
    
    @Test
    void assertConvertReturnsConvertedExpressionWhenAliasAbsent() {
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        SqlNode expected = mock(SqlNode.class);
        when(ExpressionConverter.convert(expressionSegment)).thenReturn(Optional.of(expected));
        Optional<SqlNode> actual = ExpressionProjectionConverter.convert(new ExpressionProjectionSegment(0, 0, "text", expressionSegment));
        assertTrue(actual.isPresent());
        assertThat(actual.orElse(null), is(expected));
    }
}
