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

import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ColumnConverter.class)
class ColumnProjectionConverterTest {
    
    @Test
    void assertConvertWrapsWithAliasWhenPresent() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        ColumnProjectionSegment projectionSegment = new ColumnProjectionSegment(columnSegment);
        projectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        SqlIdentifier columnNode = new SqlIdentifier("col", SqlParserPos.ZERO);
        when(ColumnConverter.convert(columnSegment)).thenReturn(columnNode);
        SqlBasicCall actual = (SqlBasicCall) ColumnProjectionConverter.convert(projectionSegment);
        assertThat(actual.getOperator(), instanceOf(SqlAsOperator.class));
        assertThat(actual.getOperandList().get(0), is(columnNode));
        assertThat(((SqlIdentifier) actual.getOperandList().get(1)).names, is(Collections.singletonList("alias")));
    }
    
    @Test
    void assertConvertDelegatesWhenAliasAbsent() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        SqlIdentifier expected = mock(SqlIdentifier.class);
        when(ColumnConverter.convert(columnSegment)).thenReturn(expected);
        assertThat(ColumnProjectionConverter.convert(new ColumnProjectionSegment(columnSegment)), is(expected));
    }
}
