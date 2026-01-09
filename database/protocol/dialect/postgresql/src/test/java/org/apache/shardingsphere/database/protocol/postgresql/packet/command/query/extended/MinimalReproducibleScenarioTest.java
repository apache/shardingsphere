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

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MinimalReproducibleScenarioTest {

    @Test
    void testJsonBindScenario() {
        // Minimal reproducible scenario for JSON type handling
        String jsonData = "{\"name\": \"John\", \"age\": 30}";
        PGobject parsedJson = new PostgreSQLJsonValueParser().parse(jsonData);
        
        assertThat(parsedJson.getType(), is("json"));
        assertThat(parsedJson.getValue(), is(jsonData));
        
        // Test column type detection for JSON
        PostgreSQLColumnType detectedType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "json");
        assertThat(detectedType, is(PostgreSQLColumnType.JSON));
    }

    @Test
    void testJsonBBindScenario() {
        // Minimal reproducible scenario for JSONB type handling
        String jsonbData = "{\"product\": \"laptop\", \"price\": 1200}";
        PGobject parsedJsonB = new PostgreSQLJsonBValueParser().parse(jsonbData);
        
        assertThat(parsedJsonB.getType(), is("jsonb"));
        assertThat(parsedJsonB.getValue(), is(jsonbData));
        
        // Test column type detection for JSONB
        PostgreSQLColumnType detectedType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "jsonb");
        assertThat(detectedType, is(PostgreSQLColumnType.JSONB));
    }
    
    @Test
    void testUDTBindScenario() {
        // Test UDT handling scenario
        PostgreSQLColumnType uuidType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "uuid");
        assertThat(uuidType, is(PostgreSQLColumnType.UUID));
        
        PostgreSQLColumnType customType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "custom_udt");
        // This will be mapped to JSON as per default behavior, showing the issue
        assertThat(customType, is(PostgreSQLColumnType.JSON));
    }
    
    @Test
    void testNonUdtOtherScenario() {
        // Test non-UDT Types.OTHER handling
        PostgreSQLColumnType varbitType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "varbit");
        assertThat(varbitType, is(PostgreSQLColumnType.VARBIT));
        
        PostgreSQLColumnType bitVaryingType = PostgreSQLColumnType.valueOfJDBCType(Types.OTHER, "bit varying");
        assertThat(bitVaryingType, is(PostgreSQLColumnType.VARBIT));
    }
}