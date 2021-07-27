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

package org.apache.shardingsphere.infra.metadata.schema.refresher.type;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateTableStatementSchemaRefresherTest {
    
    @Mock
    private SchemaBuilderMaterials materials;
    
    @Test
    public void refreshForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshWithTableRuleForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        refreshWithTableRule(createTableStatement);
    }
    
    @Test
    public void refreshWithTableRuleForOracle() throws SQLException {
        refreshWithTableRule(new OracleCreateTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setContainsNotExistClause(false);
        refreshWithTableRule(createTableStatement);
    }
    
    @Test
    public void refreshWithTableRuleForSQL92() throws SQLException {
        refreshWithTableRule(new SQL92CreateTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForSQLServer() throws SQLException {
        refreshWithTableRule(new SQLServerCreateTableStatement());
    }
    
    // TODO add more tests for tables with table rule
    private void refresh(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        when(materials.getDataSourceMap()).thenReturn(Collections.singletonMap("ds", dataSource));
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        SchemaRefresher<CreateTableStatement> schemaRefresher = new CreateTableStatementSchemaRefresher();
        schemaRefresher.refresh(schema, Collections.singleton("ds"), createTableStatement, materials);
        assertTrue(schema.containsTable("t_order_0"));
    }
    
    private void refreshWithTableRule(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        TableContainedRule rule = mock(TableContainedRule.class);
        when(materials.getRules()).thenReturn(Collections.singletonList(rule));
        when(rule.getTables()).thenReturn(Collections.singletonList("t_order_0"));
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        SchemaRefresher<CreateTableStatement> schemaRefresher = new CreateTableStatementSchemaRefresher();
        schemaRefresher.refresh(schema, Collections.singleton("ds"), createTableStatement, materials);
        assertTrue(schema.containsTable("t_order_0"));
    }
}
