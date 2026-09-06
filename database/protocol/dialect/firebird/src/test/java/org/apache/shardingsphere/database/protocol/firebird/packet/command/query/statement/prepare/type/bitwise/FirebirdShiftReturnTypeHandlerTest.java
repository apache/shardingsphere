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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.bitwise;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class FirebirdShiftReturnTypeHandlerTest {
    
    private final FirebirdShiftReturnTypeHandler handler = new FirebirdShiftReturnTypeHandler();
    
    @Mock
    private ShardingSphereSchema schema;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getReturnTypeArguments")
    void assertGetReturnType(final String name, final ExpressionSegment parameter, final FirebirdBinaryColumnType expected) {
        assertThat(handler.getReturnType(schema, Collections.singletonList(parameter)), is(expected));
    }
    
    private static Stream<Arguments> getReturnTypeArguments() {
        return Stream.of(
                Arguments.of("BigIntegerLiteral", new LiteralExpressionSegment(0, 0, BigInteger.ONE), FirebirdBinaryColumnType.INT128),
                Arguments.of("IntegerLiteral", new LiteralExpressionSegment(0, 0, 1), FirebirdBinaryColumnType.INT64),
                Arguments.of("ParameterMarker", new ParameterMarkerExpressionSegment(0, 0, 0), FirebirdBinaryColumnType.INT64));
    }
}
