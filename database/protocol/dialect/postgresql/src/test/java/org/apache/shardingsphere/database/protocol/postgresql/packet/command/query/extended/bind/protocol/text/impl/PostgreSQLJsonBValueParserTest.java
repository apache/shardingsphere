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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLJsonBValueParserTest {

    @Test
    void assertParse() {
        PGobject actual = new PostgreSQLJsonBValueParser().parse("{\"key\":\"value\"}");
        assertThat(actual.getType(), is("jsonb"));
        assertThat(actual.getValue(), is("{\"key\":\"value\"}"));
    }
    
    @Test
    void assertParseEmptyJson() {
        PGobject actual = new PostgreSQLJsonBValueParser().parse("{}");
        assertThat(actual.getType(), is("jsonb"));
        assertThat(actual.getValue(), is("{}"));
    }
    
    @Test
    void assertParseComplexJson() {
        String complexJson = "{\"field1\": \"value1\", \"field2\": [\"item1\", \"item2\"], \"field3\": {\"nested\": true}}";
        PGobject actual = new PostgreSQLJsonBValueParser().parse(complexJson);
        assertThat(actual.getType(), is("jsonb"));
        assertThat(actual.getValue(), is(complexJson));
    }
}