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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComDescribePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertNewInstance() {
        when(payload.readInt1()).thenReturn((int) 'P');
        when(payload.readStringNul()).thenReturn("P_1");
        PostgreSQLComDescribePacket actual = new PostgreSQLComDescribePacket(payload);
        assertThat(actual.getType(), is('P'));
        assertThat(actual.getName(), is("P_1"));
    }
    
    @Test
    void assertIdentifier() {
        PostgreSQLComDescribePacket actual = new PostgreSQLComDescribePacket(payload);
        assertThat(actual.getIdentifier(), is(PostgreSQLCommandPacketType.DESCRIBE_COMMAND));
    }
}
