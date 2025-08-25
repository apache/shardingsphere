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

package org.apache.shardingsphere.database.protocol.mysql.packet.generic;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;

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
        errorCode = exception.getErrorCode();
        sqlState = exception.getSQLState();
        errorMessage = exception.getMessage();
    }
    
    public MySQLErrPacket(final VendorError vendorError, final Object... errorMessageArgs) {
        errorCode = vendorError.getVendorCode();
        sqlState = vendorError.getSqlState().getValue();
        errorMessage = String.format(vendorError.getReason(), errorMessageArgs);
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
