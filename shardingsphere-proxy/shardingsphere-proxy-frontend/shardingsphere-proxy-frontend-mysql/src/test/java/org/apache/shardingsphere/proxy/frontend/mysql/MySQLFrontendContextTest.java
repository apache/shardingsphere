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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class MySQLFrontendContextTest {
    
    @Test
    public void assertIsRequiredSameThreadForConnection() {
        MySQLFrontendContext actual = new MySQLFrontendContext();
        ByteBuf comStmtExecuteMessage = Unpooled.wrappedBuffer(new byte[]{0x00, (byte) MySQLCommandPacketType.COM_STMT_EXECUTE.getValue()});
        ByteBuf comStmtSendLongData = Unpooled.wrappedBuffer(new byte[]{0x00, (byte) MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA.getValue()});
        assertFalse(actual.isRequiredSameThreadForConnection(comStmtExecuteMessage));
        assertTrue(actual.isRequiredSameThreadForConnection(comStmtSendLongData));
        assertTrue(actual.isRequiredSameThreadForConnection(comStmtSendLongData));
        assertTrue(actual.isRequiredSameThreadForConnection(comStmtExecuteMessage));
        assertFalse(actual.isRequiredSameThreadForConnection(comStmtExecuteMessage));
        ByteBuf comStmtCloseMessage = Unpooled.wrappedBuffer(new byte[]{0x00, (byte) MySQLCommandPacketType.COM_STMT_CLOSE.getValue()});
        assertTrue(actual.isRequiredSameThreadForConnection(comStmtCloseMessage));
        assertTrue(actual.isRequiredSameThreadForConnection(comStmtCloseMessage));
        assertTrue(actual.isRequiredSameThreadForConnection(comStmtExecuteMessage));
        assertFalse(actual.isRequiredSameThreadForConnection(comStmtExecuteMessage));
    }
    
    @Test
    public void assertNoEnoughReadableBytes() {
        MySQLFrontendContext actual = new MySQLFrontendContext();
        assertFalse(actual.isRequiredSameThreadForConnection(Unpooled.wrappedBuffer(new byte[1])));
    }
}
