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
import org.apache.shardingsphere.infra.metadata.model.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;
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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class CreateTableStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshMetaDataForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshMetaDataForOracle() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshMetaDataForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshMetaDataForSQL92() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refreshMetaData(createTableStatement);
    }

    @Test
    public void refreshMetaDataForSQLServer() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refreshMetaData(createTableStatement);
    }

    private void refreshMetaData(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        MetaDataRefreshStrategy<CreateTableStatement> metaDataRefreshStrategy = new CreateTableStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), mock(DatabaseType.class), Collections.emptyList(), createTableStatement, tableName -> Optional.of(new PhysicalTableMetaData(
                Collections.singletonList(new PhysicalColumnMetaData("order_id", 1, "String", true, false, false)),
                Collections.singletonList(new PhysicalIndexMetaData("index")))));
        assertTrue(getMetaData().getSchemaMetaData().getConfiguredSchemaMetaData().containsTable("t_order_0"));
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void refreshMetaDataWithUnConfiguredForOracle() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void refreshMetaDataWithUnConfiguredForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void refreshMetaDataWithUnConfiguredForSQL92() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refreshMetaDataWithUnConfigured(createTableStatement);
    }

    @Test
    public void refreshMetaDataWithUnConfiguredForSQLServer() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refreshMetaDataWithUnConfigured(createTableStatement);
    }

    private void refreshMetaDataWithUnConfigured(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item_0"))));
        MetaDataRefreshStrategy<CreateTableStatement> metaDataRefreshStrategy = new CreateTableStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), new MySQLDatabaseType(), Collections.singletonList("t_order_item"), createTableStatement, tableName -> Optional.empty());
        assertTrue(getMetaData().getSchemaMetaData().getUnconfiguredSchemaMetaDataMap().get("t_order_item").contains("t_order_item_0"));
    }
}
