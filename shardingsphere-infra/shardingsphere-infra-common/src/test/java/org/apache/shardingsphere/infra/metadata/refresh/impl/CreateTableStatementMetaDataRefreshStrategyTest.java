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

package org.apache.shardingsphere.infra.metadata.refresh.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.model.schema.model.column.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.model.schema.model.index.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.model.schema.model.table.TableMetaData;
import org.apache.shardingsphere.infra.metadata.refresh.AbstractMetaDataRefreshStrategyTest;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateTableStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshMySQLCreateTableMetaData() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshOracleCreateTableMetaData() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshPostgreSQLCreateTableMetaData() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshSQL92CreateTableMetaData() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshSQLServerCreateTableMetaData() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refreshMetaData(createTableStatement);
    }

    private void refreshMetaData(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        MetaDataRefreshStrategy<CreateTableStatement> metaDataRefreshStrategy = new CreateTableStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), mock(DatabaseType.class), Collections.emptyMap(), createTableStatement, tableName -> Optional.of(new TableMetaData(
                Collections.singletonList(new ColumnMetaData("order_id", 1, "String", true, false, false)),
                Collections.singletonList(new IndexMetaData("index")))));
        assertTrue(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().containsTable("t_order_0"));
    }
    
    @Test
    public void assertRefreshMySQLCreateTableMetaDataWithUnConfigured() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        assertRefreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void assertRefreshOracleCreateTableMetaDataWithUnConfigured() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        assertRefreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void assertRefreshPostgreSQLCreateTableMetaDataWithUnConfigured() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        assertRefreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void assertRefreshSQL92CreateTableMetaDataWithUnConfigured() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        assertRefreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void assertRefreshSQLServerCreateTableMetaDataWithUnConfigured() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        assertRefreshMetaDataWithUnConfigured(createTableStatement);
    }

    private void assertRefreshMetaDataWithUnConfigured(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item_0"))));
        Map<String, DataSource> dataSourceSourceMap = new LinkedHashMap<>(1, 1);
        dataSourceSourceMap.put("t_order_item", initDataSource());
        MetaDataRefreshStrategy<CreateTableStatement> metaDataRefreshStrategy = new CreateTableStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), new MySQLDatabaseType(), dataSourceSourceMap, createTableStatement, tableName -> Optional.empty());
        assertTrue(getMetaData().getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap().get("t_order_item").contains("t_order_item_0"));
    }
    
    private DataSource initDataSource() throws SQLException {
        final String catalog = "catalog";
        final String table = "t_order_item_0";
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(result.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn(catalog);
        when(connection.getSchema()).thenReturn("");
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        ResultSet columnMetaDataResultSet = mock(ResultSet.class);
        ResultSet primaryKeyResultSet = mock(ResultSet.class);
        ResultSet tableResultSet = mock(ResultSet.class);
        ResultSet indexMetaDataResultSet = mock(ResultSet.class);
        when(databaseMetaData.getColumns(catalog, "", table, "%")).thenReturn(columnMetaDataResultSet);
        when(databaseMetaData.getPrimaryKeys(catalog, "", table)).thenReturn(primaryKeyResultSet);
        when(databaseMetaData.getTables(catalog, "", table, null)).thenReturn(tableResultSet);
        when(databaseMetaData.getIndexInfo(catalog, "", table, false, false)).thenReturn(indexMetaDataResultSet);
        when(tableResultSet.next()).thenReturn(true);
        return result;
    }
}
