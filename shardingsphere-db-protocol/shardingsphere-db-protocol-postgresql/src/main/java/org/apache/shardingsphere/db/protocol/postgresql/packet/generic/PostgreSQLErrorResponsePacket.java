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

package org.apache.shardingsphere.db.protocol.postgresql.packet.generic;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Error response packet for PostgreSQL.
 * 
 * @see <a href="https://www.postgresql.org/docs/12/protocol-message-formats.html">ErrorResponse (B)</a>
 */
public final class PostgreSQLErrorResponsePacket implements PostgreSQLPacket {

    public static final char FIELD_TYPE_SEVERITY = 'S';

    public static final char FIELD_TYPE_SEVERITY2 = 'V';

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

    @Getter
    private final char messageType = PostgreSQLCommandPacketType.ERROR_RESPONSE.getValue();

    private final Map<Character, String> fields = new HashMap<>();

    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        for (Entry<Character, String> each : fields.entrySet()) {
            payload.writeInt1(each.getKey());
            payload.writeStringNul(each.getValue());
        }
        payload.writeInt1(0);
    }

    /**
     * Add field.
     *
     * @param fieldType field type
     * @param fieldValue field value
     */
    public void addField(final char fieldType, final String fieldValue) {
        fields.put(fieldType, fieldValue);
    }
}
