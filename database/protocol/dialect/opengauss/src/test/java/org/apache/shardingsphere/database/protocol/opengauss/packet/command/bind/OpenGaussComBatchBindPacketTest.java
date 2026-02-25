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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command.bind;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OpenGaussComBatchBindPacketTest {
    
    private static final byte[] BATCH_BIND_MESSAGE_BYTES = {
            'U', 0x00, 0x00, 0x00, 0x55, 0x00, 0x00, 0x00,
            0x03, 0x00, 'S', '_', '1', 0x00, 0x00, 0x03,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x31, 0x00,
            0x00, 0x00, 0x03, 0x46, 0x6f, 0x6f, 0x00, 0x00,
            0x00, 0x02, 0x31, 0x38, 0x00, 0x00, 0x00, 0x01,
            0x32, 0x00, 0x00, 0x00, 0x03, 0x42, 0x61, 0x72,
            0x00, 0x00, 0x00, 0x02, 0x33, 0x36, 0x00, 0x00,
            0x00, 0x01, 0x33, 0x00, 0x00, 0x00, 0x03, 0x54,
            0x6f, 0x6d, 0x00, 0x00, 0x00, 0x02, 0x35, 0x34,
            0x45, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    
    @Test
    void assertConstructOpenGaussComBatchBindPacket() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(BATCH_BIND_MESSAGE_BYTES), StandardCharsets.UTF_8);
        assertThat(payload.readInt1(), is((int) 'U'));
        OpenGaussComBatchBindPacket actual = new OpenGaussComBatchBindPacket(payload);
        assertThat(actual.getStatementId(), is("S_1"));
        assertThat(actual.getEachGroupParametersCount(), is(3));
        assertThat(actual.getParameterFormats(), is(Arrays.asList(0, 0, 0)));
        assertTrue(actual.getResultFormats().isEmpty());
        List<List<Object>> actualParameterSets = actual.readParameterSets(Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.VARCHAR, PostgreSQLColumnType.INT4));
        assertThat(actualParameterSets.size(), is(3));
        assertThat(actualParameterSets, is(Arrays.asList(Arrays.asList(1, "Foo", 18), Arrays.asList(2, "Bar", 36), Arrays.asList(3, "Tom", 54))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("readParameterSetsCases")
    void assertReadParameterSets(final String name, final OpenGaussComBatchBindPacket packet, final PostgreSQLPacketPayload payload,
                                 final List<PostgreSQLColumnType> parameterTypes, final List<List<Object>> expectedParameterSets, final int expectedSkipReservedBytes) {
        List<List<Object>> actualParameterSets = packet.readParameterSets(parameterTypes);
        assertThat(actualParameterSets, is(expectedParameterSets));
        verify(payload).skipReserved(expectedSkipReservedBytes);
    }
    
    @Test
    void assertWrite() {
        PostgreSQLPacketPayload actual = mock(PostgreSQLPacketPayload.class);
        assertDoesNotThrow(() -> createPacketByMessage().write(actual));
        verifyNoInteractions(actual);
    }
    
    @Test
    void assertGetIdentifier() {
        assertThat(createPacketByMessage().getIdentifier(), is(OpenGaussCommandPacketType.BATCH_BIND_COMMAND));
    }
    
    private static Stream<Arguments> readParameterSetsCases() {
        PostgreSQLPacketPayload textPayload = mock(PostgreSQLPacketPayload.class);
        ByteBuf textByteBuf = mock(ByteBuf.class);
        when(textPayload.getByteBuf()).thenReturn(textByteBuf);
        when(textPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(textPayload.readInt4()).thenReturn(0, 1, 1, 3);
        when(textPayload.readStringNul()).thenReturn("", "S_1");
        when(textPayload.readInt2()).thenReturn(1, 0, 1, 0, 2);
        when(textByteBuf.readCharSequence(1, StandardCharsets.UTF_8)).thenReturn("7");
        when(textByteBuf.readCharSequence(3, StandardCharsets.UTF_8)).thenReturn("foo");
        when(textByteBuf.readableBytes()).thenReturn(6);
        PostgreSQLPacketPayload emptyFormatPayload = mock(PostgreSQLPacketPayload.class);
        ByteBuf emptyFormatByteBuf = mock(ByteBuf.class);
        when(emptyFormatPayload.getByteBuf()).thenReturn(emptyFormatByteBuf);
        when(emptyFormatPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(emptyFormatPayload.readInt4()).thenReturn(0, 1, 2);
        when(emptyFormatPayload.readStringNul()).thenReturn("", "S_2");
        when(emptyFormatPayload.readInt2()).thenReturn(0, 0, 1);
        when(emptyFormatByteBuf.readCharSequence(2, StandardCharsets.UTF_8)).thenReturn("42");
        when(emptyFormatByteBuf.readableBytes()).thenReturn(0);
        PostgreSQLPacketPayload binaryPayload = mock(PostgreSQLPacketPayload.class);
        ByteBuf binaryByteBuf = mock(ByteBuf.class);
        when(binaryPayload.getByteBuf()).thenReturn(binaryByteBuf);
        when(binaryPayload.readInt4()).thenReturn(0, 1, -1, 4, 9);
        when(binaryPayload.readStringNul()).thenReturn("", "S_3");
        when(binaryPayload.readInt2()).thenReturn(2, 1, 1, 0, 2);
        when(binaryByteBuf.readableBytes()).thenReturn(3);
        OpenGaussComBatchBindPacket textPacket = new OpenGaussComBatchBindPacket(textPayload);
        OpenGaussComBatchBindPacket emptyFormatPacket = new OpenGaussComBatchBindPacket(emptyFormatPayload);
        OpenGaussComBatchBindPacket binaryPacket = new OpenGaussComBatchBindPacket(binaryPayload);
        return Stream.of(
                Arguments.of("textParametersWithSingleTextFormat", textPacket, textPayload, Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.VARCHAR),
                        Collections.singletonList(Arrays.asList(7, "foo")), 6),
                Arguments.of("textParametersWhenFormatListIsEmpty", emptyFormatPacket, emptyFormatPayload, Collections.singletonList(PostgreSQLColumnType.INT4),
                        Collections.singletonList(Collections.singletonList(42)), 0),
                Arguments.of("nullAndBinaryParameters", binaryPacket, binaryPayload, Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.INT4),
                        Collections.singletonList(Arrays.asList(null, 9)), 3));
    }
    
    private static OpenGaussComBatchBindPacket createPacketByMessage() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(BATCH_BIND_MESSAGE_BYTES), StandardCharsets.UTF_8);
        payload.readInt1();
        return new OpenGaussComBatchBindPacket(payload);
    }
}
