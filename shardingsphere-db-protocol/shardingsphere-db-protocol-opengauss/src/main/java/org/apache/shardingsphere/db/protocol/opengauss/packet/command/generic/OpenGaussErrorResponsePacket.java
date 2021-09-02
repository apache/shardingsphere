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

package org.apache.shardingsphere.db.protocol.opengauss.packet.command.generic;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Error response packet for openGauss.
 */
public final class OpenGaussErrorResponsePacket implements PostgreSQLIdentifierPacket {
    
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
    
    public static final char FIELD_TYPE_ERRORCODE = 'c';
    
    public static final char FIELD_TYPE_SOCKET_ADDRESS = 'a';
    
    private final Map<Character, String> fields = new LinkedHashMap<>(16, 1);
    
    private OpenGaussErrorResponsePacket(final Map<Character, String> fields) {
        this.fields.putAll(fields);
    }
    
    /**
     * To server error message.
     *
     * @return server error message
     */
    public String toServerErrorMessage() {
        return fields.entrySet().stream().map(entry -> entry.getKey() + entry.getValue()).collect(Collectors.joining("\0"));
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        for (Entry<Character, String> each : fields.entrySet()) {
            payload.writeInt1(each.getKey());
            payload.writeStringNul(each.getValue());
        }
        payload.writeInt1(0);
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.ERROR_RESPONSE;
    }
    
    /**
     * Create openGauss error response packet builder with required arguments.
     *
     * @param severity severity
     * @param postgreSQLErrorCode PostgreSQL error code
     * @param message message
     * @return openGauss error response packet builder
     */
    public static Builder newBuilder(final PostgreSQLMessageSeverityLevel severity, final PostgreSQLErrorCode postgreSQLErrorCode, final String message) {
        return newBuilder(severity, postgreSQLErrorCode.getErrorCode(), message);
    }
    
    /**
     * Create openGauss error response packet builder with required arguments.
     *
     * @param severity severity
     * @param code code
     * @param message message
     * @return openGauss error response packet builder
     */
    public static Builder newBuilder(final PostgreSQLMessageSeverityLevel severity, final String code, final String message) {
        return new Builder(severity, code, message);
    }
    
    public static final class Builder {
        
        private final Map<Character, String> fields = new LinkedHashMap<>(16, 1);
        
        private Builder(final PostgreSQLMessageSeverityLevel severity, final String code, final String message) {
            Preconditions.checkArgument(null != severity, "The severity is always present!");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(code), "The SQLSTATE code is always present!");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "The message is always present!");
            fields.put(FIELD_TYPE_SEVERITY, severity.name());
            fields.put(FIELD_TYPE_CODE, code);
            fields.put(FIELD_TYPE_MESSAGE, message);
            fields.put(FIELD_TYPE_ERRORCODE, "0");
        }
    
        /**
         * Set openGauss ERRORCODE.
         *
         * @param errorcode openGauss ERRORCODE
         * @return openGauss error response packet builder
         */
        public Builder errorcode(final String errorcode) {
            if (!Strings.isNullOrEmpty(errorcode)) {
                fields.put(FIELD_TYPE_ERRORCODE, errorcode);
            }
            return this;
        }
        
        /**
         * Set detail.
         *
         * @param detail detail
         * @return openGauss error response packet builder
         */
        public Builder detail(final String detail) {
            if (!Strings.isNullOrEmpty(detail)) {
                fields.put(FIELD_TYPE_DETAIL, detail);
            }
            return this;
        }
        
        /**
         * Set hint.
         *
         * @param hint hint
         * @return openGauss error response packet builder
         */
        public Builder hint(final String hint) {
            if (!Strings.isNullOrEmpty(hint)) {
                fields.put(FIELD_TYPE_HINT, hint);
            }
            return this;
        }
        
        /**
         * Set position. The first character has index 1, and positions are measured in characters not bytes.
         *
         * @param position position
         * @return openGauss error response packet builder
         */
        public Builder position(final int position) {
            if (position > 0) {
                fields.put(FIELD_TYPE_POSITION, Integer.toString(position));
            }
            return this;
        }
        
        /**
         * Set internal query and internal position. The first character has index 1, and positions are measured in characters not bytes.
         *
         * @param internalQuery internal query
         * @param internalPosition internal position
         * @return openGauss error response packet builder
         */
        public Builder internalQueryAndInternalPosition(final String internalQuery, final int internalPosition) {
            if (internalPosition > 0) {
                fields.put(FIELD_TYPE_INTERNAL_POSITION, Integer.toString(internalPosition));
            }
            return internalQuery(internalQuery);
        }
        
        /**
         * Set internal query.
         *
         * @param internalQuery internal query
         * @return openGauss error response packet builder
         */
        public Builder internalQuery(final String internalQuery) {
            if (!Strings.isNullOrEmpty(internalQuery)) {
                fields.put(FIELD_TYPE_INTERNAL_QUERY, internalQuery);
            }
            return this;
        }
        
        /**
         * Set where.
         *
         * @param where where
         * @return openGauss error response packet builder
         */
        public Builder where(final String where) {
            if (!Strings.isNullOrEmpty(where)) {
                fields.put(FIELD_TYPE_WHERE, where);
            }
            return this;
        }
        
        /**
         * Set file.
         *
         * @param file file
         * @return openGauss error response packet builder
         */
        public Builder file(final String file) {
            if (!Strings.isNullOrEmpty(file)) {
                fields.put(FIELD_TYPE_FILE, file);
            }
            return this;
        }
        
        /**
         * Set line.
         *
         * @param line line
         * @return openGauss error response packet builder
         */
        public Builder line(final int line) {
            if (line > 0) {
                fields.put(FIELD_TYPE_LINE, Integer.toString(line));
            }
            return this;
        }
        
        /**
         * Set routine.
         *
         * @param routine routine
         * @return openGauss error response packet builder
         */
        public Builder routine(final String routine) {
            if (!Strings.isNullOrEmpty(routine)) {
                fields.put(FIELD_TYPE_ROUTINE, routine);
            }
            return this;
        }
    
        /**
         * Set socket address.
         *
         * @param socketAddress socket address
         * @return openGauss error response packet builder
         */
        public Builder socketAddress(final String socketAddress) {
            if (!Strings.isNullOrEmpty(socketAddress)) {
                fields.put(FIELD_TYPE_SOCKET_ADDRESS, socketAddress);
            }
            return this;
        }
        
        /**
         * Build openGauss error response packet builder.
         *
         * @return openGauss error response packet builder
         */
        public OpenGaussErrorResponsePacket build() {
            return new OpenGaussErrorResponsePacket(fields);
        }
    }
}
