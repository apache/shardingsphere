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

package org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseMetaDataDialectHandlerTest {
    
    private static final String USER_NAME = "root";
    
    private static final String TABLE_NAME_PATTERN = "t_order_0";
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Before
    public void setUp() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getUserName()).thenReturn(USER_NAME);
        when(connection.getSchema()).thenReturn(USER_NAME);
    }
    
    @Test
    public void assertGetMySQLSchema() {
        assertThat(getSchema(new MySQLDatabaseType()), is(USER_NAME));
    }
    
    @Test
    public void assertGetMariaDBSchema() {
        assertThat(getSchema(new MariaDBDatabaseType()), is(USER_NAME));
    }
    
    @Test
    public void assertGetH2Schema() {
        assertThat(getSchema(new H2DatabaseType()), is(USER_NAME));
    }
    
    @Test
    public void assertGetPostgreSQLSchema() {
        assertThat(getSchema(new PostgreSQLDatabaseType()), is(USER_NAME));
    }
    
    @Test
    public void assertSQLServerSchemaSchema() {
        assertThat(getSchema(new SQLServerDatabaseType()), is(USER_NAME));
    }
    
    @Test
    public void assertGetOracleSchema() {
        assertThat(getSchema(new OracleDatabaseType()), is(USER_NAME.toUpperCase()));
    }
    
    @Test
    public void assertGetSQL92Schema() {
        assertThat(getSchema(new SQL92DatabaseType()), is(USER_NAME));
    }
    
    private String getSchema(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(handler -> handler.getSchema(connection)).orElse(getSchema(connection));
    }
    
    private String getSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException ex) {
            return null;
        }
    }
    
    @Test
    public void assertGetTableNamePatternForMySQL() {
        assertThat(getTableNamePattern(new MySQLDatabaseType()), is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetTableNamePatternForMariaDB() {
        assertThat(getTableNamePattern(new MariaDBDatabaseType()), is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetTableNamePatternForH2() {
        assertThat(getTableNamePattern(new H2DatabaseType()), is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetTableNamePatternForPostgreSQL() {
        assertThat(getTableNamePattern(new PostgreSQLDatabaseType()), is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetTableNamePatternForSQLServer() {
        assertThat(getTableNamePattern(new SQLServerDatabaseType()), is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetTableNamePatternForOracle() {
        assertThat(getTableNamePattern(new OracleDatabaseType()), is(TABLE_NAME_PATTERN.toUpperCase()));
    }
    
    @Test
    public void assertGetTableNamePatternForSQL92() {
        assertThat(getTableNamePattern(new SQL92DatabaseType()), is(TABLE_NAME_PATTERN));
    }
    
    private String getTableNamePattern(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(handler -> handler.formatTableNamePattern(TABLE_NAME_PATTERN)).orElse(TABLE_NAME_PATTERN);
    }
    
    @Test
    public void assertGetQuoteCharacterForMySQL() {
        QuoteCharacter actual = findQuoteCharacter(new MySQLDatabaseType());
        assertThat(actual.getStartDelimiter(), is("`"));
        assertThat(actual.getEndDelimiter(), is("`"));
    }
    
    @Test
    public void assertGetQuoteCharacterForMariaDB() {
        QuoteCharacter actual = findQuoteCharacter(new MariaDBDatabaseType());
        assertThat(actual.getStartDelimiter(), is("`"));
        assertThat(actual.getEndDelimiter(), is("`"));
    }
    
    @Test
    public void assertGetQuoteCharacterForH2() {
        QuoteCharacter actual = findQuoteCharacter(new H2DatabaseType());
        assertThat(actual.getStartDelimiter(), is("\""));
        assertThat(actual.getEndDelimiter(), is("\""));
    }
    
    @Test
    public void assertGetQuoteCharacterForPostgreSQL() {
        QuoteCharacter actual = findQuoteCharacter(new PostgreSQLDatabaseType());
        assertThat(actual.getStartDelimiter(), is("\""));
        assertThat(actual.getEndDelimiter(), is("\""));
    }

    @Test
    public void assertGetQuoteCharacterForSQLServer() {
        QuoteCharacter actual = findQuoteCharacter(new SQLServerDatabaseType());
        assertThat(actual.getStartDelimiter(), is("["));
        assertThat(actual.getEndDelimiter(), is("]"));
    }
    
    @Test
    public void assertGetQuoteCharacterOracle() {
        QuoteCharacter actual = findQuoteCharacter(new OracleDatabaseType());
        assertThat(actual.getStartDelimiter(), is("\""));
        assertThat(actual.getEndDelimiter(), is("\""));
    }
    
    @Test
    public void assertGetQuoteCharacterForSQL92() {
        QuoteCharacter actual = findQuoteCharacter(new SQL92DatabaseType());
        assertThat(actual.getStartDelimiter(), is("\""));
        assertThat(actual.getEndDelimiter(), is("\""));
    }
    
    private QuoteCharacter findQuoteCharacter(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(DatabaseMetaDataDialectHandler::getQuoteCharacter).orElse(QuoteCharacter.NONE);
    }
}
