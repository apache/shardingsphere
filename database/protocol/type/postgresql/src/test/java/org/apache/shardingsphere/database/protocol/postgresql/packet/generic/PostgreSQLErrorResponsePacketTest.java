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

import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostgreSQLErrorResponsePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertToServerErrorMessage() {
        PostgreSQLErrorResponsePacket responsePacket = createErrorResponsePacket();
        String expectedMessage = "SFATAL\0VFATAL\0C3D000\0Mdatabase \"test\" does not exist\0Ddetail\0Hhint\0P1\0p2\0qinternal query\0"
                + "Wwhere\0stest\0ttable\0ccolumn\0ddata type\0nconstraint\0Ffile\0L3\0Rroutine";
        assertThat(responsePacket.toServerErrorMessage(), is(expectedMessage));
    }
    
    @Test
    void assertWrite() {
        PostgreSQLErrorResponsePacket responsePacket = createErrorResponsePacket();
        responsePacket.write(payload);
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY);
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY_NON_LOCALIZED);
        verify(payload, times(2)).writeStringNul("FATAL");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE);
        verify(payload).writeStringNul("3D000");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE);
        verify(payload).writeStringNul("database \"test\" does not exist");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_DETAIL);
        verify(payload).writeStringNul("detail");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_HINT);
        verify(payload).writeStringNul("hint");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_POSITION);
        verify(payload).writeStringNul("1");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_INTERNAL_POSITION);
        verify(payload).writeStringNul("2");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_INTERNAL_QUERY);
        verify(payload).writeStringNul("internal query");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_WHERE);
        verify(payload).writeStringNul("where");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_SCHEMA_NAME);
        verify(payload).writeStringNul("test");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_TABLE_NAME);
        verify(payload).writeStringNul("table");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_COLUMN_NAME);
        verify(payload).writeStringNul("column");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_DATA_TYPE_NAME);
        verify(payload).writeStringNul("data type");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_CONSTRAINT_NAME);
        verify(payload).writeStringNul("constraint");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_FILE);
        verify(payload).writeStringNul("file");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_LINE);
        verify(payload).writeStringNul("3");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_ROUTINE);
        verify(payload).writeStringNul("routine");
        verify(payload).writeInt1(0);
    }
    
    private PostgreSQLErrorResponsePacket createErrorResponsePacket() {
        return PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.FATAL, PostgreSQLVendorError.INVALID_CATALOG_NAME, "database \"test\" does not exist").detail("detail")
                .hint("hint").position(1).internalQueryAndInternalPosition("internal query", 2).where("where").schemaName("test").tableName("table").columnName("column").dataTypeName("data type")
                .constraintName("constraint").file("file").line(3).routine("routine").build();
    }
}
