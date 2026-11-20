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

package org.apache.shardingsphere.proxy.frontend.firebird.command.admin;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class FirebirdUnsupportedCommandExecutorTest {
    
    @Test
    void assertExecuteWithUnsupportedCommandPacket() {
        FirebirdGenericResponsePacket actual = execute(new FirebirdUnsupportedCommandPacket(FirebirdCommandPacketType.INFO_REQUEST), FirebirdCommandPacketType.INFO_REQUEST);
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_wish_list));
        assertThat(actual.getErrorMessage(), is("Unsupported Firebird command packet: INFO_REQUEST"));
    }
    
    @Test
    void assertExecuteWithUnsupportedCommandExecutor() {
        FirebirdGenericResponsePacket actual = execute(mock(CommandPacket.class), FirebirdCommandPacketType.PREPARE);
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_wish_list));
        assertThat(actual.getErrorMessage(), is("Unsupported Firebird command executor: PREPARE"));
    }
    
    private FirebirdGenericResponsePacket execute(final CommandPacket commandPacket, final FirebirdCommandPacketType commandPacketType) {
        return (FirebirdGenericResponsePacket) new FirebirdUnsupportedCommandExecutor(commandPacket, commandPacketType).execute().iterator().next();
    }
}
