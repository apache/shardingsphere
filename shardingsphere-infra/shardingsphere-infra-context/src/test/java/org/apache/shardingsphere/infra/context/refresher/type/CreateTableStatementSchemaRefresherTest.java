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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateTableStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                final Collection<String> logicDataSourceNames, final ConfigurationProperties props) throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        refresh(schemaMetaData, schema, optimizerPlanners, logicDataSourceNames, createTableStatement, props);
    }
    
    @Test
    public void refreshForOracle(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                 final Collection<String> logicDataSourceNames, final ConfigurationProperties props) throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refresh(schemaMetaData, schema, optimizerPlanners, logicDataSourceNames, createTableStatement, props);
    }
    
    @Test
    public void refreshForPostgreSQL(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                     final Collection<String> logicDataSourceNames, final ConfigurationProperties props) throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        refresh(schemaMetaData, schema, optimizerPlanners, logicDataSourceNames, createTableStatement, props);
    }
    
    @Test
    public void refreshForSQL92(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                final Collection<String> logicDataSourceNames, final ConfigurationProperties props) throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refresh(schemaMetaData, schema, optimizerPlanners, logicDataSourceNames, createTableStatement, props);
    }
    
    @Test
    public void refreshForSQLServer(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                    final Collection<String> logicDataSourceNames, final ConfigurationProperties props) throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refresh(schemaMetaData, schema, optimizerPlanners, logicDataSourceNames, createTableStatement, props);
    }
    
    private void refresh(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners, 
                         final Collection<String> logicDataSourceNames, final CreateTableStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        MetaDataRefresher<CreateTableStatement> schemaRefresher = new CreateTableStatementSchemaRefresher();
        schemaRefresher.refresh(schemaMetaData, schema, optimizerPlanners, Collections.singleton("ds"), sqlStatement, props);
        assertTrue(schema.containsTable("t_order_0"));
    }
}
