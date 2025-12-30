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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultSchemaOptionTest {
    
    @Test
    void assertGetSchema() throws SQLException {
        Connection successConnection = mock(Connection.class);
        when(successConnection.getSchema()).thenReturn("actual_schema");
        assertThat(new DefaultSchemaOption(true, "foo_schema").getSchema(successConnection), is("actual_schema"));
    }
    
    @Test
    void assertGetSchemaWithException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getSchema()).thenThrow(SQLException.class);
        assertNull(new DefaultSchemaOption(false, null).getSchema(connection));
    }
    
    @Test
    void assertGetDefaultSchemaWithSchemaAvailable() {
        Optional<String> defaultSchema = new DefaultSchemaOption(true, "foo_schema").getDefaultSchema();
        assertTrue(defaultSchema.isPresent());
        assertThat(defaultSchema.get(), is("foo_schema"));
    }
    
    @Test
    void assertGetDefaultSchemaWithSchemaUnavailable() {
        assertFalse(new DefaultSchemaOption(false, null).getDefaultSchema().isPresent());
    }
    
    @Test
    void assertGetDefaultSystemSchema() {
        assertFalse(new DefaultSchemaOption(true, "foo_schema").getDefaultSystemSchema().isPresent());
    }
    
    @Test
    void assertIsSchemaAvailable() {
        assertTrue(new DefaultSchemaOption(true, "foo_schema").isSchemaAvailable());
    }
    
    @Test
    void assertIsSchemaUnavailable() {
        assertFalse(new DefaultSchemaOption(false, "foo_schema").isSchemaAvailable());
    }
}
