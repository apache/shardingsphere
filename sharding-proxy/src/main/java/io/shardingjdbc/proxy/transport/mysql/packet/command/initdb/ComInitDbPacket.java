/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.packet.command.initdb;

import io.shardingjdbc.core.constant.ShardingConstant;
import io.shardingjdbc.proxy.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * COM_INIT_DB command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-init-db.html#packet-COM_INIT_DB">COM_INIT_DB</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComInitDbPacket extends CommandPacket {
    
    private String schemaName;
    
    @Override
    public ComInitDbPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        schemaName = mysqlPacketPayload.readStringEOF();
        log.debug("Schema name received for Sharding-Proxy: {}", schemaName);
        return this;
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        if (ShardingConstant.LOGIC_SCHEMA_NAME.equalsIgnoreCase(schemaName)) {
            return Collections.<DatabaseProtocolPacket>singletonList(new OKPacket(getSequenceId() + 1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(getSequenceId() + 1, 1049, "", "", String.format("Unknown database '%s'", schemaName)));
    }
}
