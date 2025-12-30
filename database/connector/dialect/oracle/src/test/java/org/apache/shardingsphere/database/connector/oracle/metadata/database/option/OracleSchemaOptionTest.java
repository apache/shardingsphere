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

package org.apache.shardingsphere.database.connector.oracle.metadata.database.option;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OracleSchemaOptionTest {
    
    @Test
    void assertIsSchemaAvailable() {
        assertFalse(new OracleSchemaOption().isSchemaAvailable());
    }
    
    @Test
    void assertGetSchemaWithUserName() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getUserName()).thenReturn("scott");
        assertThat(new OracleSchemaOption().getSchema(connection), is("SCOTT"));
    }
    
    @Test
    void assertGetSchemaWhenSQLExceptionThrown() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getMetaData()).thenThrow(SQLException.class);
        assertNull(new OracleSchemaOption().getSchema(connection));
    }
    
    @Test
    void assertGetDefaultSchema() {
        assertFalse(new OracleSchemaOption().getDefaultSchema().isPresent());
    }
    
    @Test
    void assertGetDefaultSystemSchema() {
        assertFalse(new OracleSchemaOption().getDefaultSystemSchema().isPresent());
    }
}
