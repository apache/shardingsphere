/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.db.protocol.mysql.packet.generic;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.mysql.vendor.MySQLVendorError;

import java.sql.SQLException;

/**
 * ERR packet protocol for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html">ERR Packet</a>
 */
@Getter
public final class MySQLErrPacket extends MySQLPacket {
    
    /**
     * Header of ERR packet.
     */
    public static final int HEADER = 0xff;
    
    private static final String SQL_STATE_MARKER = "#";
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public MySQLErrPacket(final SQLException exception) {
        if (null == exception.getSQLState()) {
            errorCode = MySQLVendorError.ER_INTERNAL_ERROR.getVendorCode();
            sqlState = MySQLVendorError.ER_INTERNAL_ERROR.getSqlState().getValue();
            errorMessage = String.format(MySQLVendorError.ER_INTERNAL_ERROR.getReason(),
                    null == exception.getNextException() || !Strings.isNullOrEmpty(exception.getMessage()) ? exception.getMessage() : exception.getNextException().getMessage());
        } else {
            errorCode = exception.getErrorCode();
            sqlState = exception.getSQLState();
            errorMessage = exception.getMessage();
        }
    }
    
    public MySQLErrPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL ERR packet must be `0xff`.");
        errorCode = payload.readInt2();
        payload.readStringFix(1);
        sqlState = payload.readStringFix(5);
        errorMessage = payload.readStringEOF();
    }
    
    @Override
    protected void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeInt2(errorCode);
        payload.writeStringFix(SQL_STATE_MARKER);
        payload.writeStringFix(sqlState);
        payload.writeStringEOF(errorMessage);
    }
}
