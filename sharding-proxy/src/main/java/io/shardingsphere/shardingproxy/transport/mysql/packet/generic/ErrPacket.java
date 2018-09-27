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

package io.shardingsphere.shardingproxy.transport.mysql.packet.generic;

import com.google.common.base.Preconditions;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

/**
 * ERR packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html">ERR Packet</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@RequiredArgsConstructor
@Getter
public final class ErrPacket implements MySQLPacket {
    
    /**
     * Header of ERR packet.
     */
    public static final int HEADER = 0xff;
    
    private static final String SQL_STATE_MARKER = "#";
    
    private final int sequenceId;
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public ErrPacket(final int sequenceId, final ServerErrorCode serverErrorCode, final Object... errorMessageArguments) {
        this(sequenceId, serverErrorCode.getErrorCode(), serverErrorCode.getSqlState(), String.format(serverErrorCode.getErrorMessage(), errorMessageArguments));
    }
    
    public ErrPacket(final int sequenceId, final SQLException cause) {
        this(sequenceId, cause.getErrorCode(), cause.getSQLState(), cause.getMessage());
    }
    
    public ErrPacket(final MySQLPacketPayload payload) {
        sequenceId = payload.readInt1();
        Preconditions.checkArgument(HEADER == payload.readInt1());
        errorCode = payload.readInt2();
        Preconditions.checkArgument(SQL_STATE_MARKER.equals(payload.readStringFix(1)));
        sqlState = payload.readStringFix(5);
        errorMessage = payload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeInt2(errorCode);
        payload.writeStringFix(SQL_STATE_MARKER);
        payload.writeStringFix(sqlState);
        payload.writeStringEOF(errorMessage);
    }
}
