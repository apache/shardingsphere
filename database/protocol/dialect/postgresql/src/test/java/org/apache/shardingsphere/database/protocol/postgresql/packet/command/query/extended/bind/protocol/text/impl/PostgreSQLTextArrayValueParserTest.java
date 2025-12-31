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

import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

class PostgreSQLTextArrayValueParserTest {
    
    @Test
    void assertParse() {
        PGobject actual = new PostgreSQLTextArrayValueParser().parse("{c,d}");
        assertThat(actual.getType(), is("text[]"));
        assertThat(actual.getValue(), is("{c,d}"));
    }
    
    @Test
    void assertParseWithThrowsSQLException() {
        try (MockedConstruction<PGobject> mocked = mockConstruction(PGobject.class, (mock, context) -> doThrow(new SQLException("failed")).when(mock).setValue(anyString()))) {
            SQLWrapperException ex = assertThrows(SQLWrapperException.class, () -> new PostgreSQLTextArrayValueParser().parse("bad"));
            assertThat(ex.getCause(), isA(SQLException.class));
            assertThat(mocked.constructed().size(), is(1));
        }
    }
}
