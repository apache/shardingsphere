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

package org.apache.shardingsphere.infra.optimize.core.metadata.refresher;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederateSchemaMetadata;
import org.apache.shardingsphere.infra.optimize.core.metadata.refresher.type.CreateTableStatementFederateRefresher;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateTableStatementFederateRefresherTest {

    private final SchemaBuilderMaterials materials = mock(SchemaBuilderMaterials.class);

    @Test
    public void refreshForMySQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        refreshTableWithRule(new MySQLCreateTableStatement());
    }

    @Test
    public void refreshForOracle() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        refreshTableWithRule(new OracleCreateTableStatement());
    }

    @Test
    public void refreshForPostgreSQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        refreshTableWithRule(new PostgreSQLCreateTableStatement());
    }

    @Test
    public void refreshForSQL92() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        refreshTableWithRule(new SQL92CreateTableStatement());
    }

    @Test
    public void refreshForSQLServer() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        refreshTableWithRule(new SQLServerCreateTableStatement());
    }

    @Test
    public void refreshWithoutRuleForMySQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        refreshTableWithoutRule(new MySQLCreateTableStatement());
    }

    @Test
    public void refreshWithoutRuleForOracle() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        refreshTableWithoutRule(new OracleCreateTableStatement());
    }

    @Test
    public void refreshWithoutRuleForPostgreSQL() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        refreshTableWithoutRule(new PostgreSQLCreateTableStatement());
    }

    @Test
    public void refreshWithoutRuleForSQL92() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        refreshTableWithoutRule(new SQL92CreateTableStatement());
    }

    @Test
    public void refreshWithoutRuleForSQLServer() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        refreshTableWithoutRule(new SQLServerCreateTableStatement());
    }

    private void refreshTableWithRule(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        TableContainedRule rule = mock(TableContainedRule.class);
        Collection<ShardingSphereRule> rules = Collections.singletonList(rule);
        when(materials.getRules()).thenReturn(rules);
        when(rule.getTables()).thenReturn(Collections.singletonList("t_order"));
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
        FederateRefresher<CreateTableStatement> federateRefresher = new CreateTableStatementFederateRefresher();
        FederateSchemaMetadata schema = buildSchema();
        federateRefresher.refresh(schema, Collections.singletonList("ds"), createTableStatement, materials);
        assertTrue(schema.getTables().containsKey("t_order"));
        assertFalse(schema.getTables().get("t_order").getColumnNames().contains("order_id"));
    }

    private void refreshTableWithoutRule(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        when(materials.getDataSourceMap()).thenReturn(dataSourceMap);
        DataSource dataSource = mock(DataSource.class);
        when(dataSourceMap.get(eq("t_order_item"))).thenReturn(dataSource);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        ResultSet resultSet = mock(ResultSet.class);
        when(metaData.getTables(any(), any(), any(), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        FederateRefresher<CreateTableStatement> federateRefresher = new CreateTableStatementFederateRefresher();
        FederateSchemaMetadata schema = buildSchema();
        federateRefresher.refresh(schema, Collections.singletonList("t_order_item"), createTableStatement, materials);
        assertTrue(schema.getTables().containsKey("t_order"));
        assertFalse(schema.getTables().get("t_order").getColumnNames().contains("order_id"));
    }

    private FederateSchemaMetadata buildSchema() {
        Map<String, TableMetaData> metaData = ImmutableMap.of("t_order", new TableMetaData("t_order", Collections.singletonList(new ColumnMetaData("order_id", 1, false, false, false)),
                Collections.singletonList(new IndexMetaData("index"))));
        return new FederateSchemaMetadata("t_order", metaData);
    }
}
