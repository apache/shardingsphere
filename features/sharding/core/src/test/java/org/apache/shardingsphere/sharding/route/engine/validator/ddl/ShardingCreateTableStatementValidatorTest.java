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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCreateTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private RouteContext routeContext;
    
    @Test
    void assertPreValidateCreateTableForMySQL() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertPreValidateCreateTable(sqlStatement, "sharding_db"));
    }
    
    @Test
    void assertPreValidateCreateTableForOracle() {
        OracleCreateTableStatement sqlStatement = new OracleCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertPreValidateCreateTable(sqlStatement, "sharding_db"));
    }
    
    @Test
    void assertPreValidateCreateTableForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertPreValidateCreateTable(sqlStatement, "public"));
    }
    
    @Test
    void assertPreValidateCreateTableForSQL92() {
        SQL92CreateTableStatement sqlStatement = new SQL92CreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertPreValidateCreateTable(sqlStatement, "sharding_db"));
    }
    
    @Test
    void assertPreValidateCreateTableForSQLServer() {
        SQLServerCreateTableStatement sqlStatement = new SQLServerCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertThrows(TableExistsException.class, () -> assertPreValidateCreateTable(sqlStatement, "sharding_db"));
    }
    
    private void assertPreValidateCreateTable(final CreateTableStatement sqlStatement, final String schemaName) {
        SQLStatementContext sqlStatementContext = new CreateTableStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("sharding_db");
        when(database.getSchema(schemaName).containsTable("t_order")).thenReturn(true);
        new ShardingCreateTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertPreValidateCreateTableIfNotExistsForMySQL() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement(true);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertPreValidateCreateTableIfNotExists(sqlStatement);
    }
    
    @Test
    void assertPreValidateCreateTableIfNotExistsForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(true);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        assertPreValidateCreateTableIfNotExists(sqlStatement);
    }
    
    private void assertPreValidateCreateTableIfNotExists(final CreateTableStatement sqlStatement) {
        SQLStatementContext sqlStatementContext = new CreateTableStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        new ShardingCreateTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertPostValidateCreateTableWithSameRouteResultShardingTableForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        assertDoesNotThrow(() -> new ShardingCreateTableStatementValidator().postValidate(
                shardingRule, new CreateTableStatementContext(sqlStatement), new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateCreateTableWithDifferentRouteResultShardingTableForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        when(shardingRule.getTableRule("t_order")).thenReturn(new TableRule(Arrays.asList("ds_0", "ds_1"), "t_order"));
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t_order", "t_order_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        assertThrows(ShardingDDLRouteException.class, () -> new ShardingCreateTableStatementValidator().postValidate(shardingRule,
                new CreateTableStatementContext(sqlStatement), new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertPostValidateCreateTableWithSameRouteResultBroadcastTableForPostgreSQL() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_config"))));
        assertDoesNotThrow(() -> new ShardingCreateTableStatementValidator().postValidate(
                shardingRule, new CreateTableStatementContext(sqlStatement), new HintValueContext(), Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext));
    }
}
