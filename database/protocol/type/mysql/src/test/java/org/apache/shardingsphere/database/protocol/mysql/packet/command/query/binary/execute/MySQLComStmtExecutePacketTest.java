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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLComStmtExecutePacketTest {
    
    @Test
    void assertNewWithoutParameter() {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 0);
        assertThat(actual.getStatementId(), is(1));
        assertNull(actual.getNewParametersBoundFlag());
        assertTrue(actual.getNewParameterTypes().isEmpty());
    }
    
    @Test
    void assertNewParameterBoundWithNotNullParameters() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        assertThat(actual.getStatementId(), is(1));
        assertThat(actual.getNewParametersBoundFlag(), is(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST));
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(parameterTypes.size(), is(1));
        assertThat(parameterTypes.get(0).getColumnType(), is(MySQLBinaryColumnType.LONG));
        assertThat(parameterTypes.get(0).getUnsignedFlag(), is(0));
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.singletonList(0)), is(Collections.<Object>singletonList(1)));
    }
    
    @Test
    void assertNewWithNullParameters() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x03, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        assertThat(actual.getStatementId(), is(1));
        assertThat(actual.getNewParametersBoundFlag(), is(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST));
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(parameterTypes.size(), is(1));
        assertThat(parameterTypes.get(0).getColumnType(), is(MySQLBinaryColumnType.LONG));
        assertThat(parameterTypes.get(0).getUnsignedFlag(), is(0));
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.emptyList()), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertNewWithLongDataParameter() throws SQLException {
        byte[] data = {0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0xfc, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        assertThat(actual.getStatementId(), is(2));
        assertThat(actual.getNewParametersBoundFlag(), is(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST));
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(parameterTypes.size(), is(1));
        assertThat(parameterTypes.get(0).getColumnType(), is(MySQLBinaryColumnType.BLOB));
        assertThat(parameterTypes.get(0).getUnsignedFlag(), is(0));
        assertThat(actual.readParameters(parameterTypes, Collections.singleton(0), Collections.emptyList()), is(Collections.singletonList(null)));
    }
}
