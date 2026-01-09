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

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLJsonBValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLJsonValueParser;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.nio.charset.StandardCharsets;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ProtocolBehaviorTest {

    @Test
    void testJsonProtocolParsing() {
        // Test JSON protocol parsing behavior
        String sampleJson = "{\"message\": \"hello world\", \"count\": 42}";
        
        PostgreSQLJsonValueParser parser = new PostgreSQLJsonValueParser();
        PGobject result = parser.parse(sampleJson);
        
        assertThat(result.getType(), is("json"));
        assertThat(result.getValue(), is(sampleJson));
    }
    
    @Test
    void testJsonBProtocolParsing() {
        // Test JSONB protocol parsing behavior
        String sampleJsonB = "{\"data\": [1, 2, 3], \"active\": true}";
        
        PostgreSQLJsonBValueParser parser = new PostgreSQLJsonBValueParser();
        PGobject result = parser.parse(sampleJsonB);
        
        assertThat(result.getType(), is("jsonb"));
        assertThat(result.getValue(), is(sampleJsonB));
    }
    
    @Test
    void testProtocolTypeDetectionForVariousTypes() {
        // Test protocol type detection for different Types.OTHER variants
        
        // Test JSON
        PostgreSQLColumnType jsonColType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "json");
        assertThat(jsonColType, is(PostgreSQLColumnType.JSON));
        
        // Test JSONB
        PostgreSQLColumnType jsonbColType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "jsonb");
        assertThat(jsonbColType, is(PostgreSQLColumnType.JSONB));
        
        // Test UUID (should be UUID not JSON despite being OTHER)
        PostgreSQLColumnType uuidColType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "uuid");
        assertThat(uuidColType, is(PostgreSQLColumnType.UUID));
        
        // Test VARBIT
        PostgreSQLColumnType varbitColType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "varbit");
        assertThat(varbitColType, is(PostgreSQLColumnType.VARBIT));
        
        // Test BIT VARYING
        PostgreSQLColumnType bitVaryingColType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "bit varying");
        assertThat(bitVaryingColType, is(PostgreSQLColumnType.VARBIT));
        
        // Test generic OTHER (should default to JSON)
        PostgreSQLColumnType genericOtherColType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER);
        assertThat(genericOtherColType, is(PostgreSQLColumnType.JSON));
    }
    
    @Test
    void testProtocolValueParserConsistency() {
        // Test protocol value parser consistency for complex objects
        String complexJson = "{\n" +
                           "  \"users\": [\n" +
                           "    {\"id\": 1, \"name\": \"Alice\"},\n" +
                           "    {\"id\": 2, \"name\": \"Bob\"}\n" +
                           "  ],\n" +
                           "  \"metadata\": {\n" +
                           "    \"version\": \"1.0\",\n" +
                           "    \"timestamp\": \"2023-01-01T00:00:00Z\"\n" +
                           "  }\n" +
                           "}";
                           
        PostgreSQLJsonValueParser jsonParser = new PostgreSQLJsonValueParser();
        PGobject jsonResult = jsonParser.parse(complexJson);
        assertThat(jsonResult.getType(), is("json"));
        assertThat(jsonResult.getValue(), is(complexJson));
        
        PostgreSQLJsonBValueParser jsonbParser = new PostgreSQLJsonBValueParser();
        PGobject jsonbResult = jsonbParser.parse(complexJson);
        assertThat(jsonbResult.getType(), is("jsonb"));
        assertThat(jsonbResult.getValue(), is(complexJson));
    }
    
    @Test
    void testProtocolTextEncoding() {
        // Test how the protocol handles text encoding
        String unicodeJson = "{\"emoji\": \"😀\", \"chinese\": \"中文\", \"special\": \"!@#$%^&*()\"}";
        
        PostgreSQLJsonBValueParser parser = new PostgreSQLJsonBValueParser();
        PGobject result = parser.parse(unicodeJson);
        
        assertThat(result.getType(), is("jsonb"));
        assertThat(result.getValue(), is(unicodeJson));
        assertThat(unicodeJson.getBytes(StandardCharsets.UTF_8).length, is(result.getValue().getBytes(StandardCharsets.UTF_8).length));
    }
}