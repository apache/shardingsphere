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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateTableStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingCreateTableStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test(expected = TableExistsException.class)
    public void assertValidateMySQLCreateTable() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        sqlStatement.setNotExisted(false);
        assertValidateCreateTable(sqlStatement);
    }
    
    @Test(expected = TableExistsException.class)
    public void assertValidateOracleCreateTable() {
        OracleCreateTableStatement sqlStatement = new OracleCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        assertValidateCreateTable(sqlStatement);
    }
    
    @Test(expected = TableExistsException.class)
    public void assertValidatePostgreSQLCreateTable() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        sqlStatement.setNotExisted(false);
        assertValidateCreateTable(sqlStatement);
    }
    
    @Test(expected = TableExistsException.class)
    public void assertValidateSQL92CreateTable() {
        SQL92CreateTableStatement sqlStatement = new SQL92CreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        assertValidateCreateTable(sqlStatement);
    }
    
    @Test(expected = TableExistsException.class)
    public void assertValidateSQLServerCreateTable() {
        SQLServerCreateTableStatement sqlStatement = new SQLServerCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        assertValidateCreateTable(sqlStatement);
    }
    
    private void assertValidateCreateTable(final CreateTableStatement sqlStatement) {
        SQLStatementContext<CreateTableStatement> sqlStatementContext = new CreateTableStatementContext(sqlStatement);
        PhysicalSchemaMetaData schema = mock(PhysicalSchemaMetaData.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        new ShardingCreateTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
    
    @Test
    public void assertValidateMySQLCreateTableIfNotExists() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        sqlStatement.setNotExisted(true);
        assertValidateCreateTableIfNotExists(sqlStatement);
    }
    
    @Test
    public void assertValidatePostgreSQLCreateTableIfNotExists() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        sqlStatement.setNotExisted(true);
        assertValidateCreateTableIfNotExists(sqlStatement);
    }
    
    private void assertValidateCreateTableIfNotExists(final CreateTableStatement sqlStatement) {
        SQLStatementContext<CreateTableStatement> sqlStatementContext = new CreateTableStatementContext(sqlStatement);
        PhysicalSchemaMetaData schema = mock(PhysicalSchemaMetaData.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        new ShardingCreateTableStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
}
