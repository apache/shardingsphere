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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OracleDatabaseMetaDataDialectFacadeTest {

    private static final String USER_NAME = "root";

    private static final String TABLE_NAME_PATTERN = "t_order_0";

    private final DatabaseType oracleDatabaseType = DatabaseTypes.getTrunkDatabaseType("Oracle");

    private final DatabaseType mysqlDatabaseType = DatabaseTypes.getTrunkDatabaseType("MySQL");

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Test
    public void assertGetSchema() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getUserName()).thenReturn(USER_NAME);
        assertThat(DatabaseMetaDataDialectHandlerFacade.getSchema(connection, oracleDatabaseType), is(USER_NAME.toUpperCase()));
    }
    
    @Test
    public void assertGetTableNamePattern() {
        assertThat(DatabaseMetaDataDialectHandlerFacade.getTableNamePattern(TABLE_NAME_PATTERN, oracleDatabaseType), is(TABLE_NAME_PATTERN.toUpperCase()));
        assertThat(DatabaseMetaDataDialectHandlerFacade.getTableNamePattern(TABLE_NAME_PATTERN, mysqlDatabaseType), is(TABLE_NAME_PATTERN));
    }
    
    @Test
    public void assertGetDelimiter() {
        Pair<String, String> oracleDelimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new OracleDatabaseType());
        assertThat(oracleDelimiter.getLeft(), is("\""));
        assertThat(oracleDelimiter.getRight(), is("\""));
    
        Pair<String, String> h2Delimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new H2DatabaseType());
        assertThat(h2Delimiter.getLeft(), is("\""));
        assertThat(h2Delimiter.getRight(), is("\""));
    
        Pair<String, String> postgreSQLDelimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new PostgreSQLDatabaseType());
        assertThat(postgreSQLDelimiter.getLeft(), is("\""));
        assertThat(postgreSQLDelimiter.getRight(), is("\""));
    
        Pair<String, String> sql92Delimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new SQL92DatabaseType());
        assertThat(sql92Delimiter.getLeft(), is("\""));
        assertThat(sql92Delimiter.getRight(), is("\""));
    
        Pair<String, String> mariaDBDelimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new MariaDBDatabaseType());
        assertThat(mariaDBDelimiter.getLeft(), is("`"));
        assertThat(mariaDBDelimiter.getRight(), is("`"));
        
        Pair<String, String> mysqlDelimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new MySQLDatabaseType());
        assertThat(mysqlDelimiter.getLeft(), is("`"));
        assertThat(mysqlDelimiter.getRight(), is("`"));
    
        Pair<String, String> sqlServerDelimiter = DatabaseMetaDataDialectHandlerFacade.getDelimiter(new SQLServerDatabaseType());
        assertThat(sqlServerDelimiter.getLeft(), is("["));
        assertThat(sqlServerDelimiter.getRight(), is("]"));
    }
}
