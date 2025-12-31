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

package org.apache.shardingsphere.database.protocol.firebird.err;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

/**
 * Firebird status vector for errors handling.
 */
@Getter
public final class FirebirdStatusVector extends FirebirdPacket {
    
    private final int gdsCode;
    
    private final String errorMessage;
    
    public FirebirdStatusVector(final SQLException ex) {
        gdsCode = ex.getErrorCode() >= ISCConstants.isc_arith_except ? ex.getErrorCode() : ISCConstants.isc_random;
        String rawMessage = ex.getMessage();
        int idx = rawMessage.indexOf(';');
        String message = idx >= 0 ? rawMessage.substring(idx + 1).trim() : rawMessage;
        int stateIdx = message.indexOf(" [SQLState:");
        if (stateIdx >= 0) {
            message = message.substring(0, stateIdx).trim();
        }
        errorMessage = message;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(ISCConstants.isc_arg_gds);
        payload.writeInt4(gdsCode);
        payload.writeInt4(ISCConstants.isc_arg_string);
        payload.writeString(errorMessage);
        payload.writeInt4(ISCConstants.isc_arg_end);
    }
}
