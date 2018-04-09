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

package io.shardingjdbc.proxy.transport.mysql.packet.command;

import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.ErrPacket;

import java.util.Collections;
import java.util.List;

/**
 * Unsupported command packet.
 *
 * @author zhangliang
 */
public final class UnsupportedCommandPacket extends CommandPacket {
    
    private static final int ERROR_CODE = 0xcc;
    
    private static final String SQL_STATE_MARKER = "x";
    
    private static final String SQL_STATE = "xxxxx";
    
    private static final String ERROR_MESSAGE = "Unsupported command packet '%s'.";
    
    private final CommandPacketType type;
    
    public UnsupportedCommandPacket(final int sequenceId, final CommandPacketType type) {
        super(sequenceId);
        this.type = type;
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(getSequenceId() + 1, ERROR_CODE, SQL_STATE_MARKER, SQL_STATE, String.format(ERROR_MESSAGE, type)));
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
    }
}
