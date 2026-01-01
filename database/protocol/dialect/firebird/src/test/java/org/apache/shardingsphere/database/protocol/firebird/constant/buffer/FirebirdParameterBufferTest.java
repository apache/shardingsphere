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

package org.apache.shardingsphere.database.protocol.firebird.constant.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdValueFormat;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdParameterBufferTest {
    
    @Mock
    private FirebirdParameterBufferType intType;
    
    @Mock
    private FirebirdParameterBufferType booleanType;
    
    @Mock
    private FirebirdParameterBufferType stringType;
    
    @Mock
    private FirebirdParameterBufferType unsupportedType;
    
    @Test
    void assertParseTraditionalBuffer() {
        when(intType.getFormat()).thenReturn(FirebirdValueFormat.INT);
        when(booleanType.getFormat()).thenReturn(FirebirdValueFormat.BOOLEAN);
        when(stringType.getFormat()).thenReturn(FirebirdValueFormat.STRING);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(1);
        buffer.writeByte(1);
        buffer.writeByte(0);
        buffer.writeIntLE(123);
        buffer.writeByte(2);
        buffer.writeByte(3);
        String expectedString = "sharding";
        buffer.writeByte(expectedString.length());
        buffer.writeCharSequence(expectedString, StandardCharsets.UTF_8);
        FirebirdParameterBuffer parameterBuffer = new FirebirdParameterBuffer(createValueOf(), version -> version == 1);
        parameterBuffer.parseBuffer(buffer);
        assertThat(parameterBuffer.getVersion(), is(1));
        assertThat(parameterBuffer.<Integer>getValue(intType), is(123));
        boolean actualBoolean = parameterBuffer.getValue(booleanType);
        assertTrue(actualBoolean);
        assertThat(parameterBuffer.getValue(stringType), is(expectedString));
    }
    
    @Test
    void assertParseExtendedBuffer() {
        when(intType.getFormat()).thenReturn(FirebirdValueFormat.INT);
        when(stringType.getFormat()).thenReturn(FirebirdValueFormat.STRING);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(2);
        buffer.writeByte(1);
        buffer.writeIntLE(0);
        buffer.writeIntLE(789);
        buffer.writeByte(3);
        String expectedString = "firebird";
        buffer.writeIntLE(expectedString.length());
        buffer.writeCharSequence(expectedString, StandardCharsets.UTF_8);
        FirebirdParameterBuffer parameterBuffer = new FirebirdParameterBuffer(createValueOf(), version -> version == 1);
        parameterBuffer.parseBuffer(buffer);
        assertThat(parameterBuffer.getVersion(), is(2));
        assertThat(parameterBuffer.<Integer>getValue(intType), is(789));
        assertThat(parameterBuffer.getValue(stringType), is(expectedString));
    }
    
    @Test
    void assertParseUnsupportedFormat() {
        when(unsupportedType.getFormat()).thenReturn(FirebirdValueFormat.BINARY);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(1);
        buffer.writeByte(4);
        FirebirdParameterBuffer parameterBuffer = new FirebirdParameterBuffer(createValueOf(), version -> version == 1);
        assertThrows(FirebirdProtocolException.class, () -> parameterBuffer.parseBuffer(buffer));
    }
    
    private Function<Integer, FirebirdParameterBufferType> createValueOf() {
        Map<Integer, FirebirdParameterBufferType> mapping = new HashMap<>(4, 1F);
        mapping.put(1, intType);
        mapping.put(2, booleanType);
        mapping.put(3, stringType);
        mapping.put(4, unsupportedType);
        return mapping::get;
    }
}
