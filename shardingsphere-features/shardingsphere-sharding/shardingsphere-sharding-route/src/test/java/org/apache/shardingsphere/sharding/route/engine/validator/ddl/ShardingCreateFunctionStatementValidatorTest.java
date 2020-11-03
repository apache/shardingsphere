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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateFunctionStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingCreateFunctionStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    public void assertValidateCreateFunctionForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(createTableStatement);
        ValidStatementSegment selectValidStatementSegment = new ValidStatementSegment(0, 0);
        selectValidStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        routineBody.getValidStatements().add(selectValidStatementSegment);
        MySQLCreateFunctionStatement sqlStatement = new MySQLCreateFunctionStatement();
        sqlStatement.setRoutineBody(routineBody);
        SQLStatementContext<CreateFunctionStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getTableAddressingMetaData().getTableDataSourceNamesMapper().containsKey("t_order_item")).thenReturn(true);
        new ShardingCreateFunctionStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateCreateFunctionWithShardingTableForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        MySQLCreateFunctionStatement sqlStatement = new MySQLCreateFunctionStatement();
        sqlStatement.setRoutineBody(routineBody);
        SQLStatementContext<CreateFunctionStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        new ShardingCreateFunctionStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
    
    @Test(expected = NoSuchTableException.class)
    public void assertValidateCreateFunctionWithNoSuchTableForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        MySQLCreateFunctionStatement sqlStatement = new MySQLCreateFunctionStatement();
        sqlStatement.setRoutineBody(routineBody);
        SQLStatementContext<CreateFunctionStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        new ShardingCreateFunctionStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
    
    @Test(expected = TableExistsException.class)
    public void assertValidateCreateFunctionWithTableExistsForMySQL() {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(createTableStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        MySQLCreateFunctionStatement sqlStatement = new MySQLCreateFunctionStatement();
        sqlStatement.setRoutineBody(routineBody);
        SQLStatementContext<CreateFunctionStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getTableAddressingMetaData().getTableDataSourceNamesMapper().containsKey("t_order")).thenReturn(true);
        new ShardingCreateFunctionStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
}
