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
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
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

public final class DropTableStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        refresh(new MySQLDropTableStatement());   
    }
    
    @Test
    public void refreshForOracle() throws SQLException {
        refresh(new OracleDropTableStatement());
    }
    
    @Test
    public void refreshForPostgreSQL() throws SQLException {
        refresh(new PostgreSQLDropTableStatement());
    }
    
    @Test
    public void refreshForSQL92() throws SQLException {
        refresh(new SQL92DropTableStatement());
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        refresh(new SQLServerDropTableStatement());
    }
    
    private void refresh(final DropTableStatement dropTableStatement) throws SQLException {
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        SchemaRefresher<DropTableStatement> schemaRefresher = new DropTableStatementSchemaRefresher();
        dropTableStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        schemaRefresher.refresh(schema, Collections.emptyList(), dropTableStatement, tableName -> Optional.empty());
        assertFalse(schema.containsTable("t_order"));
    }
    
    @Test
    public void refreshWithUnConfiguredForMySQL() throws SQLException {
        refreshWithUnConfigured(new MySQLDropTableStatement());
    }
    
    @Test
    public void refreshWithUnConfiguredForOracle() throws SQLException {
        refreshWithUnConfigured(new OracleDropTableStatement());
    }
    
    @Test
    public void refreshWithUnConfiguredForPostgreSQL() throws SQLException {
        refreshWithUnConfigured(new PostgreSQLDropTableStatement());
    }
    
    @Test
    public void refreshWithUnConfiguredForSQL92() throws SQLException {
        refreshWithUnConfigured(new SQL92DropTableStatement());
    }
    
    @Test
    public void refreshWithUnConfiguredForSQLServer() throws SQLException {
        refreshWithUnConfigured(new SQLServerDropTableStatement());
    }
    
    private void refreshWithUnConfigured(final DropTableStatement dropTableStatement) throws SQLException {
        SchemaRefresher<DropTableStatement> schemaRefresher = new DropTableStatementSchemaRefresher();
        dropTableStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item"))));
        schemaRefresher.refresh(ShardingSphereSchemaBuildUtil.buildSchema(), Collections.singletonList("t_order_item"), dropTableStatement, tableName -> Optional.empty());
    }
}
