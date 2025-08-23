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

package org.apache.shardingsphere.database.protocol.postgresql.packet.generic;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Error response packet for PostgreSQL.
 * 
 * @see <a href="https://www.postgresql.org/docs/12/protocol-message-formats.html">ErrorResponse (B)</a>
 */
public final class PostgreSQLErrorResponsePacket extends PostgreSQLIdentifierPacket {
    
    public static final char FIELD_TYPE_SEVERITY = 'S';
    
    public static final char FIELD_TYPE_SEVERITY_NON_LOCALIZED = 'V';
    
    public static final char FIELD_TYPE_CODE = 'C';
    
    public static final char FIELD_TYPE_MESSAGE = 'M';
    
    public static final char FIELD_TYPE_DETAIL = 'D';
    
    public static final char FIELD_TYPE_HINT = 'H';
    
    public static final char FIELD_TYPE_POSITION = 'P';
    
    public static final char FIELD_TYPE_INTERNAL_POSITION = 'p';
    
    public static final char FIELD_TYPE_INTERNAL_QUERY = 'q';
    
    public static final char FIELD_TYPE_WHERE = 'W';
    
    public static final char FIELD_TYPE_SCHEMA_NAME = 's';
    
    public static final char FIELD_TYPE_TABLE_NAME = 't';
    
    public static final char FIELD_TYPE_COLUMN_NAME = 'c';
    
    public static final char FIELD_TYPE_DATA_TYPE_NAME = 'd';
    
    public static final char FIELD_TYPE_CONSTRAINT_NAME = 'n';
    
    public static final char FIELD_TYPE_FILE = 'F';
    
    public static final char FIELD_TYPE_LINE = 'L';
    
    public static final char FIELD_TYPE_ROUTINE = 'R';
    
    private final Map<Character, String> fields = new LinkedHashMap<>(16, 1F);
    
    private PostgreSQLErrorResponsePacket(final Map<Character, String> fields) {
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
    
    /**
     * Create PostgreSQL error response packet builder with required arguments.
     *
     * @param severity severity
     * @param vendorError PostgreSQL vendor error
     * @param message message
     * @return PostgreSQL error response packet builder
     * @see <a href="https://www.postgresql.org/docs/12/protocol-error-fields.html">52.8. Error and Notice Message Fields</a>
     */
    public static Builder newBuilder(final String severity, final PostgreSQLVendorError vendorError, final String message) {
        return newBuilder(severity, vendorError.getSqlState().getValue(), message);
    }
    
    /**
     * Create PostgreSQL error response packet builder with required arguments.
     *
     * @param severity severity
     * @param sqlState SQL state
     * @param message message
     * @return PostgreSQL error response packet builder
     * @see <a href="https://www.postgresql.org/docs/12/protocol-error-fields.html">52.8. Error and Notice Message Fields</a>
     */
    public static Builder newBuilder(final String severity, final String sqlState, final String message) {
        return new Builder(severity, sqlState, message);
    }
    
    public static final class Builder {
        
        private final Map<Character, String> fields = new LinkedHashMap<>(16, 1F);
        
        private Builder(final String severity, final String sqlState, final String message) {
            Preconditions.checkArgument(null != severity, "The severity is always present!");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(sqlState), "The SQLSTATE code is always present!");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "The message is always present!");
            fields.put(FIELD_TYPE_SEVERITY, severity);
            fields.put(FIELD_TYPE_SEVERITY_NON_LOCALIZED, severity);
            fields.put(FIELD_TYPE_CODE, sqlState);
            fields.put(FIELD_TYPE_MESSAGE, message);
        }
        
        /**
         * Set detail.
         *
         * @param detail detail
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
         */
        public Builder where(final String where) {
            if (!Strings.isNullOrEmpty(where)) {
                fields.put(FIELD_TYPE_WHERE, where);
            }
            return this;
        }
        
        /**
         * Set schema name.
         *
         * @param schemaName schema name
         * @return PostgreSQL error response packet builder
         */
        public Builder schemaName(final String schemaName) {
            if (!Strings.isNullOrEmpty(schemaName)) {
                fields.put(FIELD_TYPE_SCHEMA_NAME, schemaName);
            }
            return this;
        }
        
        /**
         * Set table name.
         *
         * @param tableName table name
         * @return PostgreSQL error response packet builder
         */
        public Builder tableName(final String tableName) {
            if (!Strings.isNullOrEmpty(tableName)) {
                fields.put(FIELD_TYPE_TABLE_NAME, tableName);
            }
            return this;
        }
        
        /**
         * Set column name.
         *
         * @param columnName column name
         * @return PostgreSQL error response packet builder
         */
        public Builder columnName(final String columnName) {
            if (!Strings.isNullOrEmpty(columnName)) {
                fields.put(FIELD_TYPE_COLUMN_NAME, columnName);
            }
            return this;
        }
        
        /**
         * Set data type name.
         *
         * @param dataTypeName data type name
         * @return PostgreSQL error response packet builder
         */
        public Builder dataTypeName(final String dataTypeName) {
            if (!Strings.isNullOrEmpty(dataTypeName)) {
                fields.put(FIELD_TYPE_DATA_TYPE_NAME, dataTypeName);
            }
            return this;
        }
        
        /**
         * Set constraint name.
         *
         * @param constraintName constraint name
         * @return PostgreSQL error response packet builder
         */
        public Builder constraintName(final String constraintName) {
            if (!Strings.isNullOrEmpty(constraintName)) {
                fields.put(FIELD_TYPE_CONSTRAINT_NAME, constraintName);
            }
            return this;
        }
        
        /**
         * Set file.
         *
         * @param file file
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
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
         * @return PostgreSQL error response packet builder
         */
        public Builder routine(final String routine) {
            if (!Strings.isNullOrEmpty(routine)) {
                fields.put(FIELD_TYPE_ROUTINE, routine);
            }
            return this;
        }
        
        /**
         * Build PostgreSQL error response packet builder.
         *
         * @return PostgreSQL error response packet builder
         */
        public PostgreSQLErrorResponsePacket build() {
            return new PostgreSQLErrorResponsePacket(fields);
        }
    }
}
