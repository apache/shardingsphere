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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command.generic;

import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.opengauss.util.ServerErrorMessage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Error response packet for openGauss.
 */
public final class OpenGaussErrorResponsePacket extends PostgreSQLIdentifierPacket {
    
    public static final char FIELD_TYPE_SEVERITY = 'S';
    
    public static final char FIELD_TYPE_CODE = 'C';
    
    public static final char FIELD_TYPE_MESSAGE = 'M';
    
    public static final char FIELD_TYPE_DETAIL = 'D';
    
    public static final char FIELD_TYPE_HINT = 'H';
    
    public static final char FIELD_TYPE_POSITION = 'P';
    
    public static final char FIELD_TYPE_INTERNAL_POSITION = 'p';
    
    public static final char FIELD_TYPE_INTERNAL_QUERY = 'q';
    
    public static final char FIELD_TYPE_WHERE = 'W';
    
    public static final char FIELD_TYPE_FILE = 'F';
    
    public static final char FIELD_TYPE_LINE = 'L';
    
    public static final char FIELD_TYPE_ROUTINE = 'R';
    
    public static final char FIELD_TYPE_ERROR_CODE = 'c';
    
    private final Map<Character, String> fields;
    
    public OpenGaussErrorResponsePacket(final ServerErrorMessage serverErrorMessage) {
        fields = new LinkedHashMap<>(13, 1F);
        fillFieldsByServerErrorMessage(serverErrorMessage);
        fillRequiredFieldsIfNecessary();
    }
    
    public OpenGaussErrorResponsePacket(final String severityLevel, final String sqlState, final String message) {
        fields = new LinkedHashMap<>(4, 1F);
        fields.put(FIELD_TYPE_SEVERITY, severityLevel);
        fields.put(FIELD_TYPE_CODE, sqlState);
        fields.put(FIELD_TYPE_MESSAGE, message);
        fillRequiredFieldsIfNecessary();
    }
    
    private void fillFieldsByServerErrorMessage(final ServerErrorMessage serverErrorMessage) {
        if (null != serverErrorMessage.getSeverity()) {
            fields.put(FIELD_TYPE_SEVERITY, serverErrorMessage.getSeverity());
        }
        if (null != serverErrorMessage.getSQLState()) {
            fields.put(FIELD_TYPE_CODE, serverErrorMessage.getSQLState());
        }
        if (null != serverErrorMessage.getMessage()) {
            fields.put(FIELD_TYPE_MESSAGE, serverErrorMessage.getMessage());
        }
        if (null != serverErrorMessage.getERRORCODE()) {
            fields.put(FIELD_TYPE_ERROR_CODE, serverErrorMessage.getERRORCODE());
        }
        if (null != serverErrorMessage.getDetail()) {
            fields.put(FIELD_TYPE_DETAIL, serverErrorMessage.getDetail());
        }
        if (null != serverErrorMessage.getHint()) {
            fields.put(FIELD_TYPE_HINT, serverErrorMessage.getHint());
        }
        if (serverErrorMessage.getPosition() > 0) {
            fields.put(FIELD_TYPE_POSITION, String.valueOf(serverErrorMessage.getPosition()));
        }
        if (serverErrorMessage.getInternalPosition() > 0) {
            fields.put(FIELD_TYPE_INTERNAL_POSITION, String.valueOf(serverErrorMessage.getInternalPosition()));
        }
        if (null != serverErrorMessage.getInternalQuery()) {
            fields.put(FIELD_TYPE_INTERNAL_QUERY, serverErrorMessage.getInternalQuery());
        }
        if (null != serverErrorMessage.getWhere()) {
            fields.put(FIELD_TYPE_WHERE, serverErrorMessage.getWhere());
        }
        if (null != serverErrorMessage.getFile()) {
            fields.put(FIELD_TYPE_FILE, serverErrorMessage.getFile());
        }
        if (serverErrorMessage.getLine() > 0) {
            fields.put(FIELD_TYPE_LINE, String.valueOf(serverErrorMessage.getLine()));
        }
        if (null != serverErrorMessage.getRoutine()) {
            fields.put(FIELD_TYPE_ROUTINE, serverErrorMessage.getRoutine());
        }
    }
    
    private void fillRequiredFieldsIfNecessary() {
        fields.putIfAbsent(FIELD_TYPE_ERROR_CODE, "0");
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        for (Entry<Character, String> entry : fields.entrySet()) {
            payload.writeInt1(entry.getKey());
            payload.writeStringNul(entry.getValue());
        }
        payload.writeInt1(0);
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.ERROR_RESPONSE;
    }
}
