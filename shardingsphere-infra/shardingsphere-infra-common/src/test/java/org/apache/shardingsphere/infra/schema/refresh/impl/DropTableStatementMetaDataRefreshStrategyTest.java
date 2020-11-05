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

package org.apache.shardingsphere.infra.schema.refresh.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.schema.refresh.AbstractMetaDataRefreshStrategyTest;
import org.apache.shardingsphere.infra.schema.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTableStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public final class DropTableStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshDropTableMetaDataForMySQL() throws SQLException {
        refreshMetaData(new MySQLDropTableStatement());   
    }
    
    @Test
    public void refreshDropTableMetaDataForOracle() throws SQLException {
        refreshMetaData(new OracleDropTableStatement());
    }
    
    @Test
    public void refreshDropTableMetaDataForPostgreSQL() throws SQLException {
        refreshMetaData(new PostgreSQLDropTableStatement());
    }
    
    @Test
    public void refreshDropTableMetaDataForSQL92() throws SQLException {
        refreshMetaData(new SQL92DropTableStatement());
    }
    
    @Test
    public void refreshDropTableMetaDataForSQLServer() throws SQLException {
        refreshMetaData(new SQLServerDropTableStatement());
    }
    
    private void refreshMetaData(final DropTableStatement dropTableStatement) throws SQLException {
        MetaDataRefreshStrategy<DropTableStatement> metaDataRefreshStrategy = new DropTableStatementMetaDataRefreshStrategy();
        dropTableStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        metaDataRefreshStrategy.refreshMetaData(getSchema(), mock(DatabaseType.class), Collections.emptyList(), dropTableStatement, tableName -> Optional.empty());
        assertFalse(getSchema().getSchemaMetaData().containsTable("t_order"));
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForMySQL() throws SQLException {
        refreshMetaDataWithUnConfigured(new MySQLDropTableStatement());
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForOracle() throws SQLException {
        refreshMetaDataWithUnConfigured(new OracleDropTableStatement());
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForPostgreSQL() throws SQLException {
        refreshMetaDataWithUnConfigured(new PostgreSQLDropTableStatement());
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForSQL92() throws SQLException {
        refreshMetaDataWithUnConfigured(new SQL92DropTableStatement());
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForSQLServer() throws SQLException {
        refreshMetaDataWithUnConfigured(new SQLServerDropTableStatement());
    }
    
    private void refreshMetaDataWithUnConfigured(final DropTableStatement dropTableStatement) throws SQLException {
        MetaDataRefreshStrategy<DropTableStatement> metaDataRefreshStrategy = new DropTableStatementMetaDataRefreshStrategy();
        dropTableStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item"))));
        metaDataRefreshStrategy.refreshMetaData(getSchema(), mock(DatabaseType.class), Collections.singletonList("t_order_item"), dropTableStatement, tableName -> Optional.empty());
    }
}
