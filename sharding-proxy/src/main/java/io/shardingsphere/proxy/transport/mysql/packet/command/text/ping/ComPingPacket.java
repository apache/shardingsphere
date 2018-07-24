/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command.text.ping;

import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * COM_PING command packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-ping.html">COM_PING</a>
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
@Getter
public final class ComPingPacket implements CommandPacket {
    
    private final int sequenceId;
    
    @Override
    public CommandResponsePackets execute() {
        return new CommandResponsePackets(new OKPacket(getSequenceId() + 1));
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_QUIT.getValue());
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public DatabasePacket getResultValue() {
        return null;
    }
}
