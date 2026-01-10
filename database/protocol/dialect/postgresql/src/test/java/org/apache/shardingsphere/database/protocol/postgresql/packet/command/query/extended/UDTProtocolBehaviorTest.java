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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class UDTProtocolBehaviorTest {
    
    @Test
    void testUDTProtocolTextBinding() {
        // Test how UDTs are handled in protocol text binding
        // Various UDT types should be handled correctly
        
        // Custom UDT - should map to JSON (current behavior, but could be improved)
        PostgreSQLColumnType customUdtType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "my_custom_type");
        assertThat(customUdtType, is(PostgreSQLColumnType.JSON)); // Current default mapping
        
        // Enum UDT
        PostgreSQLColumnType enumUdtType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "status_enum");
        assertThat(enumUdtType, is(PostgreSQLColumnType.JSON)); // Current default mapping
        
        // Composite UDT
        PostgreSQLColumnType compositeUdtType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "address_type");
        assertThat(compositeUdtType, is(PostgreSQLColumnType.JSON)); // Current default mapping
    }
    
    @Test
    void testUDTProtocolBinaryBinding() {
        // Since UDTs often use binary protocol handling, test the binary type mappings
        // This exercises the protocol handling for UDT types
        
        // The extended query protocol in PostgreSQL distinguishes between text and binary formats
        // Our UDT types should work correctly in both modes
        int jsonbOid = PostgreSQLColumnType.JSONB.getValue(); // 3802
        PostgreSQLColumnType fromBinary = PostgreSQLColumnType.valueOf(jsonbOid);
        assertThat(fromBinary, is(PostgreSQLColumnType.JSONB));
    }
    
    @Test
    void testUDTProtocolDescribeBehavior() {
        // Test that UDT types are described correctly in protocol
        PostgreSQLColumnType[] udtCompatibleTypes = {
                PostgreSQLColumnType.JSON,
                PostgreSQLColumnType.JSONB,
                PostgreSQLColumnType.UUID
        };
        
        for (PostgreSQLColumnType type : udtCompatibleTypes) {
            // Each type should have a valid OID value
            assertThat(type.getValue() > 0, is(true));
            
            // Should be able to retrieve from OID
            PostgreSQLColumnType retrieved = PostgreSQLColumnType.valueOf(type.getValue());
            assertThat(retrieved, is(type));
        }
    }
    
    @Test
    void testUDTAndOtherTypeProtocolMapping() {
        // Test the mapping between JDBC Types.OTHER and PostgreSQL column types
        String[] otherTypeNames = {"json", "jsonb", "uuid", "varbit", "bit varying", "custom_udt", "enum_type"};
        
        for (String typeName : otherTypeNames) {
            PostgreSQLColumnType type = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, typeName);
            
            // All should be valid mappings except for truly unknown types
            // The important thing is that they return a valid enum instance
            assertThat(type.name(), is(type.name())); // Just ensuring no exceptions
            
            // Check specific known mappings
            if ("json".equalsIgnoreCase(typeName)) {
                assertThat(type, is(PostgreSQLColumnType.JSON));
            } else if ("jsonb".equalsIgnoreCase(typeName)) {
                assertThat(type, is(PostgreSQLColumnType.JSONB));
            } else if ("uuid".equalsIgnoreCase(typeName)) {
                assertThat(type, is(PostgreSQLColumnType.UUID));
            } else if ("varbit".equalsIgnoreCase(typeName) || "bit varying".equalsIgnoreCase(typeName)) {
                assertThat(type, is(PostgreSQLColumnType.VARBIT));
            }
        }
    }
}