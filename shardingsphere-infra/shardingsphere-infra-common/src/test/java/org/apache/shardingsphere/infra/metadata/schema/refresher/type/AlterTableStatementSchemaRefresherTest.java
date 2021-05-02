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
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterTableStatement;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTableStatementSchemaRefresherTest {
    
    private SchemaBuilderMaterials materials = mock(SchemaBuilderMaterials.class);
    
    @Test
    public void refreshForMySQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        refresh(new MySQLAlterTableStatement());
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        refresh(new OracleAlterTableStatement());
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        refresh(new PostgreSQLAlterTableStatement());
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        refresh(new SQL92AlterTableStatement());
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        refresh(new SQLServerAlterTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForMySQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        refreshWithTableRule(new MySQLAlterTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForOracle() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        refreshWithTableRule(new OracleAlterTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForPostgreSQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        refreshWithTableRule(new PostgreSQLAlterTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForSQL92() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        refreshWithTableRule(new SQL92AlterTableStatement());
    }
    
    @Test
    public void refreshWithTableRuleForSQLServer() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        refreshWithTableRule(new SQLServerAlterTableStatement());
    }

    private void refresh(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        when(materials.getDataSourceMap()).thenReturn(dataSourceMap);
        DataSource dataSource = mock(DataSource.class);
        when(dataSourceMap.get(eq("ds"))).thenReturn(dataSource);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        ResultSet resultSet = mock(ResultSet.class);
        when(metaData.getTables(any(), any(), any(), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        SchemaRefresher<AlterTableStatement> schemaRefresher = new AlterTableStatementSchemaRefresher();
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        schemaRefresher.refresh(schema, Arrays.asList("ds"), alterTableStatement, materials);
        assertTrue(schema.containsTable("t_order"));
    }
    
    private void refreshWithTableRule(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        ShardingSphereRule rule = mock(TableContainedRule.class);
        Collection<ShardingSphereRule> rules = Arrays.asList(rule);
        when(materials.getRules()).thenReturn(rules);
        when(((TableContainedRule) rule).getTables()).thenReturn(Arrays.asList("t_order"));
        when(materials.getDataSourceMap()).thenReturn(dataSourceMap);
        DataSource dataSource = mock(DataSource.class);
        when(dataSourceMap.get(eq("ds"))).thenReturn(dataSource);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        ResultSet resultSet = mock(ResultSet.class);
        when(metaData.getTables(any(), any(), any(), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        SchemaRefresher<AlterTableStatement> schemaRefresher = new AlterTableStatementSchemaRefresher();
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        schemaRefresher.refresh(schema, Arrays.asList("ds"), alterTableStatement, materials);
        assertTrue(schema.containsTable("t_order"));
    }
}
