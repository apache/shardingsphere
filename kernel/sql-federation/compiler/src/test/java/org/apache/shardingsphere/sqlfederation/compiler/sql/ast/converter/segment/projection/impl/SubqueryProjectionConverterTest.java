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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type.SelectStatementConverter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class SubqueryProjectionConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(SubqueryProjectionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertWrapsAliasWhenSubqueryTypeNotExists() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        SubqueryProjectionSegment projectionSegment = new SubqueryProjectionSegment(new SubquerySegment(0, 0, selectStatement, "sub"), "text");
        projectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        SqlNode convertedNode = mock(SqlNode.class);
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(selectStatement)).thenReturn(convertedNode))) {
            Optional<SqlNode> actual = SubqueryProjectionConverter.convert(projectionSegment);
            assertTrue(actual.isPresent());
            SqlBasicCall asCall = (SqlBasicCall) actual.orElse(null);
            assertThat(asCall.getOperator(), is(SqlStdOperatorTable.AS));
            assertThat(asCall.getOperandList().get(0), is(convertedNode));
            SqlIdentifier aliasIdentifier = (SqlIdentifier) asCall.getOperandList().get(1);
            assertThat(aliasIdentifier.names, is(Collections.singletonList("alias")));
        }
    }
    
    @Test
    void assertConvertWrapsExistsWhenSubqueryTypeIsExists() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setSubqueryType(SubqueryType.EXISTS);
        SubqueryProjectionSegment projectionSegment = new SubqueryProjectionSegment(new SubquerySegment(0, 0, selectStatement, "sub"), "text");
        SqlNode convertedNode = mock(SqlNode.class);
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(selectStatement)).thenReturn(convertedNode))) {
            Optional<SqlNode> actual = SubqueryProjectionConverter.convert(projectionSegment);
            assertTrue(actual.isPresent());
            SqlBasicCall existsCall = (SqlBasicCall) actual.orElse(null);
            assertThat(existsCall.getOperator(), is(SqlStdOperatorTable.EXISTS));
            assertThat(existsCall.getOperandList().get(0), is(convertedNode));
        }
    }
}
