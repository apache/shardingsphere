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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBitBinlogProtocolValueTest {
    
    @Mock
    private MySQLBinlogColumnDef columnDef;
    
    @Mock
    private MySQLPacketPayload payload;
    
    private MySQLBitBinlogProtocolValue actual;
    
    @BeforeEach
    void setUp() {
        actual = new MySQLBitBinlogProtocolValue();
    }
    
    @ParameterizedTest(name = "withLength{1}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertRead(final int columnMeta, final int length, final long expected) {
        when(columnDef.getColumnMeta()).thenReturn(columnMeta);
        when(payload.readLong(length)).thenReturn(expected);
        assertThat(actual.read(columnDef, payload), is(expected));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(1, 1, 1L),
                    Arguments.of(516, 3, 1L),
                    Arguments.of(1280, 5, 1L),
                    Arguments.of(2048, 8, -1L));
        }
    }
}
