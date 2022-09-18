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

package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.dialect.PostgreSQLDataSourceMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PostgreSQLDatabaseTypeTest {
    
    @Test
    public void assertGetQuoteCharacter() {
        assertThat(new PostgreSQLDatabaseType().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    public void assertGetJdbcUrlPrefixes() {
        assertThat(new PostgreSQLDatabaseType().getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:postgresql:")));
    }
    
    @Test
    public void assertGetDataSourceMetaData() {
        assertThat(new PostgreSQLDatabaseType().getDataSourceMetaData("jdbc:postgresql://localhost:5432/demo_ds_0", "postgres"), instanceOf(PostgreSQLDataSourceMetaData.class));
    }
    
    @Test
    public void assertHandleRollbackOnlyForNotRollbackOnly() throws SQLException {
        new PostgreSQLDatabaseType().handleRollbackOnly(false, mock(CommitStatement.class));
    }
    
    @Test
    public void assertHandleRollbackOnlyForRollbackOnlyAndCommitStatement() throws SQLException {
        new PostgreSQLDatabaseType().handleRollbackOnly(true, mock(CommitStatement.class));
    }
    
    @Test
    public void assertHandleRollbackOnlyForRollbackOnlyAndRollbackStatement() throws SQLException {
        new PostgreSQLDatabaseType().handleRollbackOnly(true, mock(RollbackStatement.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertHandleRollbackOnlyForRollbackOnlyAndNotTCLStatement() throws SQLException {
        new PostgreSQLDatabaseType().handleRollbackOnly(true, mock(SelectStatement.class));
    }
    
    @Test
    public void assertGetSystemDatabases() {
        assertTrue(new PostgreSQLDatabaseType().getSystemDatabaseSchemaMap().containsKey("postgres"));
    }
    
    @Test
    public void assertGetSystemSchemas() {
        assertThat(new PostgreSQLDatabaseType().getSystemSchemas(), is(new HashSet<>(Arrays.asList("information_schema", "pg_catalog"))));
    }
    
    @Test
    public void assertIsSchemaAvailable() {
        assertTrue(new PostgreSQLDatabaseType().isSchemaAvailable());
    }
    
    @Test
    public void assertGetDefaultSchema() {
        assertThat(new PostgreSQLDatabaseType().getDefaultSchema(), is("public"));
    }
}
