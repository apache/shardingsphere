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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.cast;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class FirebirdCastReturnTypeHandlerTest {
    
    private final FirebirdCastReturnTypeHandler handler = new FirebirdCastReturnTypeHandler();
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    void assertGetReturnTypeWithDataTypeSegment() {
        DataTypeSegment dataTypeSegment = new DataTypeSegment();
        dataTypeSegment.setDataTypeName("INTEGER");
        assertThat(handler.getReturnType(schema, Arrays.asList(new LiteralExpressionSegment(0, 0, "1"), dataTypeSegment)), is(FirebirdBinaryColumnType.LONG));
    }
    
    @Test
    void assertGetReturnTypeWithUnknownDataTypeName() {
        DataTypeSegment dataTypeSegment = new DataTypeSegment();
        dataTypeSegment.setDataTypeName("UNKNOWN");
        assertNull(handler.getReturnType(schema, Arrays.asList(new LiteralExpressionSegment(0, 0, "1"), dataTypeSegment)));
    }
    
    @Test
    void assertGetReturnTypeWithNonDataTypeSegment() {
        assertThat(handler.getReturnType(schema, Arrays.asList(new LiteralExpressionSegment(0, 0, "1"), new LiteralExpressionSegment(0, 0, "2"))), is(FirebirdBinaryColumnType.LONG));
    }
    
    @Test
    void assertGetReturnTypeWithSingleParameter() {
        assertThat(handler.getReturnType(schema, Collections.singletonList(new LiteralExpressionSegment(0, 0, "1"))), is(FirebirdBinaryColumnType.LONG));
    }
}
