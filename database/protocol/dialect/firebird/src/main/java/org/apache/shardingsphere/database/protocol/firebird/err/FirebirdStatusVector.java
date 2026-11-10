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

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.FirebirdSQLException;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Firebird status vector for errors handling.
 */
@Getter
public final class FirebirdStatusVector extends FirebirdPacket {
    
    private static final String STATE_SUFFIX = " [SQLState:";
    
    private final int gdsCode;
    
    private final String errorMessage;
    
    private final List<Segment> segments;
    
    public FirebirdStatusVector(final SQLException ex) {
        errorMessage = stripStateSuffix(Strings.nullToEmpty(ex.getMessage()));
        segments = FirebirdErrorSegmentResolver.resolve(ex.getErrorCode(), errorMessage, ex.getSQLState(), getTypedMessageArguments(ex));
        gdsCode = segments.get(0).gdsCode;
    }
    
    private static String stripStateSuffix(final String message) {
        int index = message.indexOf(STATE_SUFFIX);
        return (index >= 0 ? message.substring(0, index) : message).trim();
    }
    
    private static List<Object> getTypedMessageArguments(final SQLException ex) {
        return ex instanceof FirebirdSQLException ? ((FirebirdSQLException) ex).getMessageArguments() : Collections.emptyList();
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        if (ISCConstants.isc_dsql_sqlda_err == gdsCode) {
            payload.writeInt4(ISCConstants.isc_arg_gds);
            payload.writeInt4(ISCConstants.isc_sqlerr);
            payload.writeInt4(ISCConstants.isc_arg_number);
            payload.writeInt4(-804);
        }
        for (Segment each : segments) {
            payload.writeInt4(ISCConstants.isc_arg_gds);
            payload.writeInt4(each.gdsCode);
            for (Object argument : each.arguments) {
                writeArgument(payload, argument);
            }
            if (null != each.sqlState) {
                payload.writeInt4(ISCConstants.isc_arg_sql_state);
                payload.writeString(each.sqlState);
            }
        }
        payload.writeInt4(ISCConstants.isc_arg_end);
    }
    
    private void writeArgument(final FirebirdPacketPayload payload, final Object argument) {
        if (argument instanceof Integer) {
            payload.writeInt4(ISCConstants.isc_arg_number);
            payload.writeInt4((Integer) argument);
        } else {
            payload.writeInt4(ISCConstants.isc_arg_string);
            payload.writeString(argument.toString());
        }
    }
    
    /**
     * Single segment of the status vector.
     */
    @RequiredArgsConstructor
    @Getter
    public static final class Segment {
        
        private final int gdsCode;
        
        private final List<Object> arguments;
        
        private final String sqlState;
    }
}
