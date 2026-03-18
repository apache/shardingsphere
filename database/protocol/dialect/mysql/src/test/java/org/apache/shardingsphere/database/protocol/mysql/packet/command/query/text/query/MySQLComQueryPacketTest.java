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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query;

import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLComQueryPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newWithStringArguments")
    void assertNewWithString(final String name, final String inputSQL, final String expectedSQL, final boolean expectedWriteRouteOnly) {
        MySQLComQueryPacket actual = new MySQLComQueryPacket(inputSQL);
        assertThat(actual.getSQL(), is(expectedSQL));
        assertThat(actual.getHintValueContext().isWriteRouteOnly(), is(expectedWriteRouteOnly));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newWithPayloadArguments")
    void assertNewWithPayload(final String name, final String inputSQL, final String expectedSQL, final boolean expectedWriteRouteOnly) {
        when(payload.readStringEOF()).thenReturn(inputSQL);
        MySQLComQueryPacket actual = new MySQLComQueryPacket(payload);
        assertThat(actual.getSQL(), is(expectedSQL));
        assertThat(actual.getHintValueContext().isWriteRouteOnly(), is(expectedWriteRouteOnly));
    }
    
    @Test
    void assertWrite() {
        new MySQLComQueryPacket("SELECT id FROM tbl").write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_QUERY.getValue());
        verify(payload).writeStringEOF("SELECT id FROM tbl");
    }
    
    @Test
    void assertGetSQL() {
        assertThat(new MySQLComQueryPacket("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ SELECT id FROM tbl").getSQL(), is("SELECT id FROM tbl"));
    }
    
    private static Stream<Arguments> newWithStringArguments() {
        return Stream.of(
                Arguments.of("WithoutHint", "SELECT id FROM tbl", "SELECT id FROM tbl", false),
                Arguments.of("WithHintToken", "/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ SELECT id FROM tbl", "SELECT id FROM tbl", true),
                Arguments.of("WithHintAlias", "/* ShardingSphere hint: WRITE_ROUTE_ONLY=true */ SELECT id FROM tbl", "SELECT id FROM tbl", true));
    }
    
    private static Stream<Arguments> newWithPayloadArguments() {
        return Stream.of(
                Arguments.of("WithoutHint", "SELECT id FROM tbl", "SELECT id FROM tbl", false),
                Arguments.of("WithHintToken", "/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ SELECT id FROM tbl", "SELECT id FROM tbl", true),
                Arguments.of("WithHintAlias", "/* ShardingSphere hint: WRITE_ROUTE_ONLY=true */ SELECT id FROM tbl", "SELECT id FROM tbl", true));
    }
}
