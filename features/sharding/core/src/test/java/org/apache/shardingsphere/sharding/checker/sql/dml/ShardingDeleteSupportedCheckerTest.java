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

package org.apache.shardingsphere.sharding.checker.sql.dml;

import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.dml.OracleDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dml.SQL92DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dml.SQLServerDeleteStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingDeleteSupportedCheckerTest {
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckWhenDeleteMultiTablesForMySQL() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertCheckWhenDeleteMultiTables(new MySQLDeleteStatement()));
    }
    
    @Test
    void assertCheckWhenDeleteMultiTablesForOracle() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertCheckWhenDeleteMultiTables(new OracleDeleteStatement()));
    }
    
    @Test
    void assertCheckWhenDeleteMultiTablesForPostgreSQL() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertCheckWhenDeleteMultiTables(new PostgreSQLDeleteStatement()));
    }
    
    @Test
    void assertCheckWhenDeleteMultiTablesForSQL92() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertCheckWhenDeleteMultiTables(new SQL92DeleteStatement()));
    }
    
    @Test
    void assertCheckWhenDeleteMultiTablesForSQLServer() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertCheckWhenDeleteMultiTables(new SQLServerDeleteStatement()));
    }
    
    private void assertCheckWhenDeleteMultiTables(final DeleteStatement sqlStatement) {
        DeleteMultiTableSegment tableSegment = new DeleteMultiTableSegment();
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order_item"))));
        sqlStatement.setTable(tableSegment);
        Collection<String> tableNames = new HashSet<>(Arrays.asList("user", "order", "order_item"));
        when(rule.isAllShardingTables(tableNames)).thenReturn(false);
        when(rule.containsShardingTable(tableNames)).thenReturn(true);
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(sqlStatement);
        new ShardingDeleteSupportedChecker().check(rule, mock(), mock(), sqlStatementContext);
    }
}
