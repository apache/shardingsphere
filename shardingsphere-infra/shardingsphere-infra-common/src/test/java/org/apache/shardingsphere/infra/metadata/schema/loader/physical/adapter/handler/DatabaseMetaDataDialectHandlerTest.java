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

package org.apache.shardingsphere.infra.metadata.schema.loader.physical.adapter.handler;

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
    
    @Test
    public void assertGetSchema() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getUserName()).thenReturn(USER_NAME);

        String oracleSchema = getSchema(new OracleDatabaseType());
        assertThat(oracleSchema, is(USER_NAME.toUpperCase()));

        when(connection.getSchema()).thenReturn(USER_NAME);
        String mysqlSchema = getSchema(new MySQLDatabaseType());
        assertThat(mysqlSchema, is(USER_NAME));

        String h2Schema = getSchema(new H2DatabaseType());
        assertThat(h2Schema, is(USER_NAME));

        String mariaDBSchema = getSchema(new MariaDBDatabaseType());
        assertThat(mariaDBSchema, is(USER_NAME));

        String postgreSQLSchema = getSchema(new PostgreSQLDatabaseType());
        assertThat(postgreSQLSchema, is(USER_NAME));

        String sqlServerSchema = getSchema(new SQLServerDatabaseType());
        assertThat(sqlServerSchema, is(USER_NAME));

        String sql92Schema = getSchema(new SQL92DatabaseType());
        assertThat(sql92Schema, is(USER_NAME));
    }
    
    @Test
    public void assertGetTableNamePattern() {
        String oracleTableNamePattern = getTableNamePattern(new OracleDatabaseType());
        assertThat(oracleTableNamePattern, is(TABLE_NAME_PATTERN.toUpperCase()));
        String mysqlTableNamePattern = getTableNamePattern(new MySQLDatabaseType());
        assertThat(mysqlTableNamePattern, is(TABLE_NAME_PATTERN));
        String h2TableNamePattern = getTableNamePattern(new H2DatabaseType());
        assertThat(h2TableNamePattern, is(TABLE_NAME_PATTERN));
        String mariaDBTableNamePattern = getTableNamePattern(new MariaDBDatabaseType());
        assertThat(mariaDBTableNamePattern, is(TABLE_NAME_PATTERN));
        String postgreSQLTableNamePattern = getTableNamePattern(new PostgreSQLDatabaseType());
        assertThat(postgreSQLTableNamePattern, is(TABLE_NAME_PATTERN));
        String sqlServerTableNamePattern = getTableNamePattern(new SQLServerDatabaseType());
        assertThat(sqlServerTableNamePattern, is(TABLE_NAME_PATTERN));
        String sql92TableNamePattern = getTableNamePattern(new SQL92DatabaseType());
        assertThat(sql92TableNamePattern, is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetQuoteCharacter() {
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
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(DatabaseMetaDataDialectHandler::getQuoteCharacter).orElse(QuoteCharacter.NONE);
    }
    
    private String getTableNamePattern(final DatabaseType databaseType) {
        return DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(handler -> handler.formatTableNamePattern(TABLE_NAME_PATTERN)).orElse(TABLE_NAME_PATTERN);
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
