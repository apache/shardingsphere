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
import org.apache.shardingsphere.infra.optimize.core.metadata.refresher.type.AlterTableStatementFederateRefresher;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterTableStatementFederateRefresherTest {
    
    @Mock
    private SchemaBuilderMaterials materials;
    
    @Test
    public void refreshTableWithRule() throws SQLException {
        refreshTableWithRule(new MySQLAlterTableStatement());
        refreshTableWithRule(new OracleAlterTableStatement());
        refreshTableWithRule(new PostgreSQLAlterTableStatement());
        refreshTableWithRule(new SQLServerAlterTableStatement());
        refreshTableWithRule(new SQL92AlterTableStatement());
    }
    
    private void refreshTableWithRule(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        TableContainedRule rule = mock(TableContainedRule.class);
        when(rule.getTables()).thenReturn(Collections.singletonList("t_order"));
        when(materials.getRules()).thenReturn(Collections.singletonList(rule));
        FederateRefresher<AlterTableStatement> federateRefresher = new AlterTableStatementFederateRefresher();
        FederateSchemaMetadata schema = buildSchema();
        federateRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, materials);
        assertTrue(schema.getTables().containsKey("t_order"));
    }
    
    @Test
    public void refreshTableWithoutRule() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        refreshTableWithoutRule(new MySQLAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        refreshTableWithoutRule(new OracleAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        refreshTableWithoutRule(new PostgreSQLAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        refreshTableWithoutRule(new SQLServerAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        refreshTableWithoutRule(new SQL92AlterTableStatement());
    }
    
    private void refreshTableWithoutRule(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(
                new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        when(materials.getDataSourceMap()).thenReturn(Collections.singletonMap("ds", dataSource));
        FederateRefresher<AlterTableStatement> federateRefresher = new AlterTableStatementFederateRefresher();
        FederateSchemaMetadata schema = buildSchema();
        federateRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, materials);
        assertTrue(schema.getTables().containsKey("t_order"));
    }
    
    @Test
    public void renameTableWithRule() throws SQLException {
        renameTableWithRule(new MySQLAlterTableStatement());
        renameTableWithRule(new OracleAlterTableStatement());
        renameTableWithRule(new PostgreSQLAlterTableStatement());
        renameTableWithRule(new SQLServerAlterTableStatement());
        renameTableWithRule(new SQL92AlterTableStatement());
    }
    
    private void renameTableWithRule(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        alterTableStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_new"))));
        TableContainedRule rule = mock(TableContainedRule.class);
        when(rule.getTables()).thenReturn(Arrays.asList("t_order", "t_order_new"));
        when(materials.getRules()).thenReturn(Collections.singletonList(rule));
        FederateRefresher<AlterTableStatement> federateRefresher = new AlterTableStatementFederateRefresher();
        FederateSchemaMetadata schema = buildSchema();
        federateRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, materials);
        assertFalse(schema.getTables().containsKey("t_order"));
        assertTrue(schema.getTables().containsKey("t_order_new"));
    }
    
    @Test
    public void renameTableWithoutRule() throws SQLException {
        when(materials.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        renameTableWithoutRule(new MySQLAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new OracleDatabaseType());
        renameTableWithoutRule(new OracleAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        renameTableWithoutRule(new PostgreSQLAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new SQLServerDatabaseType());
        renameTableWithoutRule(new SQLServerAlterTableStatement());
        when(materials.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        renameTableWithoutRule(new SQL92AlterTableStatement());
    }
    
    private void renameTableWithoutRule(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        alterTableStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_new"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        when(materials.getDataSourceMap()).thenReturn(Collections.singletonMap("ds", dataSource));
        FederateRefresher<AlterTableStatement> federateRefresher = new AlterTableStatementFederateRefresher();
        FederateSchemaMetadata schema = buildSchema();
        federateRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, materials);
        assertFalse(schema.getTables().containsKey("t_order"));
        assertTrue(schema.getTables().containsKey("t_order_new"));
    }
    
    private FederateSchemaMetadata buildSchema() {
        Map<String, TableMetaData> metaData = ImmutableMap.of("t_order", new TableMetaData("t_order", Collections.singletonList(new ColumnMetaData("order_id", 1, false, false, false)),
                        Collections.singletonList(new IndexMetaData("index"))));
        return new FederateSchemaMetadata("t_order", metaData);
    }
}
