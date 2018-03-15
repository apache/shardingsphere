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

import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLSentPacket;
import lombok.Getter;

/**
 * ERR packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html">ERR Packet</a>
 * 
 * @author zhangliang 
 */
@Getter
public class ErrPacket extends MySQLSentPacket {
    
    private static final int HEADER = 0xff;
    
    private final int errorCode;
    
    private final String sqlStateMarker;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public ErrPacket(final int sequenceId, final int errorCode, final String sqlStateMarker, final String sqlState, final String errorMessage) {
        setSequenceId(sequenceId);
        this.errorCode = errorCode;
        this.sqlStateMarker = sqlStateMarker;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeInt2(errorCode);
        mysqlPacketPayload.writeStringFix(sqlStateMarker);
        mysqlPacketPayload.writeStringFix(sqlState);
        mysqlPacketPayload.writeStringEOF(errorMessage);
    }
}
