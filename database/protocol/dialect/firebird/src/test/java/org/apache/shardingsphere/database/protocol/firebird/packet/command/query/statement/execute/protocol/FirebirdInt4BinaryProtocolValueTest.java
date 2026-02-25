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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInt4BinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertRead() {
        when(payload.readInt4()).thenReturn(1);
        assertThat(new FirebirdInt4BinaryProtocolValue().read(payload), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Object value, final int expected) {
        new FirebirdInt4BinaryProtocolValue().write(payload, value);
        verify(payload).writeInt4(expected);
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("big decimal", BigDecimal.ONE, 1),
                Arguments.of("integer", 1, 1),
                Arguments.of("long", 1L, 1));
    }
    
    @Test
    void assertGetLength() {
        assertThat(new FirebirdInt4BinaryProtocolValue().getLength(payload), is(4));
    }
}
