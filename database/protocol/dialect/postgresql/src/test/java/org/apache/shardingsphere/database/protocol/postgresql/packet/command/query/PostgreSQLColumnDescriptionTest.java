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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLColumnDescriptionTest {
    
    @Test
    void assertIntegerTypeOid() {
        PostgreSQLColumnDescription description = new PostgreSQLColumnDescription("age", 1, Types.INTEGER, 4, null);
        assertThat(description.getTypeOID(), is(23));
    }
    
    @Test
    void assertStringTypeOid() {
        PostgreSQLColumnDescription description = new PostgreSQLColumnDescription("name", 1, Types.VARCHAR, 4, null);
        assertThat(description.getTypeOID(), is(1043));
    }
    
    @Test
    void assertBitTypeOid() {
        PostgreSQLColumnDescription description = new PostgreSQLColumnDescription("gender", 1, Types.BIT, 1, "bit");
        assertThat(description.getTypeOID(), is(1560));
    }
    
    @Test
    void assertBoolTypeOid() {
        PostgreSQLColumnDescription description = new PostgreSQLColumnDescription("married", 1, Types.BIT, 1, "bool");
        assertThat(description.getTypeOID(), is(16));
    }
    
    @Test
    void assertIntegerArrayTypeOid() {
        PostgreSQLColumnDescription description = new PostgreSQLColumnDescription("ages", 2, Types.ARRAY, 12, "_int4");
        assertThat(description.getTypeOID(), is(1007));
    }
}
