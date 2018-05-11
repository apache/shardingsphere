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

package io.shardingsphere.proxy.transport.mysql.packet.command.text.initdb;

import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.extern.slf4j.Slf4j;

/**
 * COM_INIT_DB command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-init-db.html#packet-COM_INIT_DB">COM_INIT_DB</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComInitDbPacket extends CommandPacket {
    
    private final String schemaName;
    
    public ComInitDbPacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        schemaName = mysqlPacketPayload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(CommandPacketType.COM_INIT_DB.getValue());
        mysqlPacketPayload.writeStringEOF(schemaName);
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("Schema name received for Sharding-Proxy: {}", schemaName);
        if (ShardingConstant.LOGIC_SCHEMA_NAME.equalsIgnoreCase(schemaName)) {
            return new CommandResponsePackets(new OKPacket(getSequenceId() + 1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        return new CommandResponsePackets(new ErrPacket(getSequenceId() + 1, 1049, "", "", String.format("Unknown database '%s'", schemaName)));
    }
    
    @Override
    public boolean hasMoreResultValue() {
        return false;
    }
    
    @Override
    public DatabaseProtocolPacket getResultValue() {
        return null;
    }
}
