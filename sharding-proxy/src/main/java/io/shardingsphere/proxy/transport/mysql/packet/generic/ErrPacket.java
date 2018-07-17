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
import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

import java.sql.SQLException;

/**
 * ERR packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html">ERR Packet</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@Getter
public final class ErrPacket extends MySQLPacket {
    
    private static final int HEADER = 0xff;
    
    private static final String SQL_STATE_MARKER = "#";
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public ErrPacket(final int sequenceId, final ServerErrorCode serverErrorCode, final Object... errorMessageArguments) {
        this(sequenceId, serverErrorCode.getErrorCode(), serverErrorCode.getSqlState(), String.format(serverErrorCode.getErrorMessage(), errorMessageArguments));
    }
    
    public ErrPacket(final int sequenceId, final SQLException cause) {
        this(sequenceId, cause.getErrorCode(), cause.getSQLState(), cause.getMessage());
    }
    
    private ErrPacket(final int sequenceId, final int errorCode, final String sqlState, final String errorMessage) {
        super(sequenceId);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
    }
    
    public ErrPacket(final MySQLPacketPayload mysqlPacketPayload) {
        super(mysqlPacketPayload.readInt1());
        Preconditions.checkArgument(HEADER == mysqlPacketPayload.readInt1());
        errorCode = mysqlPacketPayload.readInt2();
        Preconditions.checkArgument(SQL_STATE_MARKER.equals(mysqlPacketPayload.readStringFix(1)));
        sqlState = mysqlPacketPayload.readStringFix(5);
        errorMessage = mysqlPacketPayload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeInt2(errorCode);
        mysqlPacketPayload.writeStringFix(SQL_STATE_MARKER);
        mysqlPacketPayload.writeStringFix(sqlState);
        mysqlPacketPayload.writeStringEOF(errorMessage);
    }
}
