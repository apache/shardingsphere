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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import lombok.Getter;
import lombok.ToString;

/**
 * COM_REGISTER_SLAVE command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-register-slave.html">COM_REGISTER_SLAVE</a>
 */
@Getter
@ToString
public final class MySQLComRegisterSlaveCommandPacket extends MySQLCommandPacket {
    
    private final int serverId;
    
    private final String slaveHostname;
    
    private final String slaveUser;
    
    private final String slavePassword;
    
    private final int slavePort;
    
    private final int masterId;
    
    public MySQLComRegisterSlaveCommandPacket(final int serverId, final String slaveHostname, final String slaveUser, final String slavePassword, final int slavePort) {
        super(MySQLCommandPacketType.COM_REGISTER_SLAVE);
        this.serverId = serverId;
        this.slaveHostname = slaveHostname;
        this.slaveUser = slaveUser;
        this.slavePassword = slavePassword;
        this.slavePort = slavePort;
        masterId = 0;
    }
    
    public MySQLComRegisterSlaveCommandPacket(final MySQLPacketPayload payload) {
        super(MySQLCommandPacketType.COM_REGISTER_SLAVE);
        serverId = payload.readInt4();
        slaveHostname = payload.readStringFix(payload.readInt1());
        slaveUser = payload.readStringFix(payload.readInt1());
        slavePassword = payload.readStringFix(payload.readInt1());
        slavePort = payload.readInt2();
        payload.skipReserved(4);
        masterId = payload.readInt4();
    }
    
    @Override
    protected void doWrite(final MySQLPacketPayload payload) {
        payload.writeInt4(serverId);
        payload.writeInt1(slaveHostname.getBytes().length);
        payload.writeStringFix(slaveHostname);
        payload.writeInt1(slaveUser.getBytes().length);
        payload.writeStringFix(slaveUser);
        payload.writeInt1(slavePassword.getBytes().length);
        payload.writeStringFix(slavePassword);
        payload.writeInt2(slavePort);
        payload.writeBytes(new byte[4]);
        payload.writeInt4(masterId);
    }
}
