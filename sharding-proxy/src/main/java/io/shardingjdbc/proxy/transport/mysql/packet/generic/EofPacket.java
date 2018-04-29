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

package io.shardingjdbc.proxy.transport.mysql.packet.generic;

import com.google.common.base.Preconditions;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * EOF packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html">EOF Packet</a>
 * 
 * @author zhangliang 
 */
@Getter
public class EofPacket extends MySQLPacket {
    
    private static final int HEADER = 0xfe;
    
    private final int warnings;
    
    private final int statusFlags;
    
    public EofPacket(final int sequenceId, final int warnings, final int statusFlags) {
        super(sequenceId);
        this.warnings = warnings;
        this.statusFlags = statusFlags;
    }
    
    public EofPacket(final MySQLPacketPayload mysqlPacketPayload) {
        super(mysqlPacketPayload.readInt1());
        Preconditions.checkArgument(HEADER == mysqlPacketPayload.readInt1());
        warnings = mysqlPacketPayload.readInt2();
        statusFlags = mysqlPacketPayload.readInt2();
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeInt2(warnings);
        mysqlPacketPayload.writeInt2(statusFlags);
    }
}
