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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCreateTableSupportedCheckerTest {
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckForMySQL() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertCheck(sqlStatement));
    }
    
    @Test
    void assertCheckForOracle() {
        OracleCreateTableStatement sqlStatement = new OracleCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertCheck(sqlStatement));
    }
    
    @Test
    void assertCheckForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertCheck(sqlStatement));
    }
    
    @Test
    void assertCheckForSQL92() {
        SQL92CreateTableStatement sqlStatement = new SQL92CreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertCheck(sqlStatement));
    }
    
    @Test
    void assertCheckForSQLServer() {
        SQLServerCreateTableStatement sqlStatement = new SQLServerCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertCheck(sqlStatement));
    }
    
    private void assertCheck(final CreateTableStatement sqlStatement) {
        CreateTableStatementContext sqlStatementContext = new CreateTableStatementContext(sqlStatement, "sharding_db");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        new ShardingCreateTableSupportedChecker().check(rule, database, schema, sqlStatementContext);
    }
    
    @Test
    void assertCheckIfNotExistsForMySQL() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement(true);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertCheckIfNotExists(sqlStatement);
    }
    
    @Test
    void assertCheckIfNotExistsForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(true);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertCheckIfNotExists(sqlStatement);
    }
    
    private void assertCheckIfNotExists(final CreateTableStatement sqlStatement) {
        CreateTableStatementContext sqlStatementContext = new CreateTableStatementContext(sqlStatement, "foo_db");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        new ShardingCreateTableSupportedChecker().check(rule, database, mock(), sqlStatementContext);
    }
}
