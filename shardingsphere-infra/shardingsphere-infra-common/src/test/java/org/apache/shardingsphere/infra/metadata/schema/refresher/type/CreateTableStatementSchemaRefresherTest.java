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

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class CreateTableStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refresh(createTableStatement);
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refresh(createTableStatement);
    }
    
    private void refresh(final CreateTableStatement createTableStatement) throws SQLException {
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_0"))));
        SchemaRefresher<CreateTableStatement> schemaRefresher = new CreateTableStatementSchemaRefresher();
        SchemaBuilderMaterials materials = mock(SchemaBuilderMaterials.class);
        schemaRefresher.refresh(schema, Collections.singleton("ds"), createTableStatement, materials);
        assertTrue(schema.containsTable("t_order_0"));
    }
    
    @Test
    public void refreshWithUnConfiguredForMySQL() throws SQLException {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshWithUnConfigured(createTableStatement);
    }
    
    @Test
    public void refreshWithUnConfiguredForOracle() throws SQLException {
        OracleCreateTableStatement createTableStatement = new OracleCreateTableStatement();
        refreshWithUnConfigured(createTableStatement);
    }
    
    @Test
    public void refreshWithUnConfiguredForPostgreSQL() throws SQLException {
        PostgreSQLCreateTableStatement createTableStatement = new PostgreSQLCreateTableStatement();
        createTableStatement.setNotExisted(false);
        refreshWithUnConfigured(createTableStatement);
    }
    
    @Test
    public void refreshWithUnConfiguredForSQL92() throws SQLException {
        SQL92CreateTableStatement createTableStatement = new SQL92CreateTableStatement();
        refreshWithUnConfigured(createTableStatement);
    }
    
    @Test
    public void refreshWithUnConfiguredForSQLServer() throws SQLException {
        SQLServerCreateTableStatement createTableStatement = new SQLServerCreateTableStatement();
        refreshWithUnConfigured(createTableStatement);
    }
    
    private void refreshWithUnConfigured(final CreateTableStatement createTableStatement) throws SQLException {
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item_0"))));
        SchemaRefresher<CreateTableStatement> schemaRefresher = new CreateTableStatementSchemaRefresher();
        schemaRefresher.refresh(ShardingSphereSchemaBuildUtil.buildSchema(), Collections.singletonList("t_order_item"), createTableStatement, mock(SchemaBuilderMaterials.class));
    }
}
