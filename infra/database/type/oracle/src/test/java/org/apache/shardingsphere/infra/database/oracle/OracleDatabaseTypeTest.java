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

package org.apache.shardingsphere.infra.database.oracle;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OracleDatabaseTypeTest {
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "Oracle").getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetJdbcUrlPrefixes() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "Oracle").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:oracle:")));
    }
    
    @Test
    void assertOracleDataSourceMetaData() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "Oracle").getDataSourceMetaData("jdbc:oracle:oci:@127.0.0.1/foo_ds", "scott"), instanceOf(OracleDataSourceMetaData.class));
    }
    
    @Test
    void assertGetSchema() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getUserName()).thenReturn("scott");
        assertThat(TypedSPILoader.getService(DatabaseType.class, "Oracle").getSchema(connection), is("SCOTT"));
    }
    
    @Test
    void assertGetSchemaIfExceptionThrown() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getUserName()).thenThrow(SQLException.class);
        assertNull(TypedSPILoader.getService(DatabaseType.class, "Oracle").getSchema(connection));
    }
    
    @Test
    void assertFormatTableNamePattern() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "Oracle").formatTableNamePattern("tbl"), is("TBL"));
    }
    
    @Test
    void assertGetSystemDatabases() {
        assertTrue(TypedSPILoader.getService(DatabaseType.class, "Oracle").getSystemDatabaseSchemaMap().isEmpty());
    }
    
    @Test
    void assertGetSystemSchemas() {
        assertTrue(TypedSPILoader.getService(DatabaseType.class, "Oracle").getSystemSchemas().isEmpty());
    }
}
