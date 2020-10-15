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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    
    @Test
    public void assertGetSchema() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getUserName()).thenReturn(USER_NAME);
        String schema = getSchema(new OracleDatabaseType());
        assertThat(schema, is(USER_NAME.toUpperCase()));
        when(connection.getSchema()).thenReturn(USER_NAME);
        String schemaMySQL = getSchema(new MySQLDatabaseType());
        assertThat(schemaMySQL, is(USER_NAME));
        // TODO H2, MariaDB, PostgreSQL, SQLServer, SQL92 database getSchema
    }
    
    @Test
    public void assertGetTableNamePattern() {
        String oracleTableNamePattern = getTableNamePattern(new OracleDatabaseType());
        assertThat(oracleTableNamePattern, is(TABLE_NAME_PATTERN.toUpperCase()));
        String mysqlTableNamePattern = getTableNamePattern(new MySQLDatabaseType());
        assertThat(mysqlTableNamePattern, is(TABLE_NAME_PATTERN));
        // TODO H2, MariaDB, PostgreSQL, SQLServer, SQL92 decorate table name pattern
    }
    
    @Test
    public void assertGetDelimiter() {
        QuoteCharacter oracleQuoteCharacter = findQuoteCharacter(new OracleDatabaseType());
        assertThat(oracleQuoteCharacter.getStartDelimiter(), is("\""));
        assertThat(oracleQuoteCharacter.getEndDelimiter(), is("\""));
    
        QuoteCharacter h2QuoteCharacter = findQuoteCharacter(new H2DatabaseType());
        assertThat(h2QuoteCharacter.getStartDelimiter(), is("\""));
        assertThat(h2QuoteCharacter.getEndDelimiter(), is("\""));
    
        QuoteCharacter postgreSQLQuoteCharacter = findQuoteCharacter(new PostgreSQLDatabaseType());
        assertThat(postgreSQLQuoteCharacter.getStartDelimiter(), is("\""));
        assertThat(postgreSQLQuoteCharacter.getEndDelimiter(), is("\""));
    
        QuoteCharacter sql92QuoteCharacter = findQuoteCharacter(new SQL92DatabaseType());
        assertThat(sql92QuoteCharacter.getStartDelimiter(), is("\""));
        assertThat(sql92QuoteCharacter.getEndDelimiter(), is("\""));
    
        QuoteCharacter mariaDBQuoteCharacter = findQuoteCharacter(new MariaDBDatabaseType());
        assertThat(mariaDBQuoteCharacter.getStartDelimiter(), is("`"));
        assertThat(mariaDBQuoteCharacter.getEndDelimiter(), is("`"));
        
        QuoteCharacter mysqlQuoteCharacter = findQuoteCharacter(new MySQLDatabaseType());
        assertThat(mysqlQuoteCharacter.getStartDelimiter(), is("`"));
        assertThat(mysqlQuoteCharacter.getEndDelimiter(), is("`"));
    
        QuoteCharacter sqlServerQuoteCharacter = findQuoteCharacter(new SQLServerDatabaseType());
        assertThat(sqlServerQuoteCharacter.getStartDelimiter(), is("["));
        assertThat(sqlServerQuoteCharacter.getEndDelimiter(), is("]"));
    }
    
    private QuoteCharacter findQuoteCharacter(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(DatabaseMetaDataDialectHandler::getDelimiter).orElse(QuoteCharacter.NONE);
    }
    
    private String getTableNamePattern(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(handler -> handler.decorate(TABLE_NAME_PATTERN)).orElse(TABLE_NAME_PATTERN);
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
}
