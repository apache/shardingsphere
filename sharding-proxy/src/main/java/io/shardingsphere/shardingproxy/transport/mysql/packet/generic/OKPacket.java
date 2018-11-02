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
import io.shardingsphere.shardingproxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OK packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html">OK Packet</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@RequiredArgsConstructor
@Getter
public final class OKPacket implements MySQLPacket {
    
    /**
     * Header of OK packet.
     */
    public static final int HEADER = 0x00;
    
    private static final int STATUS_FLAG = StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue();
    
    private final int sequenceId;
    
    private final long affectedRows;
    
    private final long lastInsertId;
    
    private final int warnings;
    
    private final String info;
    
    public OKPacket(final int sequenceId) {
        this(sequenceId, 0L, 0L, 0, "");
    }
    
    public OKPacket(final int sequenceId, final long affectedRows, final long lastInsertId) {
        this(sequenceId, affectedRows, lastInsertId, 0, "");
    }
    
    public OKPacket(final MySQLPacketPayload payload) {
        this.sequenceId = payload.readInt1();
        Preconditions.checkArgument(HEADER == payload.readInt1());
        affectedRows = payload.readIntLenenc();
        lastInsertId = payload.readIntLenenc();
        payload.readInt2();
        warnings = payload.readInt2();
        info = payload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeIntLenenc(affectedRows);
        payload.writeIntLenenc(lastInsertId);
        payload.writeInt2(STATUS_FLAG);
        payload.writeInt2(warnings);
        payload.writeStringEOF(info);
    }
}
