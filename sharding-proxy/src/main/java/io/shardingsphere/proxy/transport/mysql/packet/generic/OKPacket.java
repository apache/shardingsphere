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

package io.shardingsphere.proxy.transport.mysql.packet.generic;

import com.google.common.base.Preconditions;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * OK packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html">OK Packet</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@Getter
public class OKPacket extends MySQLPacket {
    
    private static final int HEADER = 0x00;
    
    private final long affectedRows;
    
    private final long lastInsertId;
    
    private final int statusFlags;
    
    private final int warnings;
    
    private final String info;
    
    public OKPacket(final int sequenceId, final long affectedRows, final long lastInsertId, final int statusFlags, final int warnings, final String info) {
        super(sequenceId);
        this.affectedRows = affectedRows;
        this.lastInsertId = lastInsertId;
        this.statusFlags = statusFlags;
        this.warnings = warnings;
        this.info = info;
    }
    
    public OKPacket(final MySQLPacketPayload mysqlPacketPayload) {
        super(mysqlPacketPayload.readInt1());
        Preconditions.checkArgument(HEADER == mysqlPacketPayload.readInt1());
        affectedRows = mysqlPacketPayload.readIntLenenc();
        lastInsertId = mysqlPacketPayload.readIntLenenc();
        statusFlags = mysqlPacketPayload.readInt2();
        warnings = mysqlPacketPayload.readInt2();
        info = mysqlPacketPayload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeIntLenenc(affectedRows);
        mysqlPacketPayload.writeIntLenenc(lastInsertId);
        mysqlPacketPayload.writeInt2(statusFlags);
        mysqlPacketPayload.writeInt2(warnings);
        mysqlPacketPayload.writeStringEOF(info);
    }
}
