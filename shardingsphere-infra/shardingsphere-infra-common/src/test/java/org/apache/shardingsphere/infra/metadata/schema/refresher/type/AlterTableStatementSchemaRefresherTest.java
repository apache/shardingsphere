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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTableStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        refresh(new MySQLAlterTableStatement());
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        refresh(new OracleAlterTableStatement());
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        refresh(new PostgreSQLAlterTableStatement());
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        refresh(new SQL92AlterTableStatement());
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        refresh(new SQLServerAlterTableStatement());
    }
    
    @Test
    public void refreshWithRenameTableForPostgreSQL() throws SQLException {
        refreshWithRenameTable(new PostgreSQLAlterTableStatement());
    }
    
    private void refresh(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        SchemaRefresher<AlterTableStatement> schemaRefresher = new AlterTableStatementSchemaRefresher();
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), schema);
        schemaRefresher.refresh(metaData, Collections.singletonList("ds"), alterTableStatement, new ConfigurationProperties(new Properties()));
        assertTrue(schema.containsTable("t_order"));
    }
    
    private void refreshWithRenameTable(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        alterTableStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_new"))));
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getTables(any(), any(), any(), any())).thenReturn(mock(ResultSet.class));
        SchemaRefresher<AlterTableStatement> schemaRefresher = new AlterTableStatementSchemaRefresher();
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), schema);
        schemaRefresher.refresh(metaData, Collections.singletonList("ds"), alterTableStatement, new ConfigurationProperties(new Properties()));
        assertFalse(schema.containsTable("t_order"));
        assertTrue(schema.containsTable("t_order_new"));
    }
}
